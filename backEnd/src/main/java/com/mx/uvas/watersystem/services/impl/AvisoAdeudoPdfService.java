package com.mx.uvas.watersystem.services.impl;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class AvisoAdeudoPdfService {

    private static final String LOGO_CLASSPATH = "pdf/logo_comite.png";
    private static final float MARGEN_LR = 32f;
    private static final float MARGEN_TB = 14f;

    private static final String HORARIO_COBRO_INICIO = "7:30 p.m.";
    private static final String HORARIO_COBRO_FIN = "9:00 p.m.";

    private static final String[] MESES = {
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    };

    // Reducidas ligeramente (13->12.3, 11->10.3, 8.1->7.7, 7.8->7.4) --
    // con todo lo que se le fue agregando a la página 1 con el tiempo
    // (desglose por año, multa acumulada, notas de Art. 10/22 Bis, Art. 27,
    // aviso de pago ya realizado...) las firmas y el pie de página ya no
    // cabían siempre en una sola hoja y se partían hacia la página 2. Ver
    // también los spacingAfter/spacingBefore más abajo, apretados en el
    // mismo ajuste.
    private final Font fontComite = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12.3f, new Color(0, 90, 160));
    private final Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.3f);
    private final Font fontTituloAviso = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.3f, Color.RED);
    private final Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 7.7f);
    private final Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.7f);
    private final Font fontItalicChico = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 6.7f);
    private final Font fontRojoNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.7f, Color.RED);
    // Mismo tamaño que fontNormal, pero en rojo sin negrita -- para el
    // párrafo de publicación de adeudos (Art. 4 Bis), que Ely pidió
    // resaltar en rojo (v6 de la carta, sept. 2026) sin que se vea tan
    // fuerte como fontRojoNegrita (esa se reserva para las advertencias
    // más serias, ej. "la multa por falta de pago aplica...").
    private final Font fontRojo = FontFactory.getFont(FontFactory.HELVETICA, 7.7f, Color.RED);
    private final Font fontRojoItalica = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 7.7f, Color.RED);
    private final Font fontFolio = FontFactory.getFont(FontFactory.HELVETICA, 7.4f);
    private final Font fontArticuloTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.8f);
    private final Font fontReglamentoTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    // Fuente más chica que fontNormal/fontBold, solo para el cuerpo de los
    // artículos citados en la página del reglamento (página 2) -- ese texto
    // es genérico (igual para todos), así que se puede achicar un poco más
    // que los datos del aviso para que, con el Art. 22 Bis y el Art. 27
    // agregados, los 11 artículos sigan cabiendo en una sola hoja.
    private final Font fontReglamentoBody = FontFactory.getFont(FontFactory.HELVETICA, 7f);
    private final Font fontReglamentoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f);
    // Para los datos que se llenan en automático dentro del texto (mes/año
    // de la fecha, horario de cobro) -- en rojo, negrita y subrayado aunque
    // ya vengan completados, para que resalten igual que un campo llenado a
    // mano en la carta de papel.
    private final Font fontDatoLlenado = FontFactory.getFont(FontFactory.HELVETICA, 7.7f, Font.BOLD | Font.UNDERLINE, Color.RED);

    public byte[] generarLote(List<CartaAdeudoDatos> cartas) throws IOException, DocumentException {
        Document document = new Document(PageSize.LETTER, MARGEN_LR, MARGEN_LR, MARGEN_TB, MARGEN_TB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, salida);
        document.open();

        byte[] logoBytes = leerLogo();

        boolean primera = true;
        for (CartaAdeudoDatos carta : cartas) {
            if (!primera) {
                document.newPage();
            }
            primera = false;
            agregarPaginaAviso(document, carta, logoBytes);
            document.newPage();
            agregarPaginaReglamento(document);
        }

        document.close();
        return salida.toByteArray();
    }

    private byte[] leerLogo() throws IOException {
        try (InputStream in = new ClassPathResource(LOGO_CLASSPATH).getInputStream()) {
            return in.readAllBytes();
        }
    }

    private void agregarPaginaAviso(Document document, CartaAdeudoDatos carta, byte[] logoBytes) throws DocumentException, IOException {
        boolean esSegundo = "SEGUNDO".equalsIgnoreCase(carta.tipoAviso());

        Paragraph folio = new Paragraph("Folio de notificación No. " + carta.folioNotificacion(), fontFolio);
        folio.setAlignment(Element.ALIGN_LEFT);
        folio.setSpacingAfter(1.5f);
        document.add(folio);

        if (logoBytes != null) {
            Image logo = Image.getInstance(logoBytes);
            logo.scaleToFit(32, 32);
            logo.setAlignment(Element.ALIGN_CENTER);
            logo.setSpacingBefore(0.5f);
            document.add(logo);
        }

        Paragraph comite = new Paragraph("COMITÉ DE AGUA POTABLE \"LOS LÓPEZ\"", fontComite);
        comite.setAlignment(Element.ALIGN_CENTER);
        comite.setSpacingBefore(0.5f);
        comite.setSpacingAfter(2f);
        document.add(comite);

        Paragraph titulo = new Paragraph("ACTA DE NOTIFICACIÓN DE ADEUDO Y AVISO DE SUSPENSIÓN DEL SERVICIO", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(0.8f);
        document.add(titulo);

        // Marca en rojo, justo después del título -- la manera principal de
        // distinguir primer/segundo aviso ahora que el resto del texto es
        // igual para los dos (antes cambiaba un párrafo completo).
        Paragraph tipoAvisoLinea = new Paragraph(esSegundo ? "SEGUNDO AVISO" : "PRIMER AVISO", fontTituloAviso);
        tipoAvisoLinea.setAlignment(Element.ALIGN_CENTER);
        tipoAvisoLinea.setSpacingAfter(2.2f);
        document.add(tipoAvisoLinea);

        // Fecha de emisión -- el día se deja en blanco (se llena a mano al
        // momento de entregar), pero mes y año sí se conocen de antemano y
        // se marcan en negrita+subrayado para que resalten igual que un
        // campo llenado a mano.
        Paragraph fecha = new Paragraph();
        fecha.add(new Chunk("León, Guanajuato, a ______ de ", fontNormal));
        fecha.add(new Chunk(mesActual(), fontDatoLlenado));
        fecha.add(new Chunk(" de 20", fontNormal));
        fecha.add(new Chunk(anioActualDosDigitos(), fontDatoLlenado));
        fecha.add(new Chunk(".", fontNormal));
        fecha.setAlignment(Element.ALIGN_CENTER);
        fecha.setSpacingAfter(2.2f);
        document.add(fecha);

        NumberFormat formatoMonedaTabla = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

        PdfPTable datos = new PdfPTable(2);
        datos.setWidthPercentage(100);
        datos.setWidths(new float[]{42f, 58f});
        agregarFilaDatos(datos, "Nombre del usuario titular", nullToVacio(carta.nombreUsuarioTitular()));

        // No. Casa + Domicilio de la toma en el mismo renglón (v7, sept.
        // 2026, pedido de Ely, mismo criterio que Multa/Mantenimiento) --
        // Casa suele ser muy corto ("2-D") y se queda con poco espacio
        // vacío si va en su propio renglón de la tabla completa; si el
        // usuario no tiene casa capturada, el valor simplemente se deja en
        // blanco (la etiqueta se sigue mostrando).
        // El valor de "No. Casa" es corto (o va vacío) -- la etiqueta ya
        // trae su ancho justo, así que el valor solo necesita lo mínimo;
        // el espacio que sobraba se lo lleva el valor de "Domicilio de la
        // toma", que suele ser el dato más largo del renglón (v8, sept.
        // 2026, Ely reportó espacio en blanco desperdiciado ahí).
        agregarFilaDatosDoble(datos,
                "No. Casa", carta.noCasa() != null && !carta.noCasa().isBlank() ? carta.noCasa() : "",
                "Domicilio de la toma", nullToVacio(carta.domicilioToma()),
                new float[]{13f, 7f, 22f, 58f});

        agregarFilaDatos(datos, "Desglose del adeudo por año",
                carta.desglosePorAnio() != null && !carta.desglosePorAnio().isBlank() ? carta.desglosePorAnio() : "--",
                fontItalicChico);

        // Multa acumulada y mantenimiento de cajón, cada uno con su propia
        // etiqueta y monto (no texto libre en 2 líneas -- eso confundía).
        String textoMulta = carta.multaAcumulada() != null && carta.multaAcumulada() > 0
                ? formatoMonedaTabla.format(carta.multaAcumulada())
                : "Sin multas pendientes";
        // "$100.00 - 2024 | $100.00 - 2025" (monto antes del año) --
        // desglose de mantenimiento por año cuando aplica.
        String textoMantenimiento = carta.mantenimientoPendiente() != null && carta.mantenimientoPendiente() > 0
                ? formatoMonedaTabla.format(carta.mantenimientoPendiente())
                        + (carta.mantenimientoPorAnioTexto() != null && !carta.mantenimientoPorAnioTexto().isBlank()
                                ? " (" + carta.mantenimientoPorAnioTexto() + ")"
                                : "")
                : "Sin adeudo de mantenimiento";
        agregarFilaDatosDoble(datos,
                "Multa acumulada", textoMulta,
                "Mantenimiento", textoMantenimiento,
                new float[]{17f, 25f, 17f, 41f});

        // Folio y fecha del último pago, mismo criterio -- ambos son datos
        // cortos, no tiene caso que cada uno ocupe un renglón completo.
        agregarFilaDatosDoble(datos,
                "Folio último pago", carta.noFolioUltimoPago() != null ? String.valueOf(carta.noFolioUltimoPago()) : "Sin pagos registrados",
                "Fecha", carta.fechaUltimoPagoTexto() != null && !carta.fechaUltimoPagoTexto().isBlank() ? carta.fechaUltimoPagoTexto() : "--",
                new float[]{20f, 30f, 15f, 35f});

        datos.setSpacingAfter(2f);
        document.add(datos);

        // En el Segundo aviso, si se sabe cuándo/a quién se entregó el
        // Primero (ver AvisoAdeudoService.construirNotaEntregaPrimerAviso()),
        // se muestra eso en vez de las 3 notas genéricas de abajo -- ya no
        // hace falta el disclaimer de mantenimiento (ahora se calcula e
        // incluye solo, ver "Multa acumulada" arriba) ni repetir el Art.
        // 22 Bis (ya está completo en la página 2/reglamento), y en cambio
        // sí es útil dejar constancia de la entrega anterior en la misma
        // carta que agrava la situación del usuario.
        if (esSegundo && carta.notaEntregaPrimerAviso() != null) {
            Paragraph notaEntrega = new Paragraph(carta.notaEntregaPrimerAviso(), fontItalicChico);
            notaEntrega.setSpacingAfter(1.8f);
            document.add(notaEntrega);
        } else {
            // Notas, cada una en su propio párrafo chico (mismo criterio
            // que la versión más reciente de la carta física): de qué está
            // hecho el adeudo/multa, qué falta por revisar aparte
            // (cooperaciones extraordinarias fuera de 2024/2025, que no se
            // calculan solas), y la regla de prelación de pagos (Art. 22
            // Bis).
            Paragraph nota1 = new Paragraph("El adeudo indicado corresponde a la aportación de luz (CFE); la multa y el mantenimiento son "
                    + "conceptos independientes, cada uno con su propio monto en la tabla de arriba. "
                    + "Los datos pueden contener errores de captura; el usuario o el Comité pueden validarlos con el expediente físico en las "
                    + "oficinas del Comité.", fontItalicChico);
            nota1.setSpacingAfter(1.1f);
            document.add(nota1);

            // Redactada de nuevo (v6, sept. 2026) -- ya no dice que el
            // mantenimiento "no está incluido" (dejó de ser cierto desde
            // que se agregó al renglón combinado de arriba); ahora aclara
            // que el cálculo automático es una aproximación y puede no
            // detectar todos los años pendientes (ver
            // AvisoAdeudoService.crearCargoMantenimiento() / ANIOS_MANTENIMIENTO,
            // que solo revisa años con adeudo de luz).
            Paragraph nota2 = new Paragraph("El mantenimiento mostrado en el renglón de arriba puede no incluir todos los años pendientes; "
                    + "verifique con el expediente físico.", fontItalicChico);
            nota2.setSpacingAfter(1.1f);
            document.add(nota2);

            Paragraph nota3 = new Paragraph("Conforme al Artículo 22 Bis del Reglamento Interno, cuando un pago no cubra la totalidad de lo "
                    + "adeudado, se aplica primero a intereses moratorios, después a multas pendientes, en seguida a cooperaciones "
                    + "extraordinarias y, por último, a la aportación de luz (CFE).", fontItalicChico);
            nota3.setSpacingAfter(1.5f);
            document.add(nota3);
        }

        // Solo aplica a personas SIN usuario en el sistema (censo en
        // proceso) -- pudieron haber pagado a un comité anterior antes de
        // que existiera este registro, así que se les invita a presentar
        // ese comprobante para ajustar el adeudo mostrado.
        if (carta.esNoRegistrado()) {
            Paragraph notaNoRegistrado = new Paragraph(
                    "Si tiene recibos de algún comité anterior, favor de presentarlos para hacer el ajuste correspondiente.",
                    fontItalicChico);
            notaNoRegistrado.setSpacingAfter(1.8f);
            document.add(notaNoRegistrado);
        }

        // Usuario registrado con monto de adeudo capturado A MANO (ver
        // UsuarioManualAdeudoDto/AvisoAdeudoService) -- se deja constancia
        // en la carta del motivo, para que quede claro por qué el monto no
        // salió del cálculo automático (ej. cuenta juntada con la de un
        // familiar).
        if (carta.observacionManual() != null && !carta.observacionManual().isBlank()) {
            Paragraph notaManual = new Paragraph("Nota del Comité: " + carta.observacionManual(), fontRojoItalica);
            notaManual.setSpacingAfter(1.8f);
            document.add(notaManual);
        }

        // La consecuencia de no presentarse (siguiente aviso, o corte) ya
        // NO se menciona aquí -- se dice una sola vez, al final, con
        // redacción específica según si esta carta es Primero o Segundo
        // (ver el párrafo "De no presentarse..." más abajo). Antes se
        // repetía la misma idea dos veces en la carta.
        agregarParrafo(document, "Conforme al Artículo 20 del Reglamento Interno (ver artículos completos al reverso), el primer y el segundo "
                + "aviso tienen un costo de $200.00 cada uno, cargado a la cuenta del usuario. Si después del segundo aviso el usuario no se "
                + "pone al corriente, procede el corte del servicio en la siguiente visita, sin necesidad de emitir un tercer aviso.");

        agregarParrafo(document, "Este aviso también aplica si en su domicilio viven otras familias que no se han puesto al corriente con su "
                + "pago. El titular o dueño registrado de la toma es responsable del pago de todas las personas que habiten el domicilio; si "
                + "alguna deja de habitarlo, debe avisar por escrito al Comité para actualizar el padrón, pues de no hacerlo el cobro "
                + "continuará aplicándose hasta que se presente dicho aviso.");

        Paragraph multa = new Paragraph("La multa por falta de pago aplica cuando se tienen al menos 3 meses sin realizar el pago.", fontRojoNegrita);
        multa.setSpacingAfter(1.5f);
        document.add(multa);

        Paragraph suspension = new Paragraph();
        suspension.add(new Chunk("Con base en el reglamento, y por el adeudo arriba indicado, se le notifica que ", fontNormal));
        suspension.add(new Chunk("su toma está programada para suspensión del servicio", fontBold));
        suspension.add(new Chunk(" si no regulariza su situación conforme a este documento.", fontNormal));
        suspension.setSpacingAfter(1.5f);
        document.add(suspension);

        // Publicación de adeudos (Art. 4 Bis) -- agregado en la v5 de la
        // carta (sept. 2026), no existía antes en la página 1 ni en el
        // reglamento del reverso (ver agregarPaginaReglamento). En rojo
        // (v6, sept. 2026, pedido explícito de Ely) para que resalte igual
        // que las demás advertencias de la carta.
        Paragraph padron = new Paragraph("Por pertenecer al padrón de usuarios, su situación de adeudo podrá ser exhibida en lugares públicos de "
                + "la comunidad, en las redes sociales del Comité (incluyendo Facebook), y ante la asamblea (Artículo 4 Bis del Reglamento "
                + "Interno).", fontRojo);
        padron.setSpacingAfter(1.5f);
        document.add(padron);

        // Día y horario de presentación -- deliberadamente NO se describe
        // como un plazo/fecha límite (se aclara al final del párrafo): es
        // el día y horario puntual en que se le espera. Se usa la fecha
        // completa elegida al generar (carta.fechaPresentacion(), día+mes+
        // año) en vez de asumir el mes/año actual -- antes solo se pedía el
        // día (1-31) y se combinaba con el mes/año de HOY, lo que salía mal
        // si la carta se generaba a fin de mes para una cita el mes
        // siguiente. El horario de atención sí es fijo y se marca en
        // negrita+subrayado igual que el resto de los datos llenados.
        Paragraph plazo = new Paragraph();
        plazo.add(new Chunk("Debe presentarse el día ", fontNormal));
        if (carta.fechaPresentacion() != null) {
            plazo.add(new Chunk(String.valueOf(carta.fechaPresentacion().getDayOfMonth()), fontDatoLlenado));
            plazo.add(new Chunk(" de ", fontNormal));
            plazo.add(new Chunk(mesDe(carta.fechaPresentacion()), fontDatoLlenado));
            plazo.add(new Chunk(" de 20", fontNormal));
            plazo.add(new Chunk(anioDosDigitosDe(carta.fechaPresentacion()), fontDatoLlenado));
        } else {
            plazo.add(new Chunk("______ de __________________ de 20____", fontNormal));
        }
        plazo.add(new Chunk(", en el horario de ", fontNormal));
        plazo.add(new Chunk(HORARIO_COBRO_INICIO, fontDatoLlenado));
        plazo.add(new Chunk(" a ", fontNormal));
        plazo.add(new Chunk(HORARIO_COBRO_FIN, fontDatoLlenado));
        plazo.add(new Chunk(" horas, en las oficinas del Comité, para: a) aclarar su estado de cuenta; b) liquidar su adeudo; o c) celebrar un "
                + "convenio de pago por escrito. No se trata de una fecha límite: es el día y horario en que debe presentarse.", fontNormal));
        plazo.setSpacingAfter(1.5f);
        document.add(plazo);

        // Redacción específica según el tipo de aviso -- ya no un genérico
        // "se procederá conforme al Art. 20 (siguiente aviso, o corte...)"
        // igual para los dos, que además resultaba confuso en el Segundo
        // aviso (no hay "siguiente aviso" después del Segundo, solo corte).
        if (esSegundo) {
            agregarParrafo(document, "De no presentarse en la fecha y horario señalados, procede el corte del servicio, mismo que podrá "
                    + "realizarse a partir del día hábil siguiente, sin necesidad de emitir un tercer aviso; para la reconexión, los costos "
                    + "materiales corren por separado, a cargo del usuario.");
        } else {
            agregarParrafo(document, "De no presentarse en la fecha y horario señalados, se generará el Segundo aviso conforme al Artículo 20 "
                    + "del Reglamento Interno, y de continuar sin regularizarse procede el corte del servicio.");
        }

        Paragraph tratoRespetuoso = new Paragraph("Se solicita un trato respetuoso hacia el personal del Comité (Art. 27); de haber falta de "
                + "respeto reiterada, conforme al Artículo 29 puede derivar en la suspensión definitiva del servicio.", fontBold);
        tratoRespetuoso.setSpacingAfter(1.5f);
        document.add(tratoRespetuoso);

        Paragraph avisoPago = new Paragraph("Si ya realizó su pago, puede hacer caso omiso de esta notificación (no le aplicaría la multa); solo "
                + "envíe la foto de su último recibo de pago al 477 190 4765 para actualizar su registro.", fontRojoItalica);
        avisoPago.setSpacingAfter(1.7f);
        document.add(avisoPago);

        PdfPTable razonTitulo = new PdfPTable(1);
        razonTitulo.setWidthPercentage(100);
        PdfPCell celdaTitulo = new PdfPCell(new Phrase("RAZÓN DE NOTIFICACIÓN (llenar en el momento de la entrega)", fontBold));
        celdaTitulo.setPadding(2f);
        celdaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        razonTitulo.addCell(celdaTitulo);
        razonTitulo.setSpacingAfter(1.8f);
        document.add(razonTitulo);

        // Aquí sí se deja pendiente TODO lo relacionado a la visita física
        // (día y hora de la notificación) -- eso no se puede saber de
        // antemano, solo mes/año que sí se conocen al momento de imprimir.
        Paragraph razon = new Paragraph();
        razon.add(new Chunk("El día ______ de ", fontNormal));
        razon.add(new Chunk(mesActual(), fontDatoLlenado));
        razon.add(new Chunk(" de 20", fontNormal));
        razon.add(new Chunk(anioActualDosDigitos(), fontDatoLlenado));
        razon.add(new Chunk(", siendo las ______ horas, me constituí en el domicilio arriba señalado y (marcar lo que corresponda):", fontNormal));
        razon.setSpacingAfter(1.8f);
        document.add(razon);

        agregarParrafoChico(document, "(   ) Entregué el documento a la persona titular de la toma, quien firmó de recibido.");
        agregarParrafoChico(document, "(   ) Entregué el documento a: ______________________________________, quien dijo ser "
                + "__________________ del titular y firmó de recibido.");
        agregarParrafoChico(document, "(   ) La persona que atendió se negó a recibir o a firmar; se fijó copia en lugar visible del domicilio, "
                + "ante los testigos que firman al calce, con constancia fotográfica.");
        agregarParrafoChico(document, "(   ) No se encontró a persona alguna; se fijó copia en lugar visible del domicilio, ante los testigos que "
                + "firman al calce, con constancia fotográfica.");

        // setKeepTogether: si por algún motivo (nombre/domicilio muy largo,
        // desglose de varios años) el contenido de arriba no deja suficiente
        // espacio, la tabla completa de firmas se mueve entera a la
        // siguiente hoja en vez de partirse a la mitad (que era el problema
        // reportado -- Testigo 1/2 quedaban solos al inicio de la página 2).
        PdfPTable firmas = new PdfPTable(2);
        firmas.setWidthPercentage(100);
        firmas.setSpacingBefore(3f);
        firmas.setKeepTogether(true);
        firmas.addCell(celdaFirma("Notificador(a) por el Comité", "Nombre, cargo y firma"));
        firmas.addCell(celdaFirma("Recibí original de la notificación", "Nombre y firma"));
        firmas.addCell(celdaFirma("Testigo 1", "Nombre y firma"));
        firmas.addCell(celdaFirma("Testigo 2", "Nombre y firma"));
        document.add(firmas);

        Paragraph piePagina = new Paragraph("Este documento se levanta en dos tantos: uno para el usuario y otro, firmado de recibido, "
                + "para el archivo del Comité.", fontItalicChico);
        piePagina.setSpacingBefore(2f);
        piePagina.setAlignment(Element.ALIGN_CENTER);
        document.add(piePagina);
    }

    private String mesActual() {
        return MESES[LocalDate.now().getMonthValue() - 1];
    }

    private String anioActualDosDigitos() {
        int anio = LocalDate.now().getYear();
        return String.valueOf(anio % 100);
    }

    // Igual que mesActual()/anioActualDosDigitos() pero para una fecha
    // elegida (ej. carta.fechaPresentacion()) en vez de HOY -- para no
    // asumir que la fecha en que debe presentarse cae en el mes/año en que
    // se generó/imprimió la carta.
    private String mesDe(LocalDate fecha) {
        return MESES[fecha.getMonthValue() - 1];
    }

    private String anioDosDigitosDe(LocalDate fecha) {
        return String.valueOf(fecha.getYear() % 100);
    }

    private PdfPCell celdaFirma(String linea1, String linea2) {
        Paragraph contenido = new Paragraph();
        contenido.add(new Chunk("____________________________\n", fontNormal));
        contenido.add(new Chunk(linea1 + "\n", fontNormal));
        contenido.add(new Chunk(linea2, fontItalicChico));
        contenido.setAlignment(Element.ALIGN_CENTER);
        PdfPCell celda = new PdfPCell();
        celda.addElement(contenido);
        celda.setBorder(0);
        celda.setPaddingTop(2.3f);
        return celda;
    }

    private void agregarFilaDatos(PdfPTable tabla, String etiqueta, String valor) {
        agregarFilaDatos(tabla, etiqueta, valor, fontNormal);
    }

    // Con fuente más chica para valores largos, sin que empujen la carta a
    // una tercera hoja.
    private void agregarFilaDatos(PdfPTable tabla, String etiqueta, String valor, Font fontValor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fontBold));
        celdaEtiqueta.setPadding(2.3f);
        celdaEtiqueta.setBackgroundColor(new Color(240, 240, 240));
        tabla.addCell(celdaEtiqueta);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fontValor));
        celdaValor.setPadding(2.3f);
        tabla.addCell(celdaValor);
    }

    // Junta 2 pares etiqueta/valor en un solo renglón de la tabla "datos"
    // (4 celdas dentro de una tabla anidada de 1 fila, ocupando colspan 2
    // en la tabla exterior) -- para campos cortos que no necesitan un
    // renglón completo cada uno (No. Casa/Domicilio, Multa/Mantenimiento,
    // Folio/Fecha del último pago). "anchos" son los 4 anchos relativos
    // (etiqueta1, valor1, etiqueta2, valor2) que deben sumar 100.
    private void agregarFilaDatosDoble(PdfPTable tabla, String etiqueta1, String valor1, String etiqueta2, String valor2, float[] anchos) {
        PdfPTable subTabla = new PdfPTable(4);
        subTabla.setWidthPercentage(100);
        subTabla.setWidths(anchos);
        agregarFilaDatos(subTabla, etiqueta1, valor1, fontItalicChico);
        agregarFilaDatos(subTabla, etiqueta2, valor2, fontItalicChico);

        PdfPCell celda = new PdfPCell(subTabla);
        celda.setColspan(2);
        celda.setPadding(0f);
        tabla.addCell(celda);
    }

    private void agregarParrafo(Document document, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, fontNormal);
        // 1.5f (antes 1.8f, antes 2.5f/3f) -- apretado de nuevo al agregar
        // el párrafo de publicación de adeudos (Art. 4 Bis, v5 de la carta)
        // para que las firmas sigan cabiendo en la hoja 1.
        p.setSpacingAfter(1.5f);
        document.add(p);
    }

    // Para los 4 renglones de "marcar lo que corresponda" -- mismo texto
    // chico, pero con aún menos espacio entre cada uno (son opciones de una
    // sola lista, no párrafos independientes).
    private void agregarParrafoChico(Document document, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, fontNormal);
        p.setSpacingAfter(0.9f);
        document.add(p);
    }

    private String nullToVacio(String valor) {
        return valor != null ? valor : "";
    }

    // Página 2 -- el texto del reglamento es siempre el mismo, sin datos
    // capturados, así que se arma una sola vez y se repite igual en cada
    // carta del lote (cada usuario se lleva su propia copia física).
    private void agregarPaginaReglamento(Document document) throws DocumentException {
        Paragraph titulo = new Paragraph("ARTÍCULOS DEL REGLAMENTO RELACIONADOS CON ESTA NOTIFICACIÓN", fontReglamentoTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(3f);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("Reglamento Interno del Comité de Agua Potable \"Los López\", a la vista de cualquier usuario en el "
                + "domicilio del Comité.", fontItalicChico);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(2f);
        document.add(subtitulo);

        agregarArticulo(document, "Publicación de adeudos", "Art. 4 Bis. Publicación de adeudos.", "Por el solo hecho de pertenecer al padrón de "
                + "usuarios, el usuario acepta que su situación de adeudo pueda darse a conocer en lugares públicos de la comunidad, en las "
                + "redes sociales del comité (incluyendo Facebook), y ante la asamblea, con el fin de fomentar el cumplimiento oportuno del "
                + "pago.");

        agregarArticulo(document, "Periodicidad de pago", "Art. 11. Periodicidad de pago.", "El usuario podrá cubrir su cuota conforme a alguna "
                + "de las siguientes periodicidades: pago anual, cuyo plazo vence al término de enero y febrero; pago semestral, cuyo plazo "
                + "vence al término de enero y de julio; o pago mensual, cuyo plazo vence al término de los primeros dos lunes de cada mes. "
                + "Vencido el plazo correspondiente sin haberse cubierto el pago, se considera atraso para los efectos del artículo de "
                + "intereses moratorios.");

        agregarArticulo(document, "Responsabilidad del titular de la toma", "Art. 15. Responsable de toma en tomas compartidas.",
                "Cuando una toma es compartida por varias familias o personas y quienes habitan el domicilio se nieguen a cubrir el pago, la "
                + "deuda se cargará al usuario responsable o dueño registrado de la toma.");

        agregarArticulo(document, null, "Art. 15 Bis. Aviso de cambio de habitantes.", "El usuario titular o dueño registrado de la toma es "
                + "responsable del pago del servicio correspondiente a todas las personas que habiten el domicilio. Cuando alguna de estas "
                + "personas deje de habitar el domicilio, el titular deberá avisar por escrito al comité para actualizar el padrón. De no "
                + "darse dicho aviso, el cobro correspondiente continuará aplicándose hasta que el aviso sea presentado.");

        agregarArticulo(document, "Revisión de la toma y el aljibe", "Art. 7 Bis. Derecho de inspección.", "Por el solo hecho de ser usuario "
                + "del servicio, el comité tiene derecho a ingresar al domicilio para revisar el estado de la toma de agua y del aljibe o "
                + "cisterna. El usuario está obligado a permitir dicho acceso cuando el comité lo solicite.");

        agregarArticulo(document, null, "Art. 25. Fugas, desperfectos y desperdicio de agua.", "Toda fuga o desperfecto en la toma domiciliaria "
                + "debe repararse de inmediato por el usuario. Si el usuario no atiende la fuga y el comité tiene que intervenir directamente, "
                + "procede el corte del servicio. Los aljibes o cisternas con flotador roto o sin flotador que generen desperdicio de agua "
                + "también serán sujetos a corte del servicio.");

        agregarArticulo(document, null, "Art. 25 Bis. Suspensión por daño en el aljibe.", "En caso de que el aljibe o cisterna presente daño, "
                + "la toma correspondiente se suspenderá hasta que el daño sea reparado por el usuario.");

        agregarArticulo(document, "Cortes del servicio", "Art. 19. Corte por falta de pago.", "La lista de corte la integran todos los "
                + "usuarios que, conforme a la periodicidad de pago señalada en el Artículo 11, hayan incumplido su pago dentro del plazo "
                + "correspondiente.");

        agregarArticulo(document, null, "Art. 20. Multa de corte y reconexión.", "Se aplicará una multa de $500.00 (quinientos pesos) por "
                + "concepto de corte y reconexión del servicio. La visita del comité a un domicilio que ya se encuentre en la lista de corte "
                + "(Artículo 19) tiene un costo de $200.00 por aviso, hasta por dos avisos. Si después del segundo aviso el usuario no se "
                + "pone al corriente, en la tercera visita procede el corte del servicio. Para la reconexión, el usuario debe ponerse al "
                + "corriente en sus pagos; los costos materiales del corte y la reconexión corren por separado, a cargo del usuario.");

        agregarArticulo(document, null, "Art. 29. Suspensión definitiva de la toma.", "Cuando un usuario no llegue a un acuerdo de pago con el "
                + "comité respecto de sus adeudos, y su caso haya sido expuesto ante la asamblea en más de una ocasión sin que el usuario "
                + "muestre disposición a respetar lo que ésta acuerde, el comité podrá determinar, como último recurso, la suspensión "
                + "definitiva del servicio de agua a su toma.");

        agregarArticulo(document, "Trato al personal del Comité", "Art. 27. Trato al personal del comité.", "El comité se reserva el derecho de "
                + "negar el cobro, ese día, a quien falte al respeto a sus integrantes, quedando a salvo la posibilidad de que el usuario se "
                + "presente a pagar el siguiente día de cobro. Estos casos se expondrán ante la asamblea.");

        agregarArticulo(document, "Multas e intereses", "Art. 22. Intereses moratorios.", "El incumplimiento de convenios o adeudos, una vez "
                + "vencido el plazo de pago conforme al Artículo 11 (periodicidad de pago), genera un interés desde el primer día de atraso. "
                + "El interés aplicable es de $3.00 (tres pesos) por día de atraso.");

        agregarArticulo(document, null, "Art. 22 Bis. Prelación en la aplicación de pagos.", "Cuando un usuario realice un pago y este no cubra "
                + "la totalidad de lo adeudado, el pago se aplicará conforme al siguiente orden: primero a los intereses moratorios (Artículo 22); "
                + "en seguida a las multas pendientes; después a las cooperaciones extraordinarias de mantenimiento (Artículo 10); y, por último, "
                + "a la aportación ordinaria de energía eléctrica (CFE).");

        agregarArticulo(document, null, "Art. 24. Manipulación de válvulas, candados o bastones.", "Retirar o manipular sin autorización un "
                + "candado, válvula o bastón se sanciona con la suspensión del servicio de uno a dos meses, o con una multa (de $500.00 a "
                + "$2,000.00 según la gravedad y reincidencia). Si una válvula resulta alterada o dañada, su reemplazo corre a cargo del "
                + "usuario responsable.");
        // Nota "(La multa de corte y reconexión se encuentra en el Art. 20...)"
        // ELIMINADA (v6 de la carta, sept. 2026, pedido de Ely) -- era
        // redundante: el Art. 20 ya está a la vista unos renglones arriba,
        // bajo su propia sección "Cortes del servicio", así que remitir a
        // él no aporta nada y solo ocupaba una línea que estaba empujando
        // el reglamento a una 3a hoja.
    }

    private void agregarArticulo(Document document, String seccion, String tituloArticulo, String texto) throws DocumentException {
        if (seccion != null) {
            Paragraph tituloSeccion = new Paragraph(seccion, fontArticuloTitulo);
            tituloSeccion.setSpacingBefore(1.5f);
            tituloSeccion.setSpacingAfter(0.3f);
            document.add(tituloSeccion);
        }
        Paragraph p = new Paragraph();
        p.add(new Chunk(tituloArticulo + " ", fontReglamentoBold));
        p.add(new Chunk(texto, fontReglamentoBody));
        // 1.9f (antes 2.2f, antes 2.5f) -- se apretó de nuevo al agregar el
        // Art. 4 Bis (nueva sección "Publicación de adeudos", v5 de la
        // carta), para que los 14 artículos sigan cabiendo en una sola hoja.
        p.setSpacingAfter(1.9f);
        document.add(p);
    }
}

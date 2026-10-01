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

// Genera el "AVISO INFORMATIVO DE ADEUDO" -- notificación PREVIA y más
// suave que las Cartas de adeudo (ver AvisoAdeudoPdfService), plantilla
// tomada directamente del docx que Ely proporcionó como referencia
// ("Aviso_Informativo_Adeudo_Comite_Los_Lopez"). Mismo estilo visual y
// mismos helpers que AvisoAdeudoPdfService (fuentes, tabla de datos,
// sección de firmas, página de reglamento), pero con su propio texto y
// tabla de "Periodicidad de pago" (Art. 11) que la carta formal no trae.
@Service
public class AvisoInformativoAdeudoPdfService {

    private static final String LOGO_CLASSPATH = "pdf/logo_comite.png";
    private static final float MARGEN_LR = 32f;
    private static final float MARGEN_TB = 14f;

    // Mismo horario de atención fijo que ya usan Cartas de adeudo/Aviso
    // Padron para "Debe presentarse el día ___ en el horario de ___ a ___".
    private static final String HORARIO_INICIO = "7:30 p.m.";
    private static final String HORARIO_FIN = "9:00 p.m.";

    private static final String[] MESES = {
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    };

    private final Font fontComite = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12.3f, new Color(0, 90, 160));
    private final Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.3f);
    private final Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 7.7f);
    private final Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.7f);
    private final Font fontItalicChico = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 6.7f);
    private final Font fontRojoNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.7f, Color.RED);
    private final Font fontRojo = FontFactory.getFont(FontFactory.HELVETICA, 7.7f, Color.RED);
    private final Font fontRojoItalica = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 7.7f, Color.RED);
    private final Font fontFolio = FontFactory.getFont(FontFactory.HELVETICA, 7.4f);
    private final Font fontArticuloTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.8f);
    private final Font fontReglamentoTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private final Font fontReglamentoBody = FontFactory.getFont(FontFactory.HELVETICA, 7f);
    private final Font fontReglamentoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f);
    private final Font fontDatoLlenado = FontFactory.getFont(FontFactory.HELVETICA, 7.7f, Font.BOLD | Font.UNDERLINE, Color.RED);
    // Encabezado de la tabla de periodicidad de pago -- blanco sobre fondo
    // oscuro, para que se note que es un título de tabla y no un dato más.
    private final Font fontTablaPeriodicidadTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.7f, Color.WHITE);

    public byte[] generarLote(List<CartaInformativoAdeudoDatos> avisos) throws IOException, DocumentException {
        Document document = new Document(PageSize.LETTER, MARGEN_LR, MARGEN_LR, MARGEN_TB, MARGEN_TB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, salida);
        document.open();

        byte[] logoBytes = leerLogo();

        boolean primera = true;
        for (CartaInformativoAdeudoDatos aviso : avisos) {
            if (!primera) {
                document.newPage();
            }
            primera = false;
            agregarPaginaAviso(document, aviso, logoBytes);
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

    private void agregarPaginaAviso(Document document, CartaInformativoAdeudoDatos aviso, byte[] logoBytes) throws DocumentException, IOException {
        Paragraph folio = new Paragraph("Folio de notificación No. " + aviso.folioNotificacion(), fontFolio);
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

        Paragraph titulo = new Paragraph("AVISO INFORMATIVO DE ADEUDO", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(2.2f);
        document.add(titulo);

        Paragraph fecha = new Paragraph();
        fecha.add(new Chunk("León, Guanajuato, a ______ de ", fontNormal));
        fecha.add(new Chunk(mesActual(), fontDatoLlenado));
        fecha.add(new Chunk(" de 20", fontNormal));
        fecha.add(new Chunk(anioActualDosDigitos(), fontDatoLlenado));
        fecha.add(new Chunk(".", fontNormal));
        fecha.setAlignment(Element.ALIGN_CENTER);
        fecha.setSpacingAfter(2.2f);
        document.add(fecha);

        // Tabla de datos -- 7 renglones simples, uno por dato, igual que el
        // docx de referencia (a diferencia de Cartas de adeudo, aquí no se
        // combinan en renglones dobles: son pocos usuarios por lote, no
        // hace falta apretar tanto el espacio).
        PdfPTable datos = new PdfPTable(2);
        datos.setWidthPercentage(100);
        datos.setWidths(new float[]{42f, 58f});
        agregarFilaDatos(datos, "Nombre del usuario titular", nullToVacio(aviso.nombreUsuarioTitular()));
        agregarFilaDatos(datos, "No. Casa", nullToVacio(aviso.noCasa()));
        agregarFilaDatos(datos, "Domicilio de la toma", nullToVacio(aviso.domicilioToma()));
        agregarFilaDatos(datos, "Desglose del adeudo por año",
                aviso.periodosAdeudados() != null && !aviso.periodosAdeudados().isBlank() ? aviso.periodosAdeudados() : "--",
                fontItalicChico);

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        // Desglose por concepto (v7, sept. 2026) -- mismo criterio y mismo
        // campo que en Cartas de adeudo, ver
        // AdeudoLuzUsuarioDto.multaAcumuladaDesglose /
        // AvisoAdeudoPdfService para el detalle de la regla.
        String textoMulta;
        if (aviso.multaAcumulada() != null && aviso.multaAcumulada() > 0) {
            textoMulta = formatoMoneda.format(aviso.multaAcumulada());
            if (aviso.multaAcumuladaDesglose() != null && !aviso.multaAcumuladaDesglose().isBlank()) {
                textoMulta += "  (" + aviso.multaAcumuladaDesglose() + ")";
            }
        } else {
            textoMulta = "Sin multas pendientes";
        }
        agregarFilaDatos(datos, "Multa acumulada a la fecha", textoMulta, fontItalicChico);

        // Cooperación extraordinaria de mantenimiento de cajón (Art. 10) --
        // renglón propio, aparte de la multa, mismo criterio que Cartas de
        // adeudo (v5, sept. 2026).
        String textoMantenimiento;
        if (aviso.mantenimientoPendiente() != null && aviso.mantenimientoPendiente() > 0) {
            String desglose = aviso.mantenimientoPorAnioTexto() != null && !aviso.mantenimientoPorAnioTexto().isBlank()
                    ? " (" + aviso.mantenimientoPorAnioTexto() + ")" : "";
            textoMantenimiento = formatoMoneda.format(aviso.mantenimientoPendiente()) + desglose;
        } else {
            textoMantenimiento = "Sin adeudo de mantenimiento";
        }
        agregarFilaDatos(datos, "Mantenimiento", textoMantenimiento);

        agregarFilaDatos(datos, "No. de recibo del último pago",
                aviso.noFolioUltimoPago() != null ? String.valueOf(aviso.noFolioUltimoPago()) : "Sin pagos registrados");
        agregarFilaDatos(datos, "Fecha del último pago",
                aviso.fechaUltimoPagoTexto() != null && !aviso.fechaUltimoPagoTexto().isBlank() ? aviso.fechaUltimoPagoTexto() : "--");

        datos.setSpacingAfter(2f);
        document.add(datos);

        Paragraph nota1 = new Paragraph("El adeudo, la multa y el mantenimiento indicados corresponden a la aportación de luz (CFE), a los "
                + "avisos o corte ya generados, y a las cooperaciones extraordinarias de mantenimiento de cajón (Artículo 10) que ya se "
                + "encuentren registradas. Los datos pueden contener errores de captura; el usuario o el Comité pueden validarlos con el "
                + "expediente físico en las oficinas del Comité.", fontItalicChico);
        nota1.setSpacingAfter(1.1f);
        document.add(nota1);

        Paragraph nota3 = new Paragraph("Conforme al Artículo 22 Bis del Reglamento Interno, cuando un pago no cubra la totalidad de lo "
                + "adeudado, se aplica primero a intereses moratorios, después a multas pendientes, en seguida a cooperaciones extraordinarias "
                + "y, por último, a la aportación de luz (CFE).", fontItalicChico);
        nota3.setSpacingAfter(1.5f);
        document.add(nota3);

        // Día y horario de presentación -- para que quede folio con fecha y
        // el Comité le pueda dar seguimiento (pedido explícito de Ely). Se
        // usa la fecha completa elegida al generar (día+mes+año), mismo
        // criterio que Cartas de adeudo / Aviso Padron.
        Paragraph plazo = new Paragraph();
        plazo.add(new Chunk("Se le invita a presentarse el día ", fontBold));
        if (aviso.fechaPresentacion() != null) {
            plazo.add(new Chunk(String.valueOf(aviso.fechaPresentacion().getDayOfMonth()), fontDatoLlenado));
            plazo.add(new Chunk(" de ", fontBold));
            plazo.add(new Chunk(mesDe(aviso.fechaPresentacion()), fontDatoLlenado));
            plazo.add(new Chunk(" de 20", fontBold));
            plazo.add(new Chunk(anioDosDigitosDe(aviso.fechaPresentacion()), fontDatoLlenado));
        } else {
            plazo.add(new Chunk("______ de __________________ de 20____", fontBold));
        }
        plazo.add(new Chunk(", en el horario de ", fontBold));
        plazo.add(new Chunk(HORARIO_INICIO, fontDatoLlenado));
        plazo.add(new Chunk(" a ", fontBold));
        plazo.add(new Chunk(HORARIO_FIN, fontDatoLlenado));
        plazo.add(new Chunk(", en las oficinas del Comité, para regularizar su situación. No se trata de una fecha límite: es el día y "
                + "horario en que se le invita a acudir.", fontBold));
        plazo.setSpacingAfter(1.8f);
        document.add(plazo);

        // Nota libre del Comité al elegir a este usuario (opcional) -- mismo
        // criterio que "observacionManual" en Cartas de adeudo.
        if (aviso.observacion() != null && !aviso.observacion().isBlank()) {
            Paragraph notaComite = new Paragraph("Nota del Comité: " + aviso.observacion(), fontRojoItalica);
            notaComite.setSpacingAfter(1.8f);
            document.add(notaComite);
        }

        agregarTablaPeriodicidadPago(document);

        agregarParrafo(document, "Este aviso también aplica si en su domicilio viven otras familias que no se han puesto al corriente con su "
                + "pago. El titular o dueño registrado de la toma es responsable del pago de todas las personas que habiten el domicilio; si "
                + "alguna deja de habitarlo, debe avisar por escrito al Comité para actualizar el padrón, pues de no hacerlo el cobro "
                + "continuará aplicándose hasta que se presente dicho aviso.");

        Paragraph multa = new Paragraph("La multa por falta de pago aplica cuando se tienen al menos 3 meses sin realizar el pago.", fontRojoNegrita);
        multa.setSpacingAfter(1.5f);
        document.add(multa);

        agregarParrafo(document, "Le solicitamos ponerse al corriente con su adeudo a la brevedad posible, ya sea en las oficinas del Comité, "
                + "en el horario habitual de cobro (lunes, de 7:30 p.m. a 9:00 p.m.), o mediante un convenio de pago por escrito.");

        agregarParrafo(document, "De no regularizar su situación, su toma podrá integrarse a la lista de corte (Artículo 19) y dará inicio el "
                + "proceso de avisos y suspensión que corresponda conforme al Reglamento Interno (Artículo 20).");

        Paragraph padron = new Paragraph("Por pertenecer al padrón de usuarios, su situación de adeudo podrá ser exhibida en lugares públicos "
                + "de la comunidad, en las redes sociales del Comité (incluyendo Facebook), y ante la asamblea (Artículo 4 Bis del Reglamento "
                + "Interno).", fontRojo);
        padron.setSpacingAfter(1.5f);
        document.add(padron);

        Paragraph tratoRespetuoso = new Paragraph("Se solicita un trato respetuoso hacia el personal del Comité (Art. 27); de haber falta de "
                + "respeto reiterada, conforme al Artículo 29 puede derivar en la suspensión definitiva del servicio.", fontBold);
        tratoRespetuoso.setSpacingAfter(1.5f);
        document.add(tratoRespetuoso);

        Paragraph avisoPago = new Paragraph("Si ya realizó su pago, puede hacer caso omiso de esta notificación; solo envíe la foto de su "
                + "último recibo de pago al 477 190 4765 para actualizar su registro.", fontRojoItalica);
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
        agregarParrafoChico(document, "(   ) No se encontró a persona alguna; se fijó copia en lugar visible del domicilio, ante los testigos "
                + "que firman al calce, con constancia fotográfica.");

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

    // Tabla "PERIODICIDAD DE PAGO E INICIO DE INTERESES (Artículo 11)" --
    // exclusiva de este aviso informativo, no existe en Cartas de adeudo.
    // Texto fijo (no depende del usuario), tomado tal cual del docx de Ely.
    private void agregarTablaPeriodicidadPago(Document document) {
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{20f, 45f, 35f});
        tabla.setSpacingBefore(1f);
        tabla.setSpacingAfter(2.5f);

        PdfPCell tituloCelda = new PdfPCell(new Phrase("PERIODICIDAD DE PAGO E INICIO DE INTERESES (Artículo 11)", fontTablaPeriodicidadTitulo));
        tituloCelda.setColspan(3);
        tituloCelda.setBackgroundColor(new Color(0, 90, 160));
        tituloCelda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tituloCelda.setPadding(2.5f);
        tabla.addCell(tituloCelda);

        agregarFilaPeriodicidad(tabla, "Anual", "Tiene enero y febrero para pagar", "Interés desde marzo");
        agregarFilaPeriodicidad(tabla, "Semestral", "Tiene enero y julio para pagar", "Interés desde febrero y agosto, respectivamente");
        agregarFilaPeriodicidad(tabla, "Mensual", "Tiene los primeros 10 días del mes para pagar", "Interés desde el día 11");

        document.add(tabla);
    }

    private void agregarFilaPeriodicidad(PdfPTable tabla, String periodo, String plazo, String interes) {
        PdfPCell celdaPeriodo = new PdfPCell(new Phrase(periodo, fontBold));
        celdaPeriodo.setPadding(2.3f);
        celdaPeriodo.setBackgroundColor(new Color(240, 240, 240));
        tabla.addCell(celdaPeriodo);

        PdfPCell celdaPlazo = new PdfPCell(new Phrase(plazo, fontItalicChico));
        celdaPlazo.setPadding(2.3f);
        tabla.addCell(celdaPlazo);

        PdfPCell celdaInteres = new PdfPCell(new Phrase(interes, fontItalicChico));
        celdaInteres.setPadding(2.3f);
        tabla.addCell(celdaInteres);
    }

    private String mesActual() {
        return MESES[LocalDate.now().getMonthValue() - 1];
    }

    private String anioActualDosDigitos() {
        int anio = LocalDate.now().getYear();
        return String.valueOf(anio % 100);
    }

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

    private void agregarFilaDatos(PdfPTable tabla, String etiqueta, String valor, Font fontValor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fontBold));
        celdaEtiqueta.setPadding(2.3f);
        celdaEtiqueta.setBackgroundColor(new Color(240, 240, 240));
        tabla.addCell(celdaEtiqueta);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fontValor));
        celdaValor.setPadding(2.3f);
        tabla.addCell(celdaValor);
    }

    private void agregarParrafo(Document document, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, fontNormal);
        p.setSpacingAfter(1.5f);
        document.add(p);
    }

    private void agregarParrafoChico(Document document, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, fontNormal);
        p.setSpacingAfter(0.9f);
        document.add(p);
    }

    private String nullToVacio(String valor) {
        return valor != null ? valor : "";
    }

    // Página 2 -- mismo criterio que AvisoAdeudoPdfService.agregarPaginaReglamento(),
    // pero solo con el subconjunto de artículos que trae el docx de
    // referencia de Ely (Art. 4 Bis, 11, 15, 15 Bis, 19, 20, 29, 27, 22, 22
    // Bis -- en ese orden), SIN los artículos de inspección/fugas/aljibe
    // (7 Bis, 24, 25, 25 Bis) que solo aplican a la carta formal de
    // suspensión, no a este aviso puramente informativo.
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
        p.setSpacingAfter(1.9f);
        document.add(p);
    }
}

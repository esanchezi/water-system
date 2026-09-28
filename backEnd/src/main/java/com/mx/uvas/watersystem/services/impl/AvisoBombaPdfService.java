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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// PDF de "Aviso por uso indebido de bomba" (Art. 23 del Reglamento Interno)
// -- mismo estilo visual y misma sección "RAZÓN DE NOTIFICACIÓN" (firmas)
// que AvisoAdeudoPdfService/RenunciaTemporalPdfService, pero con su propio
// contenido: no hay cálculo de deuda ni Primero/Segundo, es un solo aviso
// por reporte de uso indebido (Art. 23: "el comité notificará al usuario
// mediante un solo aviso").
@Service
public class AvisoBombaPdfService {

    private static final String LOGO_CLASSPATH = "pdf/logo_comite.png";
    private static final float MARGEN_LR = 32f;
    private static final float MARGEN_TB = 14f;

    private static final String[] MESES = {
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    };
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Font fontComite = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(0, 90, 160));
    private final Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private final Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 9f);
    private final Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f);
    private final Font fontItalicChico = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7.8f);
    private final Font fontFolio = FontFactory.getFont(FontFactory.HELVETICA, 7.8f);
    private final Font fontArticuloTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f);
    private final Font fontReglamentoTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private final Font fontReglamentoBody = FontFactory.getFont(FontFactory.HELVETICA, 8.3f);
    private final Font fontReglamentoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.3f);
    // Para mes/año llenados en automático dentro del texto -- en rojo,
    // negrita y subrayado, igual que un campo llenado a mano.
    private final Font fontDatoLlenado = FontFactory.getFont(FontFactory.HELVETICA, 9f, Font.BOLD | Font.UNDERLINE, Color.RED);

    public byte[] generarLote(List<CartaBombaDatos> cartas) throws IOException, DocumentException {
        Document document = new Document(PageSize.LETTER, MARGEN_LR, MARGEN_LR, MARGEN_TB, MARGEN_TB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, salida);
        document.open();

        byte[] logoBytes = leerLogo();

        boolean primera = true;
        for (CartaBombaDatos carta : cartas) {
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

    private void agregarPaginaAviso(Document document, CartaBombaDatos carta, byte[] logoBytes) throws DocumentException, IOException {
        Paragraph folio = new Paragraph("Folio de notificación No. " + carta.folioNotificacion(), fontFolio);
        folio.setAlignment(Element.ALIGN_LEFT);
        folio.setSpacingAfter(2f);
        document.add(folio);

        if (logoBytes != null) {
            Image logo = Image.getInstance(logoBytes);
            logo.scaleToFit(34, 34);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
        }

        Paragraph comite = new Paragraph("COMITÉ DE AGUA POTABLE \"LOS LÓPEZ\"", fontComite);
        comite.setAlignment(Element.ALIGN_CENTER);
        comite.setSpacingBefore(1f);
        comite.setSpacingAfter(3f);
        document.add(comite);

        Paragraph titulo = new Paragraph("AVISO POR USO INDEBIDO DE BOMBA", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(3f);
        document.add(titulo);

        Paragraph fecha = new Paragraph();
        fecha.add(new Chunk("León, Guanajuato, a ______ de ", fontNormal));
        fecha.add(new Chunk(mesActual(), fontDatoLlenado));
        fecha.add(new Chunk(" de 20", fontNormal));
        fecha.add(new Chunk(anioActualDosDigitos(), fontDatoLlenado));
        fecha.add(new Chunk(".", fontNormal));
        fecha.setAlignment(Element.ALIGN_CENTER);
        fecha.setSpacingAfter(3f);
        document.add(fecha);

        PdfPTable datos = new PdfPTable(2);
        datos.setWidthPercentage(100);
        datos.setWidths(new float[]{42f, 58f});
        agregarFilaDatos(datos, "Nombre del usuario titular", nullToVacio(carta.nombreUsuarioTitular()));
        agregarFilaDatos(datos, "No. Casa", carta.noCasa() != null && !carta.noCasa().isBlank() ? carta.noCasa() : "");
        agregarFilaDatos(datos, "Domicilio de la toma", nullToVacio(carta.domicilioToma()));
        agregarFilaDatos(datos, "Fecha del reporte / revisión",
                carta.fechaReporte() != null ? carta.fechaReporte().format(FORMATO_FECHA) : "");
        datos.setSpacingAfter(3f);
        document.add(datos);

        agregarParrafoChicoItalico(document, "Este aviso se emite conforme al Artículo 23 del Reglamento Interno (ver artículos completos al "
                + "reverso), por haberse recibido un reporte de uso de bomba para extraer agua de la red fuera de lo autorizado por el Comité.");

        Paragraph multa = new Paragraph("Conforme al Artículo 23 del Reglamento Interno, de comprobarse el uso indebido de la bomba, se aplicará "
                + "una multa de $2,000.00 (dos mil pesos) y la instalación de un bastón de restricción en la toma. En caso de reincidencia, "
                + "procederá el corte del servicio y la suspensión de la toma por el tiempo que determine el Comité.", fontBold);
        multa.setSpacingAfter(3f);
        document.add(multa);

        agregarParrafo(document, "Por el solo hecho de ser usuario del servicio, el Comité tiene derecho a ingresar al domicilio para revisar "
                + "el estado de la toma, conforme al Artículo 7 Bis del Reglamento Interno.");

        agregarParrafo(document, "Si considera que este reporte no corresponde a su situación, puede presentarse en las oficinas del Comité, "
                + "en el horario de atención, para aclararlo antes de que se realice la revisión de comprobación.");

        Paragraph suspension = new Paragraph("El usuario declara estar consciente de que, conforme al Artículo 29 del Reglamento Interno, toda "
                + "falta grave o el no respetar el reglamento -- incluido el uso indebido de bomba -- lo hace acreedor(a) a la suspensión "
                + "definitiva del servicio de agua a su toma.", fontBold);
        suspension.setSpacingAfter(3f);
        document.add(suspension);

        PdfPTable razonTitulo = new PdfPTable(1);
        razonTitulo.setWidthPercentage(100);
        PdfPCell celdaTitulo = new PdfPCell(new Phrase("RAZÓN DE NOTIFICACIÓN (llenar en el momento de la entrega)", fontBold));
        celdaTitulo.setPadding(3f);
        celdaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        razonTitulo.addCell(celdaTitulo);
        razonTitulo.setSpacingAfter(3f);
        document.add(razonTitulo);

        Paragraph razon = new Paragraph();
        razon.add(new Chunk("El día ______ de ", fontNormal));
        razon.add(new Chunk(mesActual(), fontDatoLlenado));
        razon.add(new Chunk(" de 20", fontNormal));
        razon.add(new Chunk(anioActualDosDigitos(), fontDatoLlenado));
        razon.add(new Chunk(", siendo las ______ horas, me constituí en el domicilio arriba señalado y (marcar lo que corresponda):", fontNormal));
        razon.setSpacingAfter(3f);
        document.add(razon);

        agregarParrafoChico(document, "(   ) Entregué el documento a la persona titular de la toma, quien firmó de recibido.");
        agregarParrafoChico(document, "(   ) Entregué el documento a: ______________________________________, quien dijo ser "
                + "__________________ del titular y firmó de recibido.");
        agregarParrafoChico(document, "(   ) La persona que atendió se negó a recibir o a firmar; se fijó copia en lugar visible del domicilio, "
                + "ante los testigos que firman al calce, con constancia fotográfica.");
        agregarParrafoChico(document, "(   ) No se encontró a persona alguna; se fijó copia en lugar visible del domicilio, ante los testigos que "
                + "firman al calce, con constancia fotográfica.");

        PdfPTable firmas = new PdfPTable(2);
        firmas.setWidthPercentage(100);
        firmas.setSpacingBefore(4f);
        firmas.setKeepTogether(true);
        firmas.addCell(celdaFirma("Notificador(a) por el Comité", "Nombre, cargo y firma"));
        firmas.addCell(celdaFirma("Recibí original de la notificación", "Nombre y firma"));
        firmas.addCell(celdaFirma("Testigo 1", "Nombre y firma"));
        firmas.addCell(celdaFirma("Testigo 2", "Nombre y firma"));
        document.add(firmas);

        Paragraph piePagina = new Paragraph("Este documento se levanta en dos tantos: uno para el usuario y otro, firmado de recibido, "
                + "para el archivo del Comité.", fontItalicChico);
        piePagina.setSpacingBefore(3f);
        piePagina.setAlignment(Element.ALIGN_CENTER);
        document.add(piePagina);
    }

    private void agregarPaginaReglamento(Document document) throws DocumentException {
        Paragraph titulo = new Paragraph("ARTÍCULOS DEL REGLAMENTO RELACIONADOS CON ESTA NOTIFICACIÓN", fontReglamentoTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(4f);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("Reglamento Interno del Comité de Agua Potable \"Los López\", a la vista de cualquier usuario en el "
                + "domicilio del Comité.", fontItalicChico);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(3f);
        document.add(subtitulo);

        agregarArticulo(document, "Derecho de inspección", "Art. 7 Bis. Derecho de inspección.", "Por el solo hecho de ser usuario del "
                + "servicio, el comité tiene derecho a ingresar al domicilio para revisar el estado de la toma de agua y del aljibe o cisterna. "
                + "El usuario está obligado a permitir dicho acceso cuando el comité lo solicite.");

        agregarArticulo(document, "Uso indebido de bombas", "Art. 23. Uso indebido de bombas.", "Al recibirse un reporte de uso de bomba para "
                + "extraer agua de la red fuera de lo autorizado, el comité notificará al usuario mediante un solo aviso. De comprobarse el uso "
                + "indebido, se aplicará una multa de $2,000.00 (dos mil pesos) y la instalación de un bastón de restricción. En caso de "
                + "reincidencia, procederá el corte del servicio y la suspensión de la toma por el tiempo que determine el comité.");

        agregarArticulo(document, null, "Art. 17. Identificación física de restricción.", "Cuando sea necesario -- por incumplimiento de un "
                + "convenio, manipulación no autorizada de la toma o uso indebido de bomba -- se instalará al usuario una llave de color azul "
                + "y/o un bastón de identificación/restricción en su toma. El costo de instalación corre a cargo del usuario.");

        agregarArticulo(document, "Manipulación de restricciones", "Art. 24. Manipulación de válvulas, candados o bastones.", "Retirar o "
                + "manipular sin autorización un candado, válvula o bastón se sanciona con la suspensión del servicio de uno a dos meses, o con "
                + "una multa (de $500.00 a $2,000.00 según la gravedad y reincidencia). Si una válvula resulta alterada o dañada, su reemplazo "
                + "corre a cargo del usuario responsable.");

        agregarArticulo(document, "Reincidencia y suspensión definitiva", "Art. 20. Multa de corte y reconexión.", "Se aplicará una multa de "
                + "$500.00 (quinientos pesos) por concepto de corte y reconexión del servicio. Los costos materiales del corte y la reconexión "
                + "corren por separado, a cargo del usuario.");

        agregarArticulo(document, null, "Art. 29. Suspensión definitiva de la toma.", "Cuando un usuario no llegue a un acuerdo de pago con el "
                + "comité respecto de sus adeudos, o incurra en una falta grave o en el incumplimiento reiterado de este reglamento, y su caso "
                + "haya sido expuesto ante la asamblea en más de una ocasión sin que el usuario muestre disposición a respetar lo que ésta "
                + "acuerde, el comité podrá determinar, como último recurso, la suspensión definitiva del servicio de agua a su toma.");

        agregarArticulo(document, null, "Art. 30. Acta de notificación y suspensión.", "Previo a la suspensión del servicio, el comité "
                + "levantará un acta de notificación, la cual se entregará personalmente al usuario titular de la toma. Si el usuario se niega "
                + "a recibirla, o no se encuentra a nadie en el domicilio, se fijará copia del acta en un lugar visible del domicilio ante "
                + "testigos, y se tendrá al usuario por debidamente notificado.");
    }

    private void agregarArticulo(Document document, String seccion, String tituloArticulo, String texto) throws DocumentException {
        if (seccion != null) {
            Paragraph tituloSeccion = new Paragraph(seccion, fontArticuloTitulo);
            tituloSeccion.setSpacingBefore(2f);
            tituloSeccion.setSpacingAfter(0.5f);
            document.add(tituloSeccion);
        }
        Paragraph p = new Paragraph();
        p.add(new Chunk(tituloArticulo + " ", fontReglamentoBold));
        p.add(new Chunk(texto, fontReglamentoBody));
        p.setSpacingAfter(3f);
        document.add(p);
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
        celda.setPaddingTop(3f);
        return celda;
    }

    private void agregarFilaDatos(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fontBold));
        celdaEtiqueta.setPadding(3f);
        celdaEtiqueta.setBackgroundColor(new Color(240, 240, 240));
        tabla.addCell(celdaEtiqueta);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fontNormal));
        celdaValor.setPadding(3f);
        tabla.addCell(celdaValor);
    }

    private void agregarParrafo(Document document, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, fontNormal);
        p.setSpacingAfter(3f);
        document.add(p);
    }

    private void agregarParrafoChicoItalico(Document document, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, fontItalicChico);
        p.setSpacingAfter(3f);
        document.add(p);
    }

    // Para los 4 renglones de "marcar lo que corresponda".
    private void agregarParrafoChico(Document document, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, fontNormal);
        p.setSpacingAfter(1.2f);
        document.add(p);
    }

    private String nullToVacio(String valor) {
        return valor != null ? valor : "";
    }

    private String mesActual() {
        return MESES[LocalDate.now().getMonthValue() - 1];
    }

    private String anioActualDosDigitos() {
        int anio = LocalDate.now().getYear();
        return String.valueOf(anio % 100);
    }
}

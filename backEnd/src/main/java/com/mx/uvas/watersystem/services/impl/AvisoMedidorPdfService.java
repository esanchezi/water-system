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

// PDF de "Aviso de Instalación de Medidor" (prueba de consumo, Art. 7 del
// Reglamento Interno) -- mismo estilo visual y misma sección "RAZÓN DE
// NOTIFICACIÓN" (firmas) que el resto de las cartas del Comité, pero con su
// propio contenido: no hay cálculo de deuda ni Primero/Segundo, es un solo
// aviso por instalación de medidor de prueba. Formato tomado del documento
// Word de referencia (Aviso_Instalacion_Medidor_Comite_Los_Lopez_v2.docx).
@Service
public class AvisoMedidorPdfService {

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
    private final Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLDITALIC);
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

    public byte[] generarLote(List<CartaMedidorDatos> cartas) throws IOException, DocumentException {
        Document document = new Document(PageSize.LETTER, MARGEN_LR, MARGEN_LR, MARGEN_TB, MARGEN_TB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, salida);
        document.open();

        byte[] logoBytes = leerLogo();

        boolean primera = true;
        for (CartaMedidorDatos carta : cartas) {
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

    private void agregarPaginaAviso(Document document, CartaMedidorDatos carta, byte[] logoBytes) throws DocumentException, IOException {
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

        Paragraph titulo = new Paragraph("AVISO DE INSTALACIÓN DE MEDIDOR", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(1f);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("(Prueba de consumo)", fontSubtitulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(3f);
        document.add(subtitulo);

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
        agregarFilaDatos(datos, "Fecha de instalación",
                carta.fechaInstalacion() != null ? carta.fechaInstalacion().format(FORMATO_FECHA) : "");
        agregarFilaDatos(datos, "No. de serie del medidor", nullToVacio(carta.noSerieMedidor()));
        agregarFilaDatos(datos, "Lectura inicial del medidor", nullToVacio(carta.lecturaInicialMedidor()));
        datos.setSpacingAfter(3f);
        document.add(datos);

        agregarParrafoChicoItalico(document, "Este aviso se emite conforme al Artículo 7 del Reglamento Interno (ver artículo completo al "
                + "reverso), que faculta al Comité a instalar un medidor, primero en una vivienda de prueba, con el fin de estimar el consumo "
                + "aproximado antes de aplicar esta medida de forma general. Su domicilio ha sido seleccionado por el Comité para esta prueba de "
                + "consumo; esta selección puede realizarse en cualquier domicilio y no implica inconformidad, reporte ni sanción alguna.");

        agregarParrafo(document, "El objetivo de esta prueba es conocer el consumo aproximado de agua en su domicilio, para contar con "
                + "información que permita al Comité, en su momento, evaluar si esta medida se aplica de manera general a toda la comunidad.");

        agregarParrafo(document, "A partir de la instalación, el usuario deberá permitir el acceso del Comité para la lectura y revisión "
                + "periódica del medidor, conforme al Artículo 7 Bis del Reglamento Interno (derecho de inspección).");

        Paragraph cuidado = new Paragraph("El usuario deberá cuidar el buen estado del medidor instalado por el Comité. Cualquier manipulación, "
                + "retiro o daño no autorizado será expuesto ante la asamblea, para que ésta determine la sanción que corresponda.", fontBold);
        cuidado.setSpacingAfter(3f);
        document.add(cuidado);

        Paragraph trato = new Paragraph("Se solicita un trato respetuoso hacia el personal del Comité (Art. 27); de haber falta de respeto "
                + "reiterada, conforme al Artículo 29 puede derivar en la suspensión definitiva del servicio.", fontBold);
        trato.setSpacingAfter(3f);
        document.add(trato);

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
        Paragraph titulo = new Paragraph("ARTÍCULOS DEL REGLAMENTO RELACIONADOS CON ESTE AVISO", fontReglamentoTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(4f);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("Reglamento Interno del Comité de Agua Potable \"Los López\", a la vista de cualquier usuario en el "
                + "domicilio del Comité.", fontItalicChico);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(3f);
        document.add(subtitulo);

        agregarArticulo(document, "Instalación de medidor (prueba de consumo)", "Art. 7. Instalación de medidor.", "El comité podrá instalar "
                + "medidor, a costa del usuario, en domicilios donde exista inconformidad con la cuota, se niegue el número real de familias que "
                + "habitan el predio, o se detecte uso no doméstico (negocios, riego, ganado, albercas, salones). El medidor podrá instalarse "
                + "primero en una vivienda de prueba para estimar el consumo aproximado antes de generalizar la medida.");

        agregarArticulo(document, "Derecho de inspección", "Art. 7 Bis. Derecho de inspección.", "Por el solo hecho de ser usuario del "
                + "servicio, el comité tiene derecho a ingresar al domicilio para revisar el estado de la toma de agua y del aljibe o cisterna. "
                + "El usuario está obligado a permitir dicho acceso cuando el comité lo solicite.");

        agregarArticulo(document, "Trato al personal del Comité", "Art. 27. Trato al personal del comité.", "El comité se reserva el derecho de "
                + "negar el cobro, ese día, a quien falte al respeto a sus integrantes, quedando a salvo la posibilidad de que el usuario se "
                + "presente a pagar el siguiente día de cobro. Estos casos se expondrán ante la asamblea.");

        agregarArticulo(document, null, "Art. 29. Suspensión definitiva de la toma.", "Cuando un usuario no llegue a un acuerdo de pago con el "
                + "comité respecto de sus adeudos, o incurra en una falta grave o en el incumplimiento reiterado de este reglamento, y su caso "
                + "haya sido expuesto ante la asamblea en más de una ocasión sin que el usuario muestre disposición a respetar lo que ésta "
                + "acuerde, el comité podrá determinar, como último recurso, la suspensión definitiva del servicio de agua a su toma.");
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

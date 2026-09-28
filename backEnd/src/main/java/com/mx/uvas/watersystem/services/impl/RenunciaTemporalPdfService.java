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
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// PDF de "Solicitud y acta de renuncia temporal al servicio de agua
// potable" (Art. 6 Bis del Reglamento Interno) -- mismo estilo visual que
// AvisoAdeudoPdfService (logo, encabezado, tabla de datos, reglamento al
// reverso), pero con su propio contenido y sus propios artículos (Art. 6,
// 6 Bis, 31, 32, 2 -- distintos a los de la carta de adeudo).
@Service
public class RenunciaTemporalPdfService {

    private static final String LOGO_CLASSPATH = "pdf/logo_comite.png";
    private static final float MARGEN_LR = 32f;
    private static final float MARGEN_TB = 14f;
    private static final DateTimeFormatter FORMATO_FECHA_LARGA = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "MX"));

    private final Font fontComite = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(0, 90, 160));
    private final Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private final Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8.5f);
    private final Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f);
    private final Font fontItalicChico = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7.3f);
    private final Font fontRojoNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.RED);
    private final Font fontFolio = FontFactory.getFont(FontFactory.HELVETICA, 7.8f);
    private final Font fontArticuloTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.8f);
    private final Font fontReglamentoTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private final Font fontReglamentoBody = FontFactory.getFont(FontFactory.HELVETICA, 7.6f);
    private final Font fontReglamentoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.6f);

    public byte[] generar(RenunciaTemporalDatos datos) throws IOException, DocumentException {
        Document document = new Document(PageSize.LETTER, MARGEN_LR, MARGEN_LR, MARGEN_TB, MARGEN_TB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, salida);
        document.open();

        agregarPaginaSolicitud(document, datos, leerLogo());
        document.newPage();
        agregarPaginaReglamento(document);

        document.close();
        return salida.toByteArray();
    }

    private byte[] leerLogo() throws IOException {
        try (InputStream in = new ClassPathResource(LOGO_CLASSPATH).getInputStream()) {
            return in.readAllBytes();
        }
    }

    private void agregarPaginaSolicitud(Document document, RenunciaTemporalDatos datos, byte[] logoBytes) throws DocumentException, IOException {
        Paragraph folio = new Paragraph("Folio No. " + datos.folio(), fontFolio);
        folio.setSpacingAfter(2f);
        document.add(folio);

        if (logoBytes != null) {
            Image logo = Image.getInstance(logoBytes);
            logo.scaleToFit(40, 40);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
        }

        Paragraph comite = new Paragraph("COMITÉ DE AGUA POTABLE \"LOS LÓPEZ\"", fontComite);
        comite.setAlignment(Element.ALIGN_CENTER);
        comite.setSpacingBefore(1f);
        comite.setSpacingAfter(3f);
        document.add(comite);

        Paragraph titulo = new Paragraph("SOLICITUD Y ACTA DE RENUNCIA TEMPORAL AL SERVICIO DE AGUA POTABLE", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(4f);
        document.add(titulo);

        Paragraph fecha = new Paragraph("León, Guanajuato, a " + datos.fechaRenunciaTexto() + ".", fontNormal);
        fecha.setAlignment(Element.ALIGN_CENTER);
        fecha.setSpacingAfter(4f);
        document.add(fecha);

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

        PdfPTable tablaDatos = new PdfPTable(2);
        tablaDatos.setWidthPercentage(100);
        tablaDatos.setWidths(new float[]{42f, 58f});
        agregarFilaDatos(tablaDatos, "Nombre del usuario titular", datos.nombreUsuario());
        agregarFilaDatos(tablaDatos, "No. Casa", datos.noCasa());
        agregarFilaDatos(tablaDatos, "Domicilio de la toma", datos.domicilioToma());
        agregarFilaDatos(tablaDatos, "Fecha de la renuncia", datos.fechaRenunciaTexto());
        agregarFilaDatos(tablaDatos, "Motivo de la renuncia", datos.motivo());
        agregarFilaDatos(tablaDatos, "Adeudo a la fecha de la renuncia",
                datos.adeudoALaFecha() != null && datos.adeudoALaFecha() > 0
                        ? formatoMoneda.format(datos.adeudoALaFecha())
                        : "Sin adeudo registrado");
        tablaDatos.setSpacingAfter(4f);
        document.add(tablaDatos);

        agregarParrafo(document, "El usuario abajo firmante manifiesta su voluntad de renunciar de manera temporal al servicio de agua "
                + "potable que presta el Comité de Agua Potable \"Los López\". A partir de la fecha señalada, la toma correspondiente "
                + "quedará suspendida a solicitud propia.");

        agregarParrafo(document, "Conforme al Artículo 6 Bis del Reglamento Interno (ver artículos completos al reverso), mientras dure la "
                + "renuncia no se generará cobro de cuota por este concepto. La toma permanece registrada a nombre del usuario titular y "
                + "no pierde sus derechos sobre la misma (Artículo 31).");

        Paragraph notaAdeudo = new Paragraph("La renuncia no exime al usuario de los adeudos generados antes de esta fecha, ni de las "
                + "cooperaciones extraordinarias ya aprobadas por la asamblea.", fontBold);
        notaAdeudo.setSpacingAfter(4f);
        document.add(notaAdeudo);

        Paragraph notaReconexion = new Paragraph("La reconexión del servicio deberá solicitarse por escrito al Comité. Las condiciones para "
                + "la reconexión -- incluyendo, en su caso, la cooperación económica que corresponda por trabajos o proyectos de la red -- "
                + "serán expuestas y determinadas por la asamblea (Artículo 32).", fontRojoNegrita);
        notaReconexion.setSpacingAfter(4f);
        document.add(notaReconexion);

        Paragraph notaConcesion = new Paragraph("Esta renuncia no representa una cesión, venta ni concesión de la toma; el pozo y la red "
                + "son patrimonio de la comunidad (Artículo 31).", fontItalicChico);
        notaConcesion.setSpacingAfter(6f);
        document.add(notaConcesion);

        PdfPTable firmas1 = new PdfPTable(2);
        firmas1.setWidthPercentage(100);
        firmas1.addCell(celdaFirma("Firma del usuario titular", "(solicita la renuncia)"));
        firmas1.addCell(celdaFirma("Recibió por el Comité", "Nombre, cargo y firma"));
        firmas1.setSpacingAfter(8f);
        document.add(firmas1);

        PdfPTable condicionesTitulo = new PdfPTable(1);
        condicionesTitulo.setWidthPercentage(100);
        PdfPCell celdaTitulo = new PdfPCell(new Phrase("CONDICIONES DE RECONEXIÓN (llenar cuando el usuario solicite reconectarse)", fontBold));
        celdaTitulo.setPadding(2.5f);
        celdaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        condicionesTitulo.addCell(celdaTitulo);
        condicionesTitulo.setSpacingAfter(3f);
        document.add(condicionesTitulo);

        PdfPTable tablaReconexion = new PdfPTable(2);
        tablaReconexion.setWidthPercentage(100);
        tablaReconexion.setWidths(new float[]{55f, 45f});
        agregarFilaDatos(tablaReconexion, "Fecha de solicitud de reconexión", datos.fechaSolicitudReconexionTexto());
        agregarFilaDatos(tablaReconexion, "Fecha de asamblea en que se expusieron las condiciones", datos.fechaAsambleaTexto());
        agregarFilaDatos(tablaReconexion, "Cooperación u otras condiciones determinadas", datos.condicionesReconexionTexto());
        agregarFilaDatos(tablaReconexion, "Fecha de reconexión autorizada", datos.fechaReconexionTexto());
        tablaReconexion.setSpacingAfter(8f);
        document.add(tablaReconexion);

        PdfPTable firmas2 = new PdfPTable(2);
        firmas2.setWidthPercentage(100);
        firmas2.addCell(celdaFirma("Conformidad del usuario", "(al reconectarse)"));
        firmas2.addCell(celdaFirma("Autorizó por el Comité", "Nombre, cargo y firma"));
        document.add(firmas2);
    }

    private void agregarPaginaReglamento(Document document) throws DocumentException {
        Paragraph titulo = new Paragraph("ARTÍCULOS DEL REGLAMENTO RELACIONADOS CON ESTA SOLICITUD", fontReglamentoTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(4f);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("Reglamento Interno del Comité de Agua Potable \"Los López\", a la vista de cualquier usuario en "
                + "el domicilio del Comité.", fontItalicChico);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(4f);
        document.add(subtitulo);

        agregarArticulo(document, "Tomas y obligación de pago", "Art. 6. Tomas sin conectar.", "Los usuarios que cuenten con una toma "
                + "registrada pero sin conectar deberán reportarla al comité, ya que también genera obligación de pago.");

        agregarArticulo(document, null, "Art. 6 Bis. Renuncia temporal al servicio.", "El usuario podrá solicitar por escrito al comité la "
                + "renuncia temporal al servicio de agua potable. Mientras dure la renuncia no se generará cobro de cuota por este concepto, "
                + "y la toma permanecerá registrada a nombre del usuario titular, sin que ello implique la pérdida de sus derechos sobre la "
                + "misma. La renuncia no exime al usuario de los adeudos generados antes de su fecha, ni de las cooperaciones extraordinarias "
                + "ya aprobadas. La reconexión posterior deberá solicitarse por escrito y estará sujeta a las condiciones que, en su caso, "
                + "determine la asamblea, incluyendo la cooperación que corresponda por trabajos o proyectos de la red necesarios para la "
                + "reconexión.");

        agregarArticulo(document, "Tomas nuevas, ubicación y concesiones", "Art. 31. Prohibición de concesiones.", "El pozo y la red de agua "
                + "potable son patrimonio de la comunidad. No se reconocen concesiones ni derechos de venta de tomas fuera de lo gestionado "
                + "directamente por el comité.");

        agregarArticulo(document, null, "Art. 32. Solicitudes de nueva toma o cambio de ubicación.", "Toda solicitud de nueva toma, o de "
                + "cambio de ubicación de una toma existente, será analizada por el comité y sometida a la aprobación de la asamblea. En "
                + "cambios de ubicación de una toma ya perteneciente al usuario, se cobrará únicamente el costo del trabajo y material que "
                + "implique, sin cargos adicionales.");

        agregarArticulo(document, "Autoridad de la asamblea", "Art. 2. Autoridad máxima.", "La Asamblea General de usuarios es la máxima "
                + "autoridad del sistema de agua potable. Sus acuerdos son válidos y de observancia obligatoria para todos los usuarios, "
                + "estén o no presentes.");
    }

    private void agregarArticulo(Document document, String seccion, String tituloArticulo, String texto) throws DocumentException {
        if (seccion != null) {
            Paragraph tituloSeccion = new Paragraph(seccion, fontArticuloTitulo);
            tituloSeccion.setSpacingBefore(3f);
            tituloSeccion.setSpacingAfter(1f);
            document.add(tituloSeccion);
        }
        Paragraph p = new Paragraph();
        p.add(new Chunk(tituloArticulo + " ", fontReglamentoBold));
        p.add(new Chunk(texto, fontReglamentoBody));
        p.setSpacingAfter(3.5f);
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
        celda.setPaddingTop(6f);
        return celda;
    }

    private void agregarFilaDatos(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fontBold));
        celdaEtiqueta.setPadding(3f);
        celdaEtiqueta.setBackgroundColor(new Color(240, 240, 240));
        tabla.addCell(celdaEtiqueta);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor != null ? valor : "", fontNormal));
        celdaValor.setPadding(3f);
        tabla.addCell(celdaValor);
    }

    private void agregarParrafo(Document document, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, fontNormal);
        p.setSpacingAfter(4f);
        document.add(p);
    }

    // Todo lo que necesita el PDF de una renuncia, ya calculado y
    // formateado -- mismo espíritu que CartaAdeudoDatos.
    public record RenunciaTemporalDatos(
            Integer folio,
            String nombreUsuario,
            String noCasa,
            String domicilioToma,
            String fechaRenunciaTexto,
            String motivo,
            Double adeudoALaFecha,
            String fechaSolicitudReconexionTexto,
            String fechaAsambleaTexto,
            String condicionesReconexionTexto,
            String fechaReconexionTexto
    ) {
    }
}

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
import java.util.List;

// PDF de "Aviso sobre personas responsables de pago del domicilio" (Art. 5,
// 15, 15 Bis y 15 Ter del Reglamento Interno) -- mismo estilo visual y misma
// sección "RAZÓN DE NOTIFICACIÓN" (firmas) que las demás cartas, pero con su
// propia tabla de "RESOLUCIÓN DEL COMITÉ" (hasta N personas/familias). Se
// genera una carta a la vez (no en lote como Adeudo/Bomba/Padrón), porque
// esta carta se arma llenando un formulario por Casa, no seleccionando
// varios usuarios de golpe -- ver AvisoResponsablePagoService.
@Service
public class AvisoResponsablePagoPdfService {

    private static final String LOGO_CLASSPATH = "pdf/logo_comite.png";
    private static final float MARGEN_LR = 32f;
    private static final float MARGEN_TB = 14f;

    private static final String[] MESES = {
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    };

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
    private final Font fontDatoLlenado = FontFactory.getFont(FontFactory.HELVETICA, 9f, Font.BOLD | Font.UNDERLINE, Color.RED);
    private final Font fontTablaHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f);
    private final Font fontTablaBody = FontFactory.getFont(FontFactory.HELVETICA, 8.3f);

    public byte[] generar(CartaResponsablePagoDatos carta) throws IOException, DocumentException {
        Document document = new Document(PageSize.LETTER, MARGEN_LR, MARGEN_LR, MARGEN_TB, MARGEN_TB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, salida);
        document.open();

        byte[] logoBytes = leerLogo();
        agregarPaginaAviso(document, carta, logoBytes);
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

    private void agregarPaginaAviso(Document document, CartaResponsablePagoDatos carta, byte[] logoBytes) throws DocumentException, IOException {
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

        Paragraph titulo = new Paragraph("AVISO SOBRE PERSONAS RESPONSABLES DE PAGO DEL DOMICILIO", fontTitulo);
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
        agregarFilaDatos(datos, "No. Casa", carta.noCasa() != null && !carta.noCasa().isBlank() ? carta.noCasa() : "");
        agregarFilaDatos(datos, "Domicilio de la toma", nullToVacio(carta.domicilioToma()));
        agregarFilaDatos(datos, "Motivo de la solicitud", nullToVacio(carta.motivoSolicitud()));
        agregarFilaDatos(datos, "Fecha de la solicitud",
                carta.fechaSolicitud() != null ? textoFecha(carta.fechaSolicitud()) : "");
        datos.setSpacingAfter(3f);
        document.add(datos);

        agregarParrafo(document, "El presente documento se emite en atención a una solicitud de actualización del padrón de habitantes de "
                + "su domicilio, conforme a los Artículos 5, 15, 15 Bis y 15 Ter del Reglamento Interno (ver artículos completos al reverso).");

        agregarParrafo(document, "Conforme al Artículo 5 del Reglamento Interno, el pago del servicio se realiza por familia y no por toma; "
                + "esto también aplica a los negocios y locales comerciales que operen en el domicilio.");

        agregarParrafo(document, "El Comité no puede intervenir ni tomar partido en asuntos familiares o de convivencia dentro del domicilio; "
                + "ante el Comité, el único responsable del pago es el dueño o responsable registrado de la toma (Artículo 15). Si los "
                + "habitantes del domicilio no llegan a un acuerdo entre ellos, la situación podrá exponerse ante la asamblea.");

        Paragraph tituloTabla = new Paragraph("RESOLUCIÓN DEL COMITÉ (personas y/o familias responsables de pago)", fontBold);
        tituloTabla.setSpacingBefore(2f);
        tituloTabla.setSpacingAfter(2f);
        document.add(tituloTabla);

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{7f, 38f, 30f, 25f});
        agregarCeldaHeader(tabla, "No.");
        agregarCeldaHeader(tabla, "Nombre completo");
        agregarCeldaHeader(tabla, "Relación / parentesco con el titular");
        agregarCeldaHeader(tabla, "Familia para efectos de cuota (No.)");

        // Mínimo 5 renglones como en la plantilla física, aunque vengan
        // menos personas capturadas -- los que sobren quedan en blanco
        // para llenarse a mano si hace falta.
        int filasMinimas = 5;
        List<CartaResponsablePagoDatos.CartaResponsablePagoPersona> personas = carta.personas() != null ? carta.personas() : List.of();
        int totalFilas = Math.max(filasMinimas, personas.size());
        for (int i = 0; i < totalFilas; i++) {
            if (i < personas.size()) {
                CartaResponsablePagoDatos.CartaResponsablePagoPersona p = personas.get(i);
                agregarCeldaBody(tabla, p.orden() != null ? String.valueOf(p.orden()) : String.valueOf(i + 1));
                agregarCeldaBody(tabla, nullToVacio(p.nombreCompleto()));
                agregarCeldaBody(tabla, nullToVacio(p.parentesco()));
                agregarCeldaBody(tabla, p.familiaCuota() != null ? String.valueOf(p.familiaCuota()) : "");
            } else {
                agregarCeldaBody(tabla, String.valueOf(i + 1));
                agregarCeldaBody(tabla, "");
                agregarCeldaBody(tabla, "");
                agregarCeldaBody(tabla, "");
            }
        }
        tabla.setSpacingAfter(4f);
        document.add(tabla);

        Paragraph tituloObs = new Paragraph("Observaciones adicionales del Comité:", fontBold);
        tituloObs.setSpacingAfter(1.5f);
        document.add(tituloObs);

        if (carta.observacionesComite() != null && !carta.observacionesComite().isBlank()) {
            Paragraph obs = new Paragraph(carta.observacionesComite(), fontNormal);
            obs.setSpacingAfter(2f);
            document.add(obs);
        } else {
            agregarParrafo(document, "_______________________________________________________________________");
            agregarParrafo(document, "_______________________________________________________________________");
        }

        agregarParrafo(document, "En caso de no estar de acuerdo con lo aquí determinado, el usuario puede acercarse a las oficinas del "
                + "Comité para revisar su situación.");

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
        razon.add(new Chunk(anioActualDosDigitos(), fontNormal));
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

        agregarArticulo(document, "Pago por familia", "Art. 5. Pago por familia y obligatoriedad de pago.", "El pago del servicio se realiza "
                + "por familia, incluyendo los casos de viudez o de madre o padre soltero(a); el comité evaluará y determinará, caso por caso, "
                + "si un domicilio califica bajo esta condición. Cuando en un domicilio habite una sola persona, ésta se agrupa para efectos de "
                + "pago con la familia con la que reside. Están obligados al pago todos los usuarios sin excepción, incluidos los negocios, "
                + "locales comerciales y personas que vendan alimentos u otros productos en la calle; el comité podrá definir, en su caso, un "
                + "nivel de cuota diferenciado para negocios según su consumo.");

        agregarArticulo(document, null, "Art. 7. Instalación de medidor.", "El comité podrá instalar medidor, a costa del usuario, en "
                + "domicilios donde exista inconformidad con la cuota, se niegue el número real de familias que habitan el predio, o se detecte "
                + "uso no doméstico (negocios, riego, ganado, albercas, salones). El medidor podrá instalarse primero en una vivienda de prueba "
                + "para estimar el consumo aproximado antes de generalizar la medida.");

        agregarArticulo(document, "Responsabilidad del titular de la toma", "Art. 15. Responsable de toma en tomas compartidas.", "Cuando una "
                + "toma es compartida por varias familias o personas y quienes habitan el domicilio se nieguen a cubrir el pago, la deuda se "
                + "cargará al usuario responsable o dueño registrado de la toma.");

        agregarArticulo(document, null, "Art. 15 Bis. Aviso de cambio de habitantes.", "El usuario titular o dueño registrado de la toma es "
                + "responsable del pago del servicio correspondiente a todas las personas que habiten el domicilio. Cuando alguna de estas "
                + "personas deje de habitar el domicilio, el titular deberá avisar por escrito al comité para actualizar el padrón. De no darse "
                + "dicho aviso, el cobro correspondiente continuará aplicándose hasta que el aviso sea presentado.");

        agregarArticulo(document, null, "Art. 15 Ter. Reporte del padrón de habitantes y locales.", "El usuario titular de la toma deberá "
                + "reportar por escrito al comité las personas y, en su caso, los negocios o locales comerciales que habiten o hagan uso del "
                + "domicilio, conforme al Artículo 5. El comité podrá solicitar esta información en cualquier momento. Si el usuario se niega a "
                + "proporcionarla, o proporciona información falsa, el caso se expondrá ante la asamblea para que ésta determine la cuota que "
                + "corresponda.");
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

    private void agregarCeldaHeader(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fontTablaHeader));
        celda.setPadding(3f);
        celda.setBackgroundColor(new Color(240, 240, 240));
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(celda);
    }

    private void agregarCeldaBody(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fontTablaBody));
        celda.setPadding(4f);
        celda.setMinimumHeight(16f);
        tabla.addCell(celda);
    }

    private void agregarParrafo(Document document, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, fontNormal);
        p.setSpacingAfter(3f);
        document.add(p);
    }

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

    private String textoFecha(LocalDate fecha) {
        return fecha.getDayOfMonth() + " de " + MESES[fecha.getMonthValue() - 1] + " de " + fecha.getYear();
    }
}

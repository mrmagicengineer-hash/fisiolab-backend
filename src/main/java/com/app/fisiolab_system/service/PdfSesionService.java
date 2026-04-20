package com.app.fisiolab_system.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.app.fisiolab_system.model.EstadoSesionTerapia;
import com.app.fisiolab_system.model.NotaSOAP;
import com.app.fisiolab_system.model.SesionTerapia;
import com.app.fisiolab_system.repository.NotaSOAPRepository;
import com.app.fisiolab_system.repository.SesionTerapiaRepository;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;

@Service
public class PdfSesionService {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color COLOR_HEADER = new Color(30, 64, 175);   // blue-800
    private static final Color COLOR_SECCION = new Color(239, 246, 255); // blue-50
    private static final Color COLOR_FIRMA = new Color(5, 150, 105);    // emerald-600

    @Value("${app.unidad-salud:Clinica Fisiolab}")
    private String unidadSalud;

    private final SesionTerapiaRepository sesionRepository;
    private final NotaSOAPRepository notaRepository;

    public PdfSesionService(SesionTerapiaRepository sesionRepository,
            NotaSOAPRepository notaRepository) {
        this.sesionRepository = sesionRepository;
        this.notaRepository = notaRepository;
    }

    public byte[] generarPdf(Long sesionId) {
        SesionTerapia sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada: " + sesionId));

        if (sesion.getEstado() != EstadoSesionTerapia.FIRMADA) {
            throw new IllegalStateException("Solo se puede generar PDF de sesiones firmadas.");
        }

        NotaSOAP nota = notaRepository.findBySesionTerapiaId(sesionId)
                .orElseThrow(() -> new IllegalStateException("Nota SOAP no encontrada para sesión: " + sesionId));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 60, 60, 60, 60);

        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font fuenteTitulo   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.WHITE);
            Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_HEADER);
            Font fuenteLabel    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.DARK_GRAY);
            Font fuenteValor    = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font fuenteSoap     = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font fuenteFirma    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_FIRMA);
            Font fuenteHash     = FontFactory.getFont(FontFactory.COURIER, 7, Color.GRAY);

            // ── Header ──────────────────────────────────────────────────────
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell celdaHeader = new PdfPCell();
            celdaHeader.setBackgroundColor(COLOR_HEADER);
            celdaHeader.setPadding(12);
            celdaHeader.setBorder(Rectangle.NO_BORDER);
            Paragraph tituloParr = new Paragraph(unidadSalud.toUpperCase(), fuenteTitulo);
            tituloParr.setAlignment(Element.ALIGN_CENTER);
            celdaHeader.addElement(tituloParr);
            Paragraph subParr = new Paragraph("NOTA CLÍNICA DE SESIÓN DE FISIOTERAPIA",
                    FontFactory.getFont(FontFactory.HELVETICA, 10, Color.WHITE));
            subParr.setAlignment(Element.ALIGN_CENTER);
            celdaHeader.addElement(subParr);
            header.addCell(celdaHeader);
            doc.add(header);
            doc.add(espacio(8));

            // ── Datos del paciente y sesión ──────────────────────────────────
            doc.add(seccionTitulo("DATOS DE LA SESIÓN", fuenteSubtitulo));
            PdfPTable datosTable = new PdfPTable(new float[]{2, 4, 2, 4});
            datosTable.setWidthPercentage(100);
            datosTable.setSpacingBefore(4);

            agregarFila(datosTable, "Paciente:",
                    sesion.getPaciente().getNombresCompletos(), fuenteLabel, fuenteValor);
            agregarFila(datosTable, "Cédula:",
                    sesion.getPaciente().getCedula(), fuenteLabel, fuenteValor);
            agregarFila(datosTable, "Profesional:",
                    sesion.getProfesional().getName() + " " + sesion.getProfesional().getLastName(),
                    fuenteLabel, fuenteValor);
            agregarFila(datosTable, "Fecha sesión:",
                    sesion.getFechaHoraInicio().format(FECHA_FMT), fuenteLabel, fuenteValor);

            String sesionNum = sesion.getNumeroSesionEnPlan() != null
                    ? "Sesión #" + sesion.getNumeroSesionEnPlan() : "—";
            String planId = sesion.getPlanTratamiento() != null
                    ? "Plan #" + sesion.getPlanTratamiento().getId() : "Sin plan";
            agregarFila(datosTable, "Núm. sesión:", sesionNum, fuenteLabel, fuenteValor);
            agregarFila(datosTable, "Plan:", planId, fuenteLabel, fuenteValor);

            doc.add(datosTable);
            doc.add(espacio(12));

            // ── Nota SOAP ────────────────────────────────────────────────────
            doc.add(seccionTitulo("NOTA SOAP", fuenteSubtitulo));
            doc.add(espacio(4));

            doc.add(bloqueSOAP("S — SUBJETIVO", nota.getSubjetivo(), fuenteLabel, fuenteSoap));
            doc.add(espacio(6));
            doc.add(bloqueSOAP("O — OBJETIVO", nota.getObjetivo(), fuenteLabel, fuenteSoap));
            doc.add(espacio(6));
            doc.add(bloqueSOAP("A — ANÁLISIS", nota.getAnalisis(), fuenteLabel, fuenteSoap));
            doc.add(espacio(6));
            doc.add(bloqueSOAP("P — PLAN", nota.getPlan(), fuenteLabel, fuenteSoap));
            doc.add(espacio(16));

            // ── Firma digital ────────────────────────────────────────────────
            doc.add(seccionTitulo("FIRMA DIGITAL", fuenteSubtitulo));
            PdfPTable firmaTable = new PdfPTable(new float[]{2, 4, 2, 4});
            firmaTable.setWidthPercentage(100);
            firmaTable.setSpacingBefore(4);

            String firmadoPor = nota.getFirmadoPor() != null
                    ? nota.getFirmadoPor().getName() + " " + nota.getFirmadoPor().getLastName()
                    : "—";
            String firmadoEn = nota.getFirmadoEn() != null
                    ? nota.getFirmadoEn().format(FECHA_FMT) : "—";

            agregarFila(firmaTable, "Firmado por:", firmadoPor, fuenteLabel, fuenteFirma);
            agregarFila(firmaTable, "Fecha firma:", firmadoEn, fuenteLabel, fuenteFirma);
            doc.add(firmaTable);

            if (nota.getHashIntegridad() != null) {
                doc.add(espacio(6));
                Paragraph hashParr = new Paragraph("SHA-256: " + nota.getHashIntegridad(), fuenteHash);
                hashParr.setAlignment(Element.ALIGN_LEFT);
                doc.add(hashParr);
            }

            doc.add(espacio(20));

            // ── Footer ───────────────────────────────────────────────────────
            Paragraph footer = new Paragraph(
                    "Documento generado por " + unidadSalud + " — Uso exclusivo clínico. "
                            + "La integridad de este documento está garantizada por la firma digital SHA-256.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

        } finally {
            doc.close();
        }

        return baos.toByteArray();
    }

    // ── Helpers de layout ─────────────────────────────────────────────────────

    private Paragraph seccionTitulo(String texto, Font fuente) {
        Paragraph p = new Paragraph(texto, fuente);
        p.setSpacingBefore(4);
        p.setSpacingAfter(2);
        return p;
    }

    private Paragraph espacio(float altura) {
        Paragraph p = new Paragraph(" ");
        p.setLeading(altura);
        return p;
    }

    private void agregarFila(PdfPTable tabla, String label, String valor, Font fLabel, Font fValor) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fLabel));
        cLabel.setBorder(Rectangle.NO_BORDER);
        cLabel.setPadding(3);
        tabla.addCell(cLabel);

        PdfPCell cValor = new PdfPCell(new Phrase(valor != null ? valor : "—", fValor));
        cValor.setBorder(Rectangle.BOTTOM);
        cValor.setBorderColor(new Color(209, 213, 219));
        cValor.setPadding(3);
        tabla.addCell(cValor);
    }

    private PdfPTable bloqueSOAP(String etiqueta, String contenido, Font fLabel, Font fContenido) {
        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);

        PdfPCell celdaLabel = new PdfPCell(new Phrase(etiqueta, fLabel));
        celdaLabel.setBackgroundColor(COLOR_SECCION);
        celdaLabel.setPadding(5);
        celdaLabel.setBorder(Rectangle.LEFT);
        celdaLabel.setBorderColor(COLOR_HEADER);
        celdaLabel.setBorderWidth(3);
        tabla.addCell(celdaLabel);

        PdfPCell celdaContenido = new PdfPCell(
                new Phrase(contenido != null && !contenido.isBlank() ? contenido : "(sin contenido)", fContenido));
        celdaContenido.setPadding(8);
        celdaContenido.setBorder(Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT);
        celdaContenido.setBorderColor(new Color(209, 213, 219));
        tabla.addCell(celdaContenido);

        return tabla;
    }
}

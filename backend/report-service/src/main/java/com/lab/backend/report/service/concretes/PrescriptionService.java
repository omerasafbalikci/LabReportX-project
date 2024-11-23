package com.lab.backend.report.service.concretes;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lab.backend.report.utilities.exceptions.UnexpectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class responsible for generating PDF prescriptions based on diagnosis details.
 * This service interacts with {@link GeminiService} to generate insights and uses iText for PDF generation.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Service
@RequiredArgsConstructor
@Log4j2
public class PrescriptionService {
    @Value("${prescription.font-path.light}")
    private String LIGHT_FONT_PATH;

    @Value("${prescription.image-path.prescription}")
    private String PRESCRIPTION_IMAGE_PATH;

    @Value("${prescription.image-path.symbol}")
    private String SYMBOL_IMAGE_PATH;

    private final GeminiService geminiService;

    /**
     * Generates a PDF prescription document based on the given diagnosis details.
     *
     * @param diagnosisDetails Details of the diagnosis to generate insights from.
     * @return A byte array representing the generated PDF, or null if an error occurs.
     */
    public byte[] generatePrescription(String diagnosisDetails) {
        log.trace("Entering generatePrescription method in PrescriptionService");
        log.info("Starting PDF generation for prescription.");
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();
            log.debug("Loading light font from path: {}", LIGHT_FONT_PATH);
            BaseFont baseFontLight = BaseFont.createFont(LIGHT_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            com.itextpdf.text.Font lightSmall = new com.itextpdf.text.Font(baseFontLight, 12);

            log.info("Gemini image is being added to the document.");
            try (InputStream inputStream = getClass().getResourceAsStream(PRESCRIPTION_IMAGE_PATH)) {
                if (inputStream != null) {
                    com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(IOUtils.toByteArray(inputStream));
                    image.scaleToFit(100, 100);
                    image.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER);
                    document.add(image);
                } else {
                    log.warn("Gemini image not found at path: {}", PRESCRIPTION_IMAGE_PATH);
                }
            }

            document.add(new Paragraph("\n"));

            log.info("Fetching insight data from GeminiService.");
            String diagnosisAnalysis = this.geminiService.getInsight(diagnosisDetails);

            if (diagnosisAnalysis != null) {
                diagnosisAnalysis = diagnosisAnalysis.replace("\\*\\*", "").replace("##", "");
                log.debug("Formatting diagnosis analysis into sentences.");

                String[] sentencesArray = diagnosisAnalysis.split("(?<=[.!?])\\s*");
                List<String> sentences = new ArrayList<>();
                for (String sentence : sentencesArray) {
                    sentences.add(sentence.trim());
                }

                PdfPTable aiTable = new PdfPTable(2);
                aiTable.setWidthPercentage(90);
                aiTable.setWidths(new float[]{0.05f, 0.95f});

                for (String sentence : sentences) {
                    log.debug("Adding symbol image and sentence to PDF table.");
                    try (InputStream symbolInputStream = getClass().getResourceAsStream(SYMBOL_IMAGE_PATH)) {
                        if (symbolInputStream != null) {
                            com.itextpdf.text.Image symbolImage = com.itextpdf.text.Image.getInstance(IOUtils.toByteArray(symbolInputStream));
                            symbolImage.scaleToFit(15, 15);
                            symbolImage.setSpacingBefore(10f);

                            PdfPCell leftCell = new PdfPCell(symbolImage);
                            leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            leftCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                            leftCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);

                            aiTable.addCell(leftCell);
                        } else {
                            log.warn("Symbol image not found at path: {}", SYMBOL_IMAGE_PATH);
                        }
                    }

                    PdfPCell rightCell = new PdfPCell(new Phrase(sentence, lightSmall));
                    rightCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    rightCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                    aiTable.addCell(rightCell);
                }
                document.add(aiTable);
            } else {
                log.warn("No insight data returned from GeminiService.");
            }
            document.close();
            log.info("PDF generation completed successfully.");
            log.trace("Exiting generatePrescription method in PrescriptionService");
            return byteArrayOutputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error generating prescription PDF", e);
            throw new UnexpectedException("Error generating prescription PDF");
        }
    }
}

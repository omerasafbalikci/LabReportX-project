package com.lab.backend.report.service.concretes;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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

@Service
@RequiredArgsConstructor
@Log4j2
public class DiagnosisService {
    @Value("${chart.font-path.light}")
    private String LIGHT_FONT_PATH;

    @Value("${chart.font-path.regular}")
    private String REGULAR_FONT_PATH;

    @Value("${chart.image-path.gemini}")
    private String GEMINI_IMAGE_PATH;

    @Value("${chart.image-path.symbol}")
    private String SYMBOL_IMAGE_PATH;

    private final GeminiService geminiService;

    public byte[] generatePrescription(String diagnosisDetails) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            BaseFont baseFontLight = BaseFont.createFont(LIGHT_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            BaseFont baseFontRegular = BaseFont.createFont(REGULAR_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            com.itextpdf.text.Font bold = new com.itextpdf.text.Font(baseFontRegular, 20, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font boldBig = new com.itextpdf.text.Font(baseFontRegular, 30, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font boldSmall = new com.itextpdf.text.Font(baseFontRegular, 14, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font lightSmall = new com.itextpdf.text.Font(baseFontLight, 12);
            com.itextpdf.text.Font space = new com.itextpdf.text.Font(baseFontLight, 20, com.itextpdf.text.Font.BOLD);

            // Adding Gemini Image
            log.info("Gemini is active");
            try (InputStream inputStream = getClass().getResourceAsStream(GEMINI_IMAGE_PATH)) {
                if (inputStream != null) {
                    com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(IOUtils.toByteArray(inputStream));
                    image.scaleToFit(75, 75);
                    image.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER);
                    document.add(image);
                }
            }

            document.add(new Paragraph("\n"));

            // Gemini Insight
            log.info("Calling getInsight method in GeminiService");
            String diagnosisAnalysis = this.geminiService.getInsight(diagnosisDetails);

            // Formatting and adding sale analysis
            if (diagnosisAnalysis != null) {
                diagnosisAnalysis = diagnosisAnalysis.replace("\\*\\*", "").replace("##", "");

                String[] sentencesArray = diagnosisAnalysis.split("(?<=[.!?])\\s*");
                List<String> sentences = new ArrayList<>();
                for (String sentence : sentencesArray) {
                    sentences.add(sentence.trim());
                }

                PdfPTable aiTable = new PdfPTable(2);
                aiTable.setWidthPercentage(90);
                aiTable.setWidths(new float[]{0.05f, 0.95f});

                for (String sentence : sentences) {
                    // Adding symbol image to the table
                    try (InputStream symbolInputStream = getClass().getResourceAsStream(SYMBOL_IMAGE_PATH)) {
                        if (symbolInputStream != null) {
                            com.itextpdf.text.Image symbolImage = com.itextpdf.text.Image.getInstance(IOUtils.toByteArray(symbolInputStream));
                            symbolImage.scaleToFit(10, 10);
                            symbolImage.setSpacingBefore(10f);

                            PdfPCell leftCell = new PdfPCell(symbolImage);
                            leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            leftCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                            leftCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);

                            aiTable.addCell(leftCell);
                        }
                    }

                    // Adding sentence to the table
                    PdfPCell rightCell = new PdfPCell(new Phrase(sentence, lightSmall));
                    rightCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    rightCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);

                    aiTable.addCell(rightCell);
                }

                document.add(aiTable);
            }

            // Closing document
            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error generating prescription PDF", e);
            return null;
        }
    }
}

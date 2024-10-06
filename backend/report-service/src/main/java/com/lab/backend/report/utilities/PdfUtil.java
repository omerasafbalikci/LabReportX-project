package com.lab.backend.report.utilities;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lab.backend.report.dto.responses.GetPatientResponse;
import com.lab.backend.report.dto.responses.GetReportResponse;
import com.lab.backend.report.utilities.exceptions.UnexpectedException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * PdfUtil class is responsible for generating PDF documents for patient reports.
 * This utility generates a PDF based on the patient information and report details
 * passed as arguments. It uses a custom font, images, and formats the document to fit
 * a professional standard.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Component
@Log4j2
public class PdfUtil {
    @Value("${pdf.font-path}")
    private String FONT_PATH;

    @Value("${pdf.image-path}")
    private String IMAGE_PATH;

    @Value("${pdf.hospital-name}")
    private String HOSPITAL_NAME;

    /**
     * Generates a PDF document for a patient's report based on the provided
     * report and patient details.
     *
     * @param reportResponse  The report details including the diagnosis, technician info, etc.
     * @param patientResponse The patient details including name, contact info, etc.
     * @return A byte array containing the generated PDF document.
     */
    public byte[] generatePdf(GetReportResponse reportResponse, GetPatientResponse patientResponse) {
        log.trace("Entering generatePdf method in PdfUtil");
        log.info("Generating PDF for report: {} and patient: {}", reportResponse.getFileNumber(), patientResponse.getTrIdNumber());
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, byteArrayOutputStream);

            document.open();

            BaseFont baseFont = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font boldBig = new Font(baseFont, 22, Font.BOLD);
            Font boldSmall = new Font(baseFont, 14, Font.BOLD);
            Font light = new Font(baseFont, 10);
            Font hyphen = new Font(baseFont, 10, Font.BOLD);

            Paragraph hyphens = new Paragraph("-------------------------------------------------------------------------------------------------------------------------------------------------------------", hyphen);
            hyphens.setAlignment(Paragraph.ALIGN_CENTER);

            document.add(new Paragraph("\n"));

            Paragraph title = new Paragraph("HASTANIN RAPORU", boldBig);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Tarih              : " + reportResponse.getDate(), light));
            document.add(new Paragraph("Dosya Numarası     : " + reportResponse.getFileNumber(), light));

            document.add(hyphens);
            Paragraph patientInfoTitle = new Paragraph("HASTA BİLGİLERİ", boldSmall);
            document.add(patientInfoTitle);
            document.add(new Paragraph("Adı                : " + patientResponse.getFirstName(), light));
            document.add(new Paragraph("Soyadı             : " + patientResponse.getLastName(), light));
            document.add(new Paragraph("TC Kimlik No       : " + patientResponse.getTrIdNumber(), light));
            document.add(new Paragraph("Doğum Tarihi       : " + patientResponse.getBirthDate(), light));
            document.add(new Paragraph("Cinsiyet           : " + patientResponse.getGender(), light));
            document.add(new Paragraph("Kan Grubu          : " + patientResponse.getBloodType(), light));
            document.add(new Paragraph("Telefon Numarası   : " + patientResponse.getPhoneNumber(), light));

            document.add(hyphens);
            Paragraph diagnosisTitle = new Paragraph("TANI", boldSmall);
            document.add(diagnosisTitle);
            document.add(new Paragraph(reportResponse.getDiagnosisTitle(), light));

            Paragraph diagnosisDetails = new Paragraph("TANI DETAYLARI", boldSmall);
            document.add(diagnosisDetails);
            PdfPTable diagnosisTable = new PdfPTable(1);
            diagnosisTable.setWidthPercentage(100);
            PdfPCell diagnosisDetailsCell = new PdfPCell(new Paragraph(reportResponse.getDiagnosisDetails(), light));
            diagnosisDetailsCell.setFixedHeight(50);
            diagnosisDetailsCell.setVerticalAlignment(Element.ALIGN_TOP);
            diagnosisDetailsCell.setBorder(Rectangle.NO_BORDER);
            diagnosisTable.addCell(diagnosisDetailsCell);
            document.add(diagnosisTable);
            document.add(new Paragraph(" "));

            if (reportResponse.getPhotoPath() != null) {
                Image photo = Image.getInstance(reportResponse.getPhotoPath());
                photo.scaleAbsolute(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin(), 160);
                photo.setAlignment(Element.ALIGN_CENTER);
                document.add(photo);
                document.add(new Paragraph(" "));
            } else {
                Paragraph emptySpace = new Paragraph(" ", new Font(baseFont, 12));
                emptySpace.setSpacingBefore(160);
                emptySpace.setAlignment(Element.ALIGN_CENTER);
                document.add(emptySpace);
                document.add(new Paragraph(" "));
            }

            Paragraph technician = new Paragraph("Teknisyen: " + reportResponse.getTechnicianUsername(), light);
            technician.setAlignment(Element.ALIGN_RIGHT);
            document.add(technician);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(1);
            PdfPCell cell = new PdfPCell();
            cell.setFixedHeight(50);
            cell.setBorderWidth(1);
            cell.setBorder(Rectangle.BOX);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.setWidthPercentage(20);
            table.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            try (InputStream inputStream = getClass().getResourceAsStream(this.IMAGE_PATH)) {
                if (inputStream != null) {
                    Image image = Image.getInstance(IOUtils.toByteArray(inputStream));
                    image.scaleToFit(50, 50);
                    image.setAlignment(Image.ALIGN_CENTER);
                    document.add(image);
                } else {
                    log.warn("Logo image not found at path: {}", IMAGE_PATH);
                }
            }
            Paragraph hospitalName = new Paragraph(HOSPITAL_NAME, light);
            hospitalName.setAlignment(Element.ALIGN_CENTER);
            document.add(hospitalName);
            document.close();
            log.info("PDF generated successfully for report: {} and patient: {}", reportResponse.getFileNumber(), patientResponse.getTrIdNumber());
            log.trace("Exiting generatePdf method in PdfUtil");
            return byteArrayOutputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error occurred while generating PDF for report: {} and patient: {}", reportResponse.getFileNumber(), patientResponse.getTrIdNumber(), e);
            throw new UnexpectedException("Error occurred while generating the PDF document" + e);
        }
    }
}

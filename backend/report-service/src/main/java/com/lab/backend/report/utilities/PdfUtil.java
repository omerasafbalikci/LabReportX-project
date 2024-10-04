package com.lab.backend.report.utilities;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lab.backend.report.dto.responses.GetPatientResponse;
import com.lab.backend.report.dto.responses.GetReportResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
@Log4j2
public class PdfUtil {
    @Value("${pdf.font-path}")
    private String FONT_PATH;

    @Value("${pdf.image-path}")
    private String IMAGE_PATH;

    @Value("${pdf.hospital-name}")
    private String HOSPITAL_NAME;

    public byte[] generatePdf(GetReportResponse reportResponse, GetPatientResponse patientResponse) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, byteArrayOutputStream);

            document.open();

            BaseFont baseFont = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font boldBig = new Font(baseFont, 24, Font.BOLD);
            Font boldSmall = new Font(baseFont, 16, Font.BOLD);
            Font light = new Font(baseFont, 12);
            Font hyphen = new Font(baseFont, 12, Font.BOLD);

            Paragraph hyphens = new Paragraph("-----------------------------------------------------------------------------------------", hyphen);
            hyphens.setAlignment(Paragraph.ALIGN_CENTER);

            document.add(new Paragraph("\n"));

            Paragraph title = new Paragraph("HASTANIN RAPORU", boldBig);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Tarih: " + reportResponse.getDate(), light));
            document.add(new Paragraph("Dosya Numarası: " + reportResponse.getFileNumber(), light));
            document.add(new Paragraph(" "));

            document.add(hyphens);
            Paragraph patientInfoTitle = new Paragraph("HASTA BİLGİLERİ", boldSmall);
            document.add(patientInfoTitle);
            document.add(new Paragraph("Adı: " + patientResponse.getFirstName(), light));
            document.add(new Paragraph("Soyadı: " + patientResponse.getLastName(), light));
            document.add(new Paragraph("TC Kimlik No: " + patientResponse.getTrIdNumber(), light));
            document.add(new Paragraph("Doğum Tarihi: " + patientResponse.getBirthDate(), light));
            document.add(new Paragraph("Cinsiyet: " + patientResponse.getGender(), light));
            document.add(new Paragraph("Kan Grubu: " + patientResponse.getBloodType(), light));
            document.add(new Paragraph("Telefon: " + patientResponse.getPhoneNumber(), light));
            document.add(new Paragraph("E-posta: " + patientResponse.getEmail(), light));
            document.add(new Paragraph(" "));

            document.add(hyphens);
            Paragraph diagnosisTitle = new Paragraph("TANI", boldSmall);
            document.add(diagnosisTitle);
            document.add(new Paragraph(reportResponse.getDiagnosisTitle(), light));

            Paragraph diagnosisDetails = new Paragraph("TANI DETAYLARI", boldSmall);
            document.add(diagnosisDetails);
            document.add(new Paragraph(reportResponse.getDiagnosisDetails(), light));
            document.add(new Paragraph(" "));

            Image photo = Image.getInstance(reportResponse.getPhotoPath());
            photo.scaleToFit(250, 250);
            photo.setAlignment(Element.ALIGN_CENTER);
            document.add(photo);
            document.add(new Paragraph(" "));

            Paragraph technician = new Paragraph("Teknisyen: " + reportResponse.getTechnicianUsername(), light);
            technician.setAlignment(Element.ALIGN_RIGHT);
            document.add(technician);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(1);
            PdfPCell cell = new PdfPCell(new Paragraph("İmza:"));
            cell.setFixedHeight(50);
            cell.setBorderWidth(1);
            cell.setBorder(Rectangle.BOX);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.setWidthPercentage(30);
            table.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
            document.add(table);
            document.add(new Paragraph(" "));

            Paragraph hospitalName = new Paragraph(HOSPITAL_NAME, light);
            hospitalName.setAlignment(Element.ALIGN_CENTER);
            document.add(hospitalName);

            try (InputStream inputStream = getClass().getResourceAsStream(this.IMAGE_PATH)) {
                if (inputStream != null) {
                    Image image = Image.getInstance(IOUtils.toByteArray(inputStream));
                    image.scaleToFit(100, 100);
                    image.setAlignment(Image.ALIGN_CENTER);
                    document.add(image);
                    document.add(new Paragraph("\n"));
                }
            }
            document.close();
            return byteArrayOutputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.lab.backend.patient.service.concretes;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.entity.BloodType;
import com.lab.backend.patient.entity.Gender;
import com.lab.backend.patient.utilities.exceptions.CameraNotOpenedException;
import com.lab.backend.patient.utilities.exceptions.UnexpectedException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class BarcodeService {
    private List<GetPatientResponse> fakePatientDatabase = new ArrayList<>();

    public BarcodeService() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        initializeFakePatientsDatabase();
    }

    private void initializeFakePatientsDatabase() {
        fakePatientDatabase = List.of(
                new GetPatientResponse(
                        1L, "Ali", "Yılmaz", "12345678901", LocalDate.of(1980, 5, 15),
                        Gender.MALE, BloodType.A_POSITIVE, "+905321234567", "ali.yilmaz@example.com", LocalDateTime.now().minusDays(10)
                ),
                new GetPatientResponse(
                        2L, "Ayşe", "Kaya", "23456789012", LocalDate.of(1990, 7, 22),
                        Gender.FEMALE, BloodType.B_NEGATIVE, "+905431234567", "ayse.kaya@example.com", LocalDateTime.now().minusDays(20)
                ),
                new GetPatientResponse(
                        3L, "Mehmet", "Demir", "34567890123", LocalDate.of(1975, 3, 10),
                        Gender.MALE, BloodType.O_POSITIVE, "+905541234567", "mehmet.demir@example.com", LocalDateTime.now().minusDays(30)
                ),
                new GetPatientResponse(
                        4L, "Elif", "Çelik", "45678901234", LocalDate.of(1985, 12, 25),
                        Gender.FEMALE, BloodType.AB_NEGATIVE, "+905651234567", "elif.celik@example.com", LocalDateTime.now().minusDays(40)
                ),
                new GetPatientResponse(
                        5L, "Ahmet", "Öztürk", "56789012345", LocalDate.of(1992, 8, 5),
                        Gender.MALE, BloodType.B_POSITIVE, "+905761234567", "ahmet.ozturk@example.com", LocalDateTime.now().minusDays(50)
                ),
                new GetPatientResponse(
                        6L, "Fatma", "Koç", "67890123456", LocalDate.of(1988, 1, 17),
                        Gender.FEMALE, BloodType.A_NEGATIVE, "+905871234567", "fatma.koc@example.com", LocalDateTime.now().minusDays(60)
                ),
                new GetPatientResponse(
                        7L, "Emre", "Yıldız", "78901234567", LocalDate.of(1970, 4, 2),
                        Gender.MALE, BloodType.O_NEGATIVE, "+905981234567", "emre.yildiz@example.com", LocalDateTime.now().minusDays(70)
                ),
                new GetPatientResponse(
                        8L, "Seda", "Arslan", "89012345678", LocalDate.of(1995, 6, 18),
                        Gender.FEMALE, BloodType.A_POSITIVE, "+906091234567", "seda.arslan@example.com", LocalDateTime.now().minusDays(80)
                ),
                new GetPatientResponse(
                        9L, "Burak", "Güler", "90123456789", LocalDate.of(1983, 9, 12),
                        Gender.MALE, BloodType.B_NEGATIVE, "+906101234567", "burak.guler@example.com", LocalDateTime.now().minusDays(90)
                ),
                new GetPatientResponse(
                        10L, "Merve", "Kurt", "81234567890", LocalDate.of(1998, 11, 23),
                        Gender.FEMALE, BloodType.AB_POSITIVE, "+906211234567", "merve.kurt@example.com", LocalDateTime.now().minusDays(100)
                )
        );
    }

    public GetPatientResponse scanAndFetchPatient() {
        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            throw new CameraNotOpenedException("Camera could not be opened.");
        }

        Mat frame = new Mat();
        GetPatientResponse patient = new GetPatientResponse();

        if (camera.read(frame)) {
            BufferedImage bufferedImage = matToBufferedImage(frame);
            String barcodeData = readBarcodeFromImage(bufferedImage);
            if (barcodeData != null) {
                patient = getRandomPatient();
            }
        }
        camera.release();
        return patient;
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        try {
            MatOfByte mob = new MatOfByte();
            Imgcodecs.imencode(".jpg", mat, mob);
            byte[] byteArray = mob.toArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
            return ImageIO.read(bis);
        } catch (Exception e) {
            throw new UnexpectedException("Failed to convert Mat to BufferedImage.");
        }
    }

    private String readBarcodeFromImage(BufferedImage bufferedImage) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            throw new UnexpectedException("An error occurred while reading the barcode.");
        }
    }

    private GetPatientResponse getRandomPatient() {
        Random random = new Random();
        int index = random.nextInt(fakePatientDatabase.size());
        return fakePatientDatabase.get(index);
    }
}

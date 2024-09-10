package com.lab.backend.patient.service.concretes;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.entity.Patient;
import com.lab.backend.patient.repository.PatientRepository;
import com.lab.backend.patient.service.abstracts.BarcodeService;
import com.lab.backend.patient.utilities.exceptions.CameraNotOpenedException;
import com.lab.backend.patient.utilities.exceptions.InvalidTrIdNumberException;
import com.lab.backend.patient.utilities.exceptions.PatientNotFoundException;
import com.lab.backend.patient.utilities.exceptions.UnexpectedException;
import com.lab.backend.patient.utilities.mappers.PatientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Log4j2
public class BarcodeServiceImpl implements BarcodeService {
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    private static final int CAMERA_INDEX = 0;
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 480;

    static {
        OpenCV.loadLocally();
    }

    @Override
    public GetPatientResponse scanAndFetchPatient() {
        log.debug("Starting scanAndFetchPatient method.");
        VideoCapture camera = new VideoCapture(CAMERA_INDEX);
        if (!camera.isOpened()) {
            log.error("Camera could not be opened.");
            throw new CameraNotOpenedException("Camera could not be opened.");
        }

        try {
            log.trace("Setting camera properties: width={}, height={}", FRAME_WIDTH, FRAME_HEIGHT);
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, FRAME_WIDTH);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, FRAME_HEIGHT);

            Mat frame = new Mat();
            if (camera.read(frame)) {
                log.debug("Frame successfully read from the camera.");
                BufferedImage bufferedImage = matToBufferedImage(frame);
                String barcodeData = readBarcodeFromImage(bufferedImage);
                if (barcodeData != null) {
                    log.info("Barcode data read successfully: {}", barcodeData);
                    return getPatientByTrIdNumber(barcodeData);
                } else {
                    log.warn("No barcode data found.");
                }
            } else {
                log.error("Failed to read frame from the camera.");
            }
        } finally {
            camera.release();
            log.debug("Camera released.");
        }
        return null;
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        try {
            log.debug("Converting Mat to BufferedImage.");
            MatOfByte mob = new MatOfByte();
            Imgcodecs.imencode(".jpg", mat, mob);
            byte[] byteArray = mob.toArray();
            try (ByteArrayInputStream bis = new ByteArrayInputStream(byteArray)) {
                BufferedImage bufferedImage = ImageIO.read(bis);
                log.trace("BufferedImage created from Mat.");
                return bufferedImage;
            }
        } catch (Exception e) {
            log.error("Failed to convert Mat to BufferedImage.", e);
            throw new UnexpectedException("Failed to convert Mat to BufferedImage.");
        }
    }

    @Cacheable(value = "patients", key = "#trIdNumber", unless = "#result == null")
    private GetPatientResponse getPatientByTrIdNumber(String trIdNumber) {
        log.trace("Fetching patient by TR ID number: {}", trIdNumber);
        Patient patient = this.patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber).orElseThrow(() -> {
            log.error("Patient not found with TR ID number: {}", trIdNumber);
            return new PatientNotFoundException("Patient not found with TR ID number: " + trIdNumber);
        });
        GetPatientResponse response = this.patientMapper.toGetPatientResponse(patient);
        log.info("Successfully fetched patient by TR ID number: {}", trIdNumber);
        return response;
    }

    private String readBarcodeFromImage(BufferedImage bufferedImage) {
        try {
            log.debug("Reading barcode from image.");
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new QRCodeReader().decode(bitmap);
            String barcodeText = result.getText();
            log.trace("Barcode data extracted: {}", barcodeText);
            return barcodeText;
        } catch (NotFoundException e) {
            log.error("Barcode not found in image.", e);
            throw new UnexpectedException("An error occurred while reading the barcode." + e);
        } catch (FormatException | ChecksumException e) {
            log.error("Error while decoding the barcode.", e);
            throw new UnexpectedException("An error occurred while reading the barcode." + e);
        }
    }

    @Override
    public byte[] generateBarcodeForPatient(String trIdNumber) {
        if (trIdNumber == null || trIdNumber.trim().isEmpty()) {
            log.error("TR ID number is null or empty.");
            throw new InvalidTrIdNumberException("TR ID number cannot be null or empty.");
        }
        String regex = "^[1-9][0-9]{10}$";
        if (!trIdNumber.matches(regex)) {
            log.error("Invalid TR ID number format: {}", trIdNumber);
            throw new InvalidTrIdNumberException("Invalid TR ID number format.");
        }
        try {
            log.debug("Generating barcode for TR ID number: {}", trIdNumber);
            Writer writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(trIdNumber, BarcodeFormat.QR_CODE, 300, 300);
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
                byte[] barcodeBytes = os.toByteArray();
                log.trace("Barcode generated successfully, byte array length: {}", barcodeBytes.length);
                return barcodeBytes;
            }
        } catch (WriterException e) {
            log.error("Error occurred while generating barcode.", e);
            throw new UnexpectedException("Error occurred while generating barcode." + e);
        } catch (IOException e) {
            log.error("Error occurred while converting barcode to byte array.", e);
            throw new UnexpectedException("Error occurred while converting barcode to byte array." + e);
        }
    }
}

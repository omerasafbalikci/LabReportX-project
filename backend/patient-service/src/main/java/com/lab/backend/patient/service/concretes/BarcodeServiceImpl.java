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
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * This class implements the {@link BarcodeService} interface and handles the scanning
 * and generation of barcodes related to patient information. It leverages the OpenCV
 * library for capturing camera input and ZXing for barcode generation and decoding.
 *
 * @author Ömer Asaf BALIKÇI
 */

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

    /**
     * Scans a barcode using the default camera, retrieves the corresponding patient's information
     * by their TR ID number, and updates their last registration time. The patient is saved in the
     * cache after retrieval.
     *
     * @return GetPatientResponse the patient information or null if the barcode is not found.
     * @throws CameraNotOpenedException if the camera cannot be opened.
     * @throws UnexpectedException      if an error occurs while processing the barcode or frame.
     */
    @Override
    @CachePut(value = "patients", key = "#result.id", unless = "#result == null")
    public GetPatientResponse scanAndSavePatient() {
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
                    GetPatientResponse patientResponse = getPatientByTrIdNumber(barcodeData);
                    Patient patient = this.patientRepository.findByIdAndDeletedFalse(patientResponse.getId())
                            .orElseThrow(() -> {
                                log.error("Patient does not exist with id: {}", patientResponse.getId());
                                return new PatientNotFoundException("Patient does not exist with id: " + patientResponse.getId());
                            });
                    patient.setLastPatientRegistrationTime(LocalDateTime.now());
                    this.patientRepository.save(patient);
                    return patientResponse;
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

    /**
     * Converts an OpenCV {@link Mat} object to a {@link BufferedImage}.
     *
     * @param mat the matrix to be converted.
     * @return the resulting BufferedImage.
     * @throws UnexpectedException if an error occurs during the conversion process.
     */
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
            throw new UnexpectedException("Failed to convert Mat to BufferedImage." + e);
        }
    }

    /**
     * Fetches a patient by their TR ID number from the database. If the patient is found,
     * it returns their information, otherwise, throws a {@link PatientNotFoundException}.
     *
     * @param trIdNumber the TR ID number of the patient.
     * @return the GetPatientResponse containing patient information.
     * @throws PatientNotFoundException if the patient is not found in the database.
     */
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

    /**
     * Reads and decodes a barcode from the given image.
     *
     * @param bufferedImage the image containing the barcode.
     * @return the decoded barcode data or null if not found.
     * @throws UnexpectedException if an error occurs during barcode reading.
     */
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

    /**
     * Generates a QR code barcode for the specified TR ID number, validates it,
     * and returns it as a byte array in PNG format.
     *
     * @param trIdNumber the TR ID number for which the barcode is generated.
     * @return the generated barcode as a byte array in PNG format.
     * @throws InvalidTrIdNumberException if the TR ID number is invalid.
     * @throws PatientNotFoundException   if the patient with the given TR ID number does not exist.
     * @throws UnexpectedException        if an error occurs while generating the barcode.
     */
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
        if (!this.patientRepository.existsByTrIdNumberAndDeletedIsFalse(trIdNumber)) {
            log.error("Patient not found with TR ID: {}", trIdNumber);
            throw new PatientNotFoundException("Patient not found.");
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

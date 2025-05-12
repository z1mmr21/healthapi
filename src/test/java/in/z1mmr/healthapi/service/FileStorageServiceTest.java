package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.entity.DoctorEntity;
import in.z1mmr.healthapi.entity.MedicalRecordEntity;
import in.z1mmr.healthapi.entity.PatientEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUploadFile_Success(){
        MultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "content".getBytes());

        PutObjectResponse response = (PutObjectResponse) PutObjectResponse.builder()
                .sdkHttpResponse(mockSdkHttpResponse(true))
                .build();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(response);

        String actualUrl = fileStorageService.uploadFile(file);
        assertTrue(actualUrl.startsWith("https://clinic-record-system.s3.amazonaws.com/"));
        assertTrue(actualUrl.endsWith(".txt"));
    }

    @Test
    void testUploadFile_Failure(){
        MultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "content".getBytes());

        PutObjectResponse response = (PutObjectResponse) PutObjectResponse.builder()
                .sdkHttpResponse(mockSdkHttpResponse(false))
                .build();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(response);

        assertThrows(ResponseStatusException.class, () -> fileStorageService.uploadFile(file));
    }

    @Test
    void testUploadFile_ExceptionHandling() {
        MultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "content".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("S3 error"));

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> fileStorageService.uploadFile(file));

        assertEquals("Failed to upload file", thrown.getReason());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatusCode());

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadDocument_Success(){
        String content = "<html><body><h1>Medical Record</h1></body></html>";

        PutObjectResponse response = (PutObjectResponse) PutObjectResponse.builder()
                .sdkHttpResponse(mockSdkHttpResponse(true))
                .build();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(response);

        String actualUrl = fileStorageService.uploadDocument(content);
        assertTrue(actualUrl.startsWith("https://clinic-record-system.s3.amazonaws.com/"));
        assertTrue(actualUrl.endsWith(".html"));
    }

    @Test
    void testUploadDocument_Failure(){
        String content = "<html><body><h1>Medical Record</h1></body></html>";

        PutObjectResponse response = (PutObjectResponse) PutObjectResponse.builder()
                .sdkHttpResponse(mockSdkHttpResponse(false))
                .build();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(response);

        assertThrows(ResponseStatusException.class, () -> fileStorageService.uploadDocument(content));
    }

    @Test
    void testUploadDocument_Exception() {
        String content = "<html><body><h1>Medical Record</h1></body></html>";

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("Runtime Exception"));

        assertThrows(ResponseStatusException.class, () -> fileStorageService.uploadDocument(content));
    }

    @Test
    void testDeleteFile() {
        String key = "some-key.txt";

        fileStorageService.deleteFile(key);

        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void testGenerateHtmlContent() {
        Date localDate = new Date();
        MedicalRecordEntity medicalRecord = MedicalRecordEntity.builder()
                .id("record1")
                .date(localDate)
                .description("Description")
                .doctorId("doctor1")
                .patientId("patient1")
                .build();

        DoctorEntity doctor = DoctorEntity.builder()
                .id("doctor1")
                .firstName("Mike")
                .lastName("Donk")
                .specialization("Cardiologist")
                .imageUrl("https://example.com/image.jpg ")
                .email("mike@example.com")
                .phone("+1234567890")
                .build();

        PatientEntity patient = PatientEntity.builder()
                .id("patient1")
                .firstName("Jessi")
                .lastName("Jam")
                .gender("female")
                .birthdate("1999-09-09")
                .imageUrl("https://example.com/patient.jpg ")
                .email("jam@example.com")
                .phone("+0987654321")
                .build();

        String expectedHtml = "<html><head><title>Medical Record</title>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; margin: 20px; padding: 20px; background-color: #f9f9f9; }"
                + "h1, h2 { color: #333; }"
                + ".section { margin-bottom: 20px; padding: 10px; background-color: #fff; border: 1px solid #ddd; border-radius: 5px; }"
                + ".section h2 { margin-top: 0; }"
                + ".info p { margin: 5px 0; }"
                + ".info strong { display: inline-block; width: 150px; }"
                + "</style>"
                + "</head><body>"
                + "<div class=\"section\"><h1>Medical Record</h1></div>"
                + "<div class=\"section\"><h2>Clinic Information</h2><div class=\"info\">"
                + "<p><strong>Name:</strong> MedClinic</p>"
                + "<p><strong>Address:</strong> st. Medichna 12, Kiev, 01001</p>"
                + "<p><strong>Phone:</strong> +380 44 123 4567</p>"
                + "<p><strong>Email:</strong> info@medclinic.ua</p>"
                + "</div></div>"
                + "<div class=\"section\"><h2>Patient Information</h2><div class=\"info\">"
                + "<p><strong>Name:</strong> Jessi Jam</p>"
                + "<p><strong>Gender:</strong> female</p>"
                + "<p><strong>Birthdate:</strong> 1999-09-09</p>"
                + "<p><strong>Email:</strong> jam@example.com</p>"
                + "<p><strong>Phone:</strong> +0987654321</p>"
                + "</div></div>"
                + "<div class=\"section\"><h2>Doctor Information</h2><div class=\"info\">"
                + "<p><strong>Name:</strong> Mike Donk</p>"
                + "<p><strong>Specialization:</strong> Cardiologist</p>"
                + "<p><strong>Email:</strong> mike@example.com</p>"
                + "<p><strong>Phone:</strong> +1234567890</p>"
                + "</div></div>"
                + "<div class=\"section\"><h2>Medical Record Details</h2><div class=\"info\">"
                + "<p><strong>Date:</strong> " + localDate + "</p>"
                + "<p><strong>Description:</strong> Description</p>"
                + "</div></div>"
                + "</body></html>";

        String actualHtml = fileStorageService.generateHtmlContent(medicalRecord, doctor, patient);
        assertEquals(expectedHtml, actualHtml);
    }

    private software.amazon.awssdk.http.SdkHttpResponse mockSdkHttpResponse(boolean isSuccess) {
        software.amazon.awssdk.http.SdkHttpResponse sdkHttpResponse = mock(software.amazon.awssdk.http.SdkHttpResponse.class);
        when(sdkHttpResponse.isSuccessful()).thenReturn(isSuccess);
        return sdkHttpResponse;
    }
}

package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.entity.PatientEntity;
import in.z1mmr.healthapi.repository.PatientRepository;
import in.z1mmr.healthapi.request.PatientRequest;
import in.z1mmr.healthapi.request.PatientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private PatientServiceImpl patientService;

    private PatientEntity patientEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        patientEntity = PatientEntity.builder()
                .id("1")
                .firstName("Mark")
                .lastName("Donk")
                .email("Mark.Donk@example.com")
                .phone("+1234567890")
                .gender("Male")
                .birthdate("1999-09-09")
                .imageUrl("https://example.com/avatar.jpg")
                .build();
    }

    @Test
    void testAddPatient_Success() {
        PatientRequest request = new PatientRequest("Mark", "Donk", "Mark.Donk@example.com", "+1234567890", "Male", "1999-09-09");
        MultipartFile file = mock(MultipartFile.class);

        when(fileStorageService.uploadFile(file)).thenReturn("https://example.com/avatar.jpg");
        when(patientRepository.save(any(PatientEntity.class))).thenReturn(patientEntity);

        PatientResponse response = patientService.addPatient(request, file);

        assertNotNull(response);
        assertEquals("Mark", response.getFirstName());
        assertEquals("Donk", response.getLastName());
        assertEquals("Mark.Donk@example.com", response.getEmail());
        assertEquals("+1234567890", response.getPhone());
        assertEquals("Male", response.getGender());
        assertEquals("1999-09-09", response.getBirthdate());
        assertEquals("https://example.com/avatar.jpg", response.getImageUrl());
    }

    @Test
    void testReadPatients() {
        when(patientRepository.findAll()).thenReturn(List.of(patientEntity));

        List<PatientResponse> patients = patientService.readPatients();
        assertEquals(1, patients.size());
        assertEquals("Mark", patients.getFirst().getFirstName());
    }

    @Test
    void testReadPatient_NotFound() {
        when(patientRepository.findById("1")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> patientService.readPatient("1"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Patient not found id:1", exception.getReason());
    }

    @Test
    void testUpdatePatient() {
        PatientRequest updatedRequest = PatientRequest.builder()
                .firstName("Ivan")
                .lastName("Donk")
                .email("Ivan.updated@example.com")
                .phone("+1234567891")
                .gender("Male")
                .birthdate("1999-09-09")
                .build();

        when(patientRepository.findById("1")).thenReturn(Optional.of(patientEntity));
        when(patientRepository.save(any(PatientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponse updated = patientService.updatePatient("1", updatedRequest);

        assertEquals("Ivan", updated.getFirstName());
        assertEquals("Ivan.updated@example.com", updated.getEmail());
        assertEquals("+1234567891", updated.getPhone());
    }

    @Test
    void testDeletePatient() {
        when(patientRepository.findById("1")).thenReturn(Optional.of(patientEntity));
        when(fileStorageService.deleteFile(any())).thenReturn(true);

        patientService.deletePatient("1");

        verify(patientRepository, times(1)).deleteById("1");
        verify(fileStorageService, times(1)).deleteFile(any());
    }

    @Test
    void testUpdatePatientAvatar() {
        MultipartFile file = mock(MultipartFile.class);
        String newImageUrl = "https://example.com/updated-avatar.jpg";

        when(patientRepository.findById("1")).thenReturn(Optional.of(patientEntity));
        when(fileStorageService.uploadFile(file)).thenReturn(newImageUrl);
        when(fileStorageService.deleteFile(any())).thenReturn(true);
        when(patientRepository.save(any(PatientEntity.class))).thenReturn(patientEntity);

        PatientResponse updated = patientService.updatePatientAvatar("1", file);

        assertEquals(newImageUrl, updated.getImageUrl());
    }
}

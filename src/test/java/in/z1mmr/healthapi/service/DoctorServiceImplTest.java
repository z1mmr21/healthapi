package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.entity.DoctorEntity;
import in.z1mmr.healthapi.repository.DoctorRepository;
import in.z1mmr.healthapi.request.DoctorRequest;
import in.z1mmr.healthapi.request.DoctorResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DoctorServiceImplTest {
    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddDoctor_Success(){
        DoctorRequest request = new DoctorRequest("Jame", "Donk", "Cardiologist", "jame@example.com", "+1234567890");
        MultipartFile file = mock(MultipartFile.class);

        String imageUrl = "https://clinic-record-system.s3.amazonaws.com/doctor_image.jpg";
        when(fileStorageService.uploadFile(file)).thenReturn(imageUrl);

        DoctorEntity savedDoctor = DoctorEntity.builder()
                .id("1")
                .firstName("Jame")
                .lastName("Donk")
                .specialization("Cardiologist")
                .email("jame@example.com")
                .phone("+1234567890")
                .imageUrl(imageUrl)
                .build();

        when(doctorRepository.save(any(DoctorEntity.class))).thenReturn(savedDoctor);

        DoctorResponse response = doctorService.addDoctor(request, file);

        assertNotNull(response);
        assertEquals("1", response.getId());
        assertEquals("Jame", response.getFirstName());
        assertEquals("Donk", response.getLastName());
        assertEquals("Cardiologist", response.getSpecialization());
        assertEquals("jame@example.com", response.getEmail());
        assertEquals("+1234567890", response.getPhone());
        assertEquals(imageUrl, response.getImageUrl());
    }

    @Test
    void testAddDoctor_NoImage(){
        DoctorRequest request = new DoctorRequest("Jame", "Donk", "Cardiologist", "jame@example.com", "+1234567890");

        DoctorEntity savedDoctor = DoctorEntity.builder()
                .id("1")
                .firstName("Jame")
                .lastName("Donk")
                .specialization("Cardiologist")
                .email("jame@example.com")
                .phone("+1234567890")
                .imageUrl("")
                .build();

        when(doctorRepository.save(any(DoctorEntity.class))).thenReturn(savedDoctor);

        DoctorResponse response = doctorService.addDoctor(request, null);

        assertNotNull(response);
        assertEquals("1", response.getId());
        assertEquals("Jame", response.getFirstName());
        assertEquals("Donk", response.getLastName());
        assertEquals("Cardiologist", response.getSpecialization());
        assertEquals("jame@example.com", response.getEmail());
        assertEquals("+1234567890", response.getPhone());
        assertEquals("", response.getImageUrl());
    }

    @Test
    void testReadDoctors() {
        DoctorEntity doctor1 = DoctorEntity.builder()
                .id("1")
                .firstName("Jame")
                .lastName("Donk")
                .specialization("Cardiologist")
                .email("jame@example.com")
                .phone("+1234567890")
                .imageUrl("https://domen.com/doctor1.jpg")
                .build();

        DoctorEntity doctor2 = DoctorEntity.builder()
                .id("2")
                .firstName("Luci")
                .lastName("Marlo")
                .specialization("Neurologist")
                .email("luci@example.com")
                .phone("+0987654321")
                .imageUrl("https://domen.com/doctor2.jpg")
                .build();

        when(doctorRepository.findAll()).thenReturn(List.of(doctor1, doctor2));

        List<DoctorResponse> doctors = doctorService.readDoctors();
        assertEquals(2, doctors.size());

        DoctorResponse doctorResponse1 = doctors.getFirst();
        assertEquals("1", doctorResponse1.getId());
        assertEquals("Jame", doctorResponse1.getFirstName());
        assertEquals("Donk", doctorResponse1.getLastName());
        assertEquals("Cardiologist", doctorResponse1.getSpecialization());
        assertEquals("jame@example.com", doctorResponse1.getEmail());
        assertEquals("+1234567890", doctorResponse1.getPhone());
        assertEquals("https://domen.com/doctor1.jpg", doctorResponse1.getImageUrl());

        DoctorResponse doctorResponse2 = doctors.get(1);
        assertEquals("2", doctorResponse2.getId());
        assertEquals("Luci", doctorResponse2.getFirstName());
        assertEquals("Marlo", doctorResponse2.getLastName());
        assertEquals("Neurologist", doctorResponse2.getSpecialization());
        assertEquals("luci@example.com", doctorResponse2.getEmail());
        assertEquals("+0987654321", doctorResponse2.getPhone());
        assertEquals("https://domen.com/doctor2.jpg", doctorResponse2.getImageUrl());
    }

    @Test
    void testReadDoctor_NotFound() {
        when(doctorRepository.findById("1")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> doctorService.readDoctor("1"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Doctor not found id:1", exception.getReason());
    }

    @Test
    void testUpdateDoctor() {
        DoctorRequest request = new DoctorRequest("Jame", "Donk", "Cardiologist", "jame@example.com", "+1234567890");

        DoctorEntity existingDoctor = DoctorEntity.builder()
                .id("1")
                .firstName("Jame")
                .lastName("Donk")
                .specialization("Surgeon")
                .email("jame@example.com")
                .phone("+1234567890")
                .build();

        when(doctorRepository.findById("1")).thenReturn(Optional.of(existingDoctor));
        when(doctorRepository.save(any(DoctorEntity.class))).thenReturn(existingDoctor);

        DoctorResponse updatedDoctor = doctorService.updateDoctor("1", request);

        assertNotNull(updatedDoctor);
        assertEquals("Jame", updatedDoctor.getFirstName());
        assertEquals("Donk", updatedDoctor.getLastName());
        assertEquals("Cardiologist", updatedDoctor.getSpecialization());
    }


    @Test
    void testDeleteDoctor() {
        DoctorEntity doctorEntity = DoctorEntity.builder()
                .id("1")
                .firstName("Jame")
                .lastName("Donk")
                .specialization("Cardiologist")
                .email("jame@example.com")
                .phone("+1234567890")
                .imageUrl("https://clinic-record-system.s3.amazonaws.com/doctor_image.jpg")
                .build();

        when(doctorRepository.findById("1")).thenReturn(Optional.of(doctorEntity));
        when(fileStorageService.deleteFile(any())).thenReturn(true);

        doctorService.deleteDoctor("1");

        verify(doctorRepository, times(1)).deleteById("1");
        verify(fileStorageService, times(1)).deleteFile(any());
    }


    @Test
    void testUpdateDoctorAvatar() {
        String newImageUrl = "https://clinic-record-system.s3.amazonaws.com/new_doctor_image.jpg";
        MultipartFile newFile = mock(MultipartFile.class);

        DoctorEntity existingDoctor = DoctorEntity.builder()
                .id("1")
                .firstName("Jame")
                .lastName("Donk")
                .specialization("Cardiologist")
                .email("jame@example.com")
                .phone("+1234567890")
                .imageUrl("https://clinic-record-system.s3.amazonaws.com/old_doctor_image.jpg")
                .build();

        when(doctorRepository.findById("1")).thenReturn(Optional.of(existingDoctor));
        when(fileStorageService.uploadFile(newFile)).thenReturn(newImageUrl);
        when(fileStorageService.deleteFile(any())).thenReturn(true);
        when(doctorRepository.save(any(DoctorEntity.class))).thenReturn(existingDoctor);

        DoctorResponse updatedDoctor = doctorService.updateDoctorAvatar("1", newFile);

        assertEquals(newImageUrl, updatedDoctor.getImageUrl());
        verify(fileStorageService, times(1)).deleteFile(any());
        verify(doctorRepository, times(1)).save(any(DoctorEntity.class));
    }
}

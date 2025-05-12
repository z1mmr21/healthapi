package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.entity.DoctorEntity;
import in.z1mmr.healthapi.entity.MedicalRecordEntity;
import in.z1mmr.healthapi.entity.PatientEntity;
import in.z1mmr.healthapi.repository.DoctorRepository;
import in.z1mmr.healthapi.repository.MedicalRecordRepository;
import in.z1mmr.healthapi.repository.PatientRepository;
import in.z1mmr.healthapi.request.MedicalRecordRequest;
import in.z1mmr.healthapi.request.MedicalRecordResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class MedicalRecordServiceImplTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    private DoctorEntity doctor;
    private PatientEntity patient;
    private final Date date = new Date();
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doctor = DoctorEntity.builder().id("doc1").medicalRecordIds(new ArrayList<>()).build();
        patient = PatientEntity.builder().id("pat1").medicalRecordIds(new ArrayList<>()).build();
    }

    @Test
    void testAddMedicalRecord() {
        MedicalRecordRequest request = new MedicalRecordRequest(date, "Flu diagnosis", "pat1", "doc1");

        when(doctorRepository.findById("doc1")).thenReturn(Optional.of(doctor));
        when(patientRepository.findById("pat1")).thenReturn(Optional.of(patient));
        when(fileStorageService.generateHtmlContent(any(), any(), any())).thenReturn("<html>Medical Record</html>");
        when(fileStorageService.uploadDocument(anyString())).thenReturn("http://fake-url.com/document.html");
        when(medicalRecordRepository.save(any(MedicalRecordEntity.class))).thenAnswer(i -> {
            MedicalRecordEntity entity = i.getArgument(0);
            entity.setId("record1");
            return entity;
        });

        MedicalRecordResponse response = medicalRecordService.addMedicalRecord("doc1", "pat1", request);

        assertNotNull(response);
        assertEquals("doc1", response.getDoctorId());
        assertEquals("pat1", response.getPatientId());
        assertEquals("http://fake-url.com/document.html", response.getFileUrl());

        verify(doctorRepository).save(any(DoctorEntity.class));
        verify(patientRepository).save(any(PatientEntity.class));
    }

    @Test
    void testUpdateMedicalRecord() {
        MedicalRecordRequest request = new MedicalRecordRequest(date, "Updated diagnosis", "pat1", "doc2");
        MedicalRecordEntity existingRecord = MedicalRecordEntity.builder()
                .id("record1")
                .date(date)
                .description("Flu diagnosis")
                .patientId("pat1")
                .doctorId("doc1")
                .fileUrl("http://old-url.com/document.html")
                .build();

        when(medicalRecordRepository.findById("record1")).thenReturn(Optional.of(existingRecord));

        when(doctorRepository.findById("doc1")).thenReturn(Optional.of(new DoctorEntity(
                "doc1", "Dr. John", "Smith", "Male", "1975-12-01", "+987654321", "john.smith@example.com", new ArrayList<>()
        )));

        when(doctorRepository.findById("doc2")).thenReturn(Optional.of(new DoctorEntity(
                "doc2", "Mark", "Stee", "Male", "1980-01-01", "+123456789", "MarkStee@example.com", new ArrayList<>()
        )));

        when(patientRepository.findById("pat1")).thenReturn(Optional.of(patient));

        when(fileStorageService.generateHtmlContent(any(), any(), any())).thenReturn("<html>Updated Medical Record</html>");
        when(fileStorageService.uploadDocument(anyString())).thenReturn("http://new-url.com/document.html");

        when(medicalRecordRepository.save(any(MedicalRecordEntity.class))).thenAnswer(i -> i.getArgument(0));

        MedicalRecordResponse response = medicalRecordService.updateMedicalRecord("record1", request);

        assertNotNull(response);
        assertEquals(date, response.getDate());
        assertEquals("Updated diagnosis", response.getDescription());
        assertEquals("doc2", response.getDoctorId());
        assertEquals("pat1", response.getPatientId());
        assertEquals("http://new-url.com/document.html", response.getFileUrl());

        verify(doctorRepository, times(2)).save(any(DoctorEntity.class));
        verify(patientRepository, times(0)).save(any(PatientEntity.class));
    }

    @Test
    void testDeleteMedicalRecord() {
        MedicalRecordEntity existingRecord = MedicalRecordEntity.builder()
                .id("record1")
                .date(date)
                .description("Flu diagnosis")
                .patientId("pat1")
                .doctorId("doc1")
                .fileUrl("http://fake-url.com/document.html")
                .build();

        when(medicalRecordRepository.findById("record1")).thenReturn(Optional.of(existingRecord));
        when(doctorRepository.findById("doc1")).thenReturn(Optional.of(doctor));
        when(patientRepository.findById("pat1")).thenReturn(Optional.of(patient));

        medicalRecordService.deleteMedicalRecord("record1");

        verify(medicalRecordRepository).deleteById("record1");
        verify(fileStorageService).deleteFile("http://fake-url.com/document.html");
        verify(doctorRepository).save(any(DoctorEntity.class));
        verify(patientRepository).save(any(PatientEntity.class));
    }

    @Test
    void testGetAllMedicalRecords() {
        List<MedicalRecordEntity> records = Arrays.asList(
                MedicalRecordEntity.builder().id("record1").date(date).description("Flu diagnosis").build(),
                MedicalRecordEntity.builder().id("record2").date(date).description("Cold diagnosis").build()
        );

        when(medicalRecordRepository.findAll()).thenReturn(records);

        List<MedicalRecordResponse> responseList = medicalRecordService.getAllMedicalRecords();

        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals("record1", responseList.get(0).getId());
        assertEquals("record2", responseList.get(1).getId());
    }
}

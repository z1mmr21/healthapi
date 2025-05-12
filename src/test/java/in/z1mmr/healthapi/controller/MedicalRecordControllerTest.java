package in.z1mmr.healthapi.controller;

import in.z1mmr.healthapi.entity.DoctorEntity;
import in.z1mmr.healthapi.entity.MedicalRecordEntity;
import in.z1mmr.healthapi.entity.PatientEntity;
import in.z1mmr.healthapi.repository.DoctorRepository;
import in.z1mmr.healthapi.repository.MedicalRecordRepository;
import in.z1mmr.healthapi.repository.PatientRepository;
import in.z1mmr.healthapi.request.*;
import in.z1mmr.healthapi.service.FileStorageService;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class MedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private FileStorageService fileStorageService;

    private DoctorEntity doctor;
    private PatientEntity patient;

    @BeforeEach
    void setUp() {
        medicalRecordRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();

        doctor = doctorRepository.save(DoctorEntity.builder()
                .firstName("Mark")
                .lastName("Logan")
                .specialization("Test")
                .email("mark@example.com")
                .phone("123456")
                .medicalRecordIds(new ArrayList<>())
                .build());

        patient = patientRepository.save(PatientEntity.builder()
                .firstName("Gregory")
                .lastName("Morla")
                .email("greg@example.com")
                .phone("987654")
                .gender("Male")
                .birthdate("1999-09-09")
                .medicalRecordIds(new ArrayList<>())
                .build());

        when(fileStorageService.generateHtmlContent(
                any(MedicalRecordEntity.class),
                any(DoctorEntity.class),
                any(PatientEntity.class)))
                .thenReturn("<html>Mock</html>");

        when(fileStorageService.uploadDocument(any(String.class)))
                .thenReturn("https://fake-url.com/mockfile.html");
    }

    @Test
    void shouldCreateMedicalRecord() throws Exception {
        MedicalRecordRequest request = MedicalRecordRequest.builder()
                .description("Annual check-up")
                .date(new Date())
                .doctorId(doctor.getId())
                .patientId(patient.getId())
                .build();

        mockMvc.perform(post("/api/medical-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Annual check-up"));
    }

    @Test
    void shouldFetchMedicalRecordById() throws Exception {
        MedicalRecordEntity saved = medicalRecordRepository.save(MedicalRecordEntity.builder()
                .date(new Date())
                .description("Visit for flu")
                .doctorId(doctor.getId())
                .patientId(patient.getId())
                .fileUrl("url")
                .build());

        mockMvc.perform(get("/api/medical-records/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Visit for flu"));
    }

    @Test
    void shouldUpdateMedicalRecord() throws Exception {
        MedicalRecordEntity saved = medicalRecordRepository.save(MedicalRecordEntity.builder()
                .date(new Date())
                .description("Old desc")
                .doctorId(doctor.getId())
                .patientId(patient.getId())
                .fileUrl("url")
                .build());

        MedicalRecordRequest request = MedicalRecordRequest.builder()
                .date(new Date())
                .description("Updated desc")
                .doctorId(doctor.getId())
                .patientId(patient.getId())
                .build();

        mockMvc.perform(put("/api/medical-records/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated desc"));
    }

    @Test
    void shouldDeleteMedicalRecord() throws Exception {
        MedicalRecordEntity saved = medicalRecordRepository.save(MedicalRecordEntity.builder()
                .date(new Date())
                .description("Delete this")
                .doctorId(doctor.getId())
                .patientId(patient.getId())
                .fileUrl("url")
                .build());

        mockMvc.perform(delete("/api/medical-records/" + saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldFetchByDoctorId() throws Exception {
        medicalRecordRepository.save(MedicalRecordEntity.builder()
                .date(new Date())
                .description("Check")
                .doctorId(doctor.getId())
                .patientId(patient.getId())
                .fileUrl("url")
                .build());

        mockMvc.perform(get("/api/medical-records/doctors/" + doctor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldFetchByPatientId() throws Exception {
        medicalRecordRepository.save(MedicalRecordEntity.builder()
                .date(new Date())
                .description("Check")
                .doctorId(doctor.getId())
                .patientId(patient.getId())
                .fileUrl("url")
                .build());

        mockMvc.perform(get("/api/medical-records/patients/" + patient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}

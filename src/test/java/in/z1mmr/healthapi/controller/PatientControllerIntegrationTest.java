package in.z1mmr.healthapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import in.z1mmr.healthapi.request.PatientRequest;
import in.z1mmr.healthapi.request.PatientResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
public class PatientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private PatientRequest createSamplePatient() {
        return PatientRequest.builder()
                .firstName("Mike")
                .lastName("Donk")
                .gender("male")
                .birthdate("1999-09-09")
                .email("Mike@example.com")
                .phone("1234567890")
                .build();
    }

    @Test
    void testAddPatient() throws Exception {
        PatientRequest request = createSamplePatient();

        MockMultipartFile patientPart = new MockMultipartFile(
                "patient", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes()
        );

        mockMvc.perform(multipart("/api/patients")
                        .file(patientPart)
                        .file(filePart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Mike"))
                .andExpect(jsonPath("$.lastName").value("Donk"))
                .andExpect(jsonPath("$.email").value("Mike@example.com"));
    }

    @Test
    void testGetAllPatients() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void testGetPatientById() throws Exception {
        PatientRequest request = createSamplePatient();
        MockMultipartFile patientPart = new MockMultipartFile(
                "patient", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile filePart = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/api/patients")
                        .file(patientPart)
                        .file(filePart))
                .andReturn();

        PatientResponse created = objectMapper.readValue(result.getResponse().getContentAsByteArray(), PatientResponse.class);

        mockMvc.perform(get("/api/patients/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()));
    }

    @Test
    void testUpdatePatient() throws Exception {
        PatientRequest request = createSamplePatient();
        MockMultipartFile patientPart = new MockMultipartFile(
                "patient", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile filePart = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/api/patients")
                        .file(patientPart)
                        .file(filePart))
                .andReturn();

        PatientResponse created = objectMapper.readValue(result.getResponse().getContentAsByteArray(), PatientResponse.class);

        PatientRequest updatedRequest = PatientRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .gender("female")
                .birthdate("1992-02-02")
                .email("updated@example.com")
                .phone("9876543210")
                .build();

        MockMultipartFile idPart = new MockMultipartFile("id", "", MediaType.TEXT_PLAIN_VALUE, created.getId().getBytes());
        MockMultipartFile updatedPart = new MockMultipartFile("patient", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(updatedRequest));

        mockMvc.perform(multipart("/api/patients")
                        .file(idPart)
                        .file(updatedPart)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void testUpdatePatientImage() throws Exception {
        PatientRequest request = createSamplePatient();
        MockMultipartFile patientPart = new MockMultipartFile(
                "patient", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile filePart = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "initial-image".getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/api/patients")
                        .file(patientPart)
                        .file(filePart))
                .andReturn();

        PatientResponse created = objectMapper.readValue(result.getResponse().getContentAsByteArray(), PatientResponse.class);

        MockMultipartFile idPart = new MockMultipartFile("id", "", MediaType.TEXT_PLAIN_VALUE, created.getId().getBytes());
        MockMultipartFile newFilePart = new MockMultipartFile("file", "new-avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "new-image".getBytes());

        mockMvc.perform(multipart("/api/patients/update-image")
                        .file(idPart)
                        .file(newFilePart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()));
    }

    @Test
    void testDeletePatient() throws Exception {
        PatientRequest request = createSamplePatient();
        MockMultipartFile patientPart = new MockMultipartFile(
                "patient", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile filePart = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/api/patients")
                        .file(patientPart)
                        .file(filePart))
                .andReturn();

        PatientResponse created = objectMapper.readValue(result.getResponse().getContentAsByteArray(), PatientResponse.class);

        mockMvc.perform(delete("/api/patients/" + created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/patients/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    @AfterAll
    void cleanup() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<String> ids = JsonPath.read(jsonResponse, "$[*].id");

        for (String id : ids) {
            mockMvc.perform(delete("/api/patients/" + id))
                    .andExpect(status().isNoContent());
        }
    }
}

package in.z1mmr.healthapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import in.z1mmr.healthapi.request.DoctorRequest;
import in.z1mmr.healthapi.request.DoctorResponse;
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
public class DoctorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private DoctorRequest createSampleDoctor() {
        return new DoctorRequest("Mike", "Donk", "Cardiologist", "Mike.Donk@example.com", "1234567890");
    }

    @Test
    void testAddDoctor() throws Exception {
        DoctorRequest request = createSampleDoctor();

        MockMultipartFile doctorPart = new MockMultipartFile(
                "doctor", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes()
        );

        mockMvc.perform(multipart("/api/doctors")
                        .file(doctorPart)
                        .file(filePart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Mike"))
                .andExpect(jsonPath("$.specialization").value("Cardiologist"));
    }

    @Test
    void testGetAllDoctors() throws Exception {
        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)));
    }

    @Test
    void testGetDoctorById() throws Exception {
        DoctorRequest request = createSampleDoctor();
        MockMultipartFile doctorPart = new MockMultipartFile("doctor", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));
        MockMultipartFile filePart = new MockMultipartFile("file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes());

        MvcResult result = mockMvc.perform(multipart("/api/doctors")
                .file(doctorPart).file(filePart)).andReturn();

        DoctorResponse response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), DoctorResponse.class);

        mockMvc.perform(get("/api/doctors/" + response.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()));
    }

    @Test
    void testUpdateDoctor() throws Exception {
        DoctorRequest original = createSampleDoctor();
        MockMultipartFile doctorPart = new MockMultipartFile("doctor", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(original));
        MockMultipartFile filePart = new MockMultipartFile("file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes());

        MvcResult result = mockMvc.perform(multipart("/api/doctors").file(doctorPart).file(filePart)).andReturn();
        DoctorResponse created = objectMapper.readValue(result.getResponse().getContentAsByteArray(), DoctorResponse.class);

        DoctorRequest updated = new DoctorRequest("Luci", "Marlo", "Therapist", "Luci@example.com", "9876543210");
        MockMultipartFile idPart = new MockMultipartFile("id", "", MediaType.TEXT_PLAIN_VALUE, created.getId().getBytes());
        MockMultipartFile updatedPart = new MockMultipartFile("doctor", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(updated));

        mockMvc.perform(multipart("/api/doctors").file(idPart).file(updatedPart).with(req -> {
                    req.setMethod("PUT");
                    return req;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Luci"))
                .andExpect(jsonPath("$.specialization").value("Therapist"));
    }

    @Test
    void testUpdateDoctorImage() throws Exception {
        DoctorRequest request = createSampleDoctor();
        MockMultipartFile doctorPart = new MockMultipartFile("doctor", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));
        MockMultipartFile filePart = new MockMultipartFile("file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "initial".getBytes());

        MvcResult result = mockMvc.perform(multipart("/api/doctors").file(doctorPart).file(filePart)).andReturn();
        DoctorResponse created = objectMapper.readValue(result.getResponse().getContentAsByteArray(), DoctorResponse.class);

        MockMultipartFile idPart = new MockMultipartFile("id", "", MediaType.TEXT_PLAIN_VALUE, created.getId().getBytes());
        MockMultipartFile newImage = new MockMultipartFile("file", "new.jpg", MediaType.IMAGE_JPEG_VALUE, "updated".getBytes());

        mockMvc.perform(multipart("/api/doctors/update-image").file(idPart).file(newImage))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()));
    }

    @Test
    void testDeleteDoctor() throws Exception {
        DoctorRequest request = createSampleDoctor();
        MockMultipartFile doctorPart = new MockMultipartFile("doctor", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));
        MockMultipartFile filePart = new MockMultipartFile("file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        MvcResult result = mockMvc.perform(multipart("/api/doctors").file(doctorPart).file(filePart)).andReturn();
        DoctorResponse created = objectMapper.readValue(result.getResponse().getContentAsByteArray(), DoctorResponse.class);

        mockMvc.perform(delete("/api/doctors/" + created.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/doctors/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    @AfterAll
    void cleanup() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<String> ids = JsonPath.read(json, "$[*].id");

        for (String id : ids) {
            mockMvc.perform(delete("/api/doctors/" + id))
                    .andExpect(status().isOk());
        }
    }
}

package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.entity.ClinicEntity;
import in.z1mmr.healthapi.entity.DoctorEntity;
import in.z1mmr.healthapi.entity.MedicalRecordEntity;
import in.z1mmr.healthapi.entity.PatientEntity;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@AllArgsConstructor
public class FileStorageService {

    private final S3Client s3Client;
    private final String bucketName = "clinic-record-sys";

    public String uploadFile(MultipartFile file) {
        String filenameExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        String key = UUID.randomUUID().toString() + "." + filenameExtension;
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    //.acl("public-read")
                    .contentType(file.getContentType())
                    .build();
            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            if (response.sdkHttpResponse().isSuccessful()) {
                return "https://" + bucketName + ".s3.amazonaws.com/" + key;
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed");
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file", ex);
        }
    }

    public String uploadDocument(String content) {
        String key = UUID.randomUUID().toString() + "." + "html";
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .acl("public-read")
                    .contentType("text/html")
                    .build();
            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, content.length()));
            if (response.sdkHttpResponse().isSuccessful()) {
                return "https://" + bucketName + ".s3.amazonaws.com/" + key;
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed");
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    public boolean deleteFile(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        return true;
    }

    public String generateHtmlContent(MedicalRecordEntity medicalRecord, DoctorEntity doctor, PatientEntity patient) {
        ClinicEntity clinicEntity = ClinicEntity.builder()
                .name("MedClinic")
                .address("st. Medichna 12, Kiev, 01001")
                .phone("+380 44 123 4567")
                .email("info@medclinic.ua")
                .build();

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><head><title>Medical Record</title>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; margin: 20px; padding: 20px; background-color: #f9f9f9; }")
                .append("h1, h2 { color: #333; }")
                .append(".section { margin-bottom: 20px; padding: 10px; background-color: #fff; border: 1px solid #ddd; border-radius: 5px; }")
                .append(".section h2 { margin-top: 0; }")
                .append(".info p { margin: 5px 0; }")
                .append(".info strong { display: inline-block; width: 150px; }")
                .append("</style>")
                .append("</head><body>");

        htmlBuilder.append("<div class=\"section\">")
                .append("<h1>Medical Record</h1>")
                .append("</div>");

        htmlBuilder.append("<div class=\"section\">")
                .append("<h2>Clinic Information</h2>")
                .append("<div class=\"info\">")
                .append("<p><strong>Name:</strong> ").append(clinicEntity.getName()).append("</p>")
                .append("<p><strong>Address:</strong> ").append(clinicEntity.getAddress()).append("</p>")
                .append("<p><strong>Phone:</strong> ").append(clinicEntity.getPhone()).append("</p>")
                .append("<p><strong>Email:</strong> ").append(clinicEntity.getEmail()).append("</p>")
                .append("</div>")
                .append("</div>");

        htmlBuilder.append("<div class=\"section\">")
                .append("<h2>Patient Information</h2>")
                .append("<div class=\"info\">")
                .append("<p><strong>Name:</strong> ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append("</p>")
                .append("<p><strong>Gender:</strong> ").append(patient.getGender()).append("</p>")
                .append("<p><strong>Birthdate:</strong> ").append(patient.getBirthdate()).append("</p>")
                .append("<p><strong>Email:</strong> ").append(patient.getEmail()).append("</p>")
                .append("<p><strong>Phone:</strong> ").append(patient.getPhone()).append("</p>")
                .append("</div>")
                .append("</div>");

        htmlBuilder.append("<div class=\"section\">")
                .append("<h2>Doctor Information</h2>")
                .append("<div class=\"info\">")
                .append("<p><strong>Name:</strong> ").append(doctor.getFirstName()).append(" ").append(doctor.getLastName()).append("</p>")
                .append("<p><strong>Specialization:</strong> ").append(doctor.getSpecialization()).append("</p>")
                .append("<p><strong>Email:</strong> ").append(doctor.getEmail()).append("</p>")
                .append("<p><strong>Phone:</strong> ").append(doctor.getPhone()).append("</p>")
                .append("</div>")
                .append("</div>");

        htmlBuilder.append("<div class=\"section\">")
                .append("<h2>Medical Record Details</h2>")
                .append("<div class=\"info\">")
                .append("<p><strong>Date:</strong> ").append(medicalRecord.getDate()).append("</p>")
                .append("<p><strong>Description:</strong> ").append(medicalRecord.getDescription()).append("</p>")
                .append("</div>")
                .append("</div>");

        htmlBuilder.append("</body></html>");
        return htmlBuilder.toString();
    }

    /*public String generateHtmlContent(MedicalRecordEntity medicalRecord, DoctorEntity doctor, PatientEntity patient) {
        ClinicEntity clinicEntity = ClinicEntity.builder()
                .name("MedClinic")
                .address("st. Medichna 12, Kiev, 01001")
                .phone("+380 44 123 4567")
                .email("info@medclinic.ua")
                .build();

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><head><title>Medical Record</title>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; margin: 20px; padding: 20px; background-color: #f9f9f9; }")
                .append("h1, h2 { color: #333; }")
                .append(".section { margin-bottom: 20px; padding: 10px; background-color: #fff; border: 1px solid #ddd; border-radius: 5px; }")
                .append(".section h2 { margin-top: 0; }")
                .append(".info p { margin: 5px 0; }")
                .append(".info strong { display: inline-block; width: 150px; }")
                .append(".profile { display: flex; align-items: center; margin-bottom: 10px; }")
                .append(".profile img { width: 100px; height: 100px; border-radius: 50%; object-fit: cover; margin-right: 15px; }")
                .append("</style>")
                .append("</head><body>");

        htmlBuilder.append("<div class=\"section\">")
                .append("<h1>Medical Record</h1>")
                .append("</div>");

        htmlBuilder.append("<div class=\"section\">")
                .append("<h2>Clinic Information</h2>")
                .append("<div class=\"info\">")
                .append("<p><strong>Name:</strong> ").append(clinicEntity.getName()).append("</p>")
                .append("<p><strong>Address:</strong> ").append(clinicEntity.getAddress()).append("</p>")
                .append("<p><strong>Phone:</strong> ").append(clinicEntity.getPhone()).append("</p>")
                .append("<p><strong>Email:</strong> ").append(clinicEntity.getEmail()).append("</p>")
                .append("</div>")
                .append("</div>");

        htmlBuilder.append("<div class=\"section\">")
                .append("<h2>Patient Information</h2>")
                .append("<div class=\"profile\">")
                .append("<img src=\"").append(patient.getImageUrl()).append("\" alt=\"Patient Photo\">")
                .append("<div class=\"info\">")
                .append("<p><strong>Name:</strong> ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append("</p>")
                .append("<p><strong>Gender:</strong> ").append(patient.getGender()).append("</p>")
                .append("<p><strong>Birthdate:</strong> ").append(patient.getBirthdate()).append("</p>")
                .append("<p><strong>Email:</strong> ").append(patient.getEmail()).append("</p>")
                .append("<p><strong>Phone:</strong> ").append(patient.getPhone()).append("</p>")
                .append("</div>")
                .append("</div>")
                .append("</div>");

        htmlBuilder.append("<div class=\"section\">")
                .append("<h2>Doctor Information</h2>")
                .append("<div class=\"profile\">")
                .append("<img src=\"").append(doctor.getImageUrl()).append("\" alt=\"Doctor Photo\">")
                .append("<div class=\"info\">")
                .append("<p><strong>Name:</strong> ").append(doctor.getFirstName()).append(" ").append(doctor.getLastName()).append("</p>")
                .append("<p><strong>Specialization:</strong> ").append(doctor.getSpecialization()).append("</p>")
                .append("<p><strong>Email:</strong> ").append(doctor.getEmail()).append("</p>")
                .append("<p><strong>Phone:</strong> ").append(doctor.getPhone()).append("</p>")
                .append("</div>")
                .append("</div>")
                .append("</div>");

        htmlBuilder.append("<div class=\"section\">")
                .append("<h2>Medical Record Details</h2>")
                .append("<div class=\"info\">")
                .append("<p><strong>Date:</strong> ").append(medicalRecord.getDate()).append("</p>")
                .append("<p><strong>Description:</strong> ").append(medicalRecord.getDescription()).append("</p>")
                .append("</div>")
                .append("</div>");

        htmlBuilder.append("</body></html>");
        return htmlBuilder.toString();
    }*/
}

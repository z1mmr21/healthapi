package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.request.PatientRequest;
import in.z1mmr.healthapi.request.PatientResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PatientService {
    String uploadFile(MultipartFile file);

    PatientResponse addPatient(PatientRequest request, MultipartFile file);
    List<PatientResponse> readPatients();
    PatientResponse readPatient(String id);
    PatientResponse updatePatient(String id, PatientRequest request);
    boolean deleteFile(String file);
    void deletePatient(String id);
    PatientResponse updatePatientAvatar(String id, MultipartFile file);
}

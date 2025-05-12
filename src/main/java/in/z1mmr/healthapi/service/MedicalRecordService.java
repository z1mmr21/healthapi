package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.request.MedicalRecordRequest;
import in.z1mmr.healthapi.request.MedicalRecordResponse;

import java.util.List;

public interface MedicalRecordService {
    MedicalRecordResponse addMedicalRecord(String doctorId, String patientId, MedicalRecordRequest request);
    List<MedicalRecordResponse> getAllMedicalRecords();
    MedicalRecordResponse getMedicalRecordById(String id);
    MedicalRecordResponse updateMedicalRecord(String id, MedicalRecordRequest request);
    void deleteMedicalRecord(String id);

    List<MedicalRecordResponse> readMedicalRecordsByDoctorId(String doctorId);
    List<MedicalRecordResponse> readMedicalRecordsByPatientId(String patientId);
}

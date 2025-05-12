package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.entity.DoctorEntity;
import in.z1mmr.healthapi.entity.MedicalRecordEntity;
import in.z1mmr.healthapi.entity.PatientEntity;
import in.z1mmr.healthapi.repository.DoctorRepository;
import in.z1mmr.healthapi.repository.MedicalRecordRepository;
import in.z1mmr.healthapi.repository.PatientRepository;
import in.z1mmr.healthapi.request.MedicalRecordRequest;
import in.z1mmr.healthapi.request.MedicalRecordResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final FileStorageService fileStorageService;

    @Override
    public MedicalRecordResponse addMedicalRecord(String doctorId, String patientId, MedicalRecordRequest request) {
        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        MedicalRecordEntity newMedicalRecordEntity = convertToEntity(request);
        newMedicalRecordEntity.setDoctorId(doctor.getId());
        newMedicalRecordEntity.setPatientId(patient.getId());

        String htmlContent = fileStorageService.generateHtmlContent(newMedicalRecordEntity, doctor, patient);
        String fileUrl = fileStorageService.uploadDocument(htmlContent);
        newMedicalRecordEntity.setFileUrl(fileUrl);
        newMedicalRecordEntity = medicalRecordRepository.save(newMedicalRecordEntity);

        doctor.getMedicalRecordIds().add(newMedicalRecordEntity.getId());
        doctorRepository.save(doctor);


        patient.getMedicalRecordIds().add(newMedicalRecordEntity.getId());
        patientRepository.save(patient);

        return convertToResponse(newMedicalRecordEntity);
    }

    @Override
    public List<MedicalRecordResponse> getAllMedicalRecords() {
        return medicalRecordRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MedicalRecordResponse getMedicalRecordById(String id) {
        MedicalRecordEntity medicalRecord = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));
        return convertToResponse(medicalRecord);
    }

    @Override
    public MedicalRecordResponse updateMedicalRecord(String id, MedicalRecordRequest request) {
        MedicalRecordEntity existingMedicalRecord = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        DoctorEntity oldDoctor = doctorRepository.findById(existingMedicalRecord.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Old Doctor not found"));
        PatientEntity oldPatient = patientRepository.findById(existingMedicalRecord.getPatientId())
                .orElseThrow(() -> new RuntimeException("Old Patient not found"));

        existingMedicalRecord.setDate(request.getDate());
        existingMedicalRecord.setDescription(request.getDescription());
        existingMedicalRecord.setPatientId(request.getPatientId());
        existingMedicalRecord.setDoctorId(request.getDoctorId());

        DoctorEntity newDoctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("New Doctor not found"));
        PatientEntity newPatient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("New Patient not found"));

        boolean doctorChanged = !oldDoctor.getId().equals(newDoctor.getId());
        boolean patientChanged = !oldPatient.getId().equals(newPatient.getId());

        if (doctorChanged) {
            oldDoctor.getMedicalRecordIds().remove(id);
            doctorRepository.save(oldDoctor);
            if (!newDoctor.getMedicalRecordIds().contains(id)) {
                newDoctor.getMedicalRecordIds().add(id);
                doctorRepository.save(newDoctor);
            }
        }

        if (patientChanged) {
            oldPatient.getMedicalRecordIds().remove(id);
            patientRepository.save(oldPatient);
            if (!newPatient.getMedicalRecordIds().contains(id)) {
                newPatient.getMedicalRecordIds().add(id);
                patientRepository.save(newPatient);
            }
        }

        String htmlContent = fileStorageService.generateHtmlContent(existingMedicalRecord, newDoctor, newPatient);
        String fileUrl = fileStorageService.uploadDocument(htmlContent);

        if (existingMedicalRecord.getFileUrl() != null) {
            fileStorageService.deleteFile(existingMedicalRecord.getFileUrl());
        }

        existingMedicalRecord.setFileUrl(fileUrl);

        MedicalRecordEntity updatedMedicalRecord = medicalRecordRepository.save(existingMedicalRecord);

        return convertToResponse(updatedMedicalRecord);
    }



    @Override
    public void deleteMedicalRecord(String id) {
        MedicalRecordEntity medicalRecord = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        DoctorEntity doctor = doctorRepository.findById(medicalRecord.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.getMedicalRecordIds().remove(id);
        doctorRepository.save(doctor);

        PatientEntity patient = patientRepository.findById(medicalRecord.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        patient.getMedicalRecordIds().remove(id);
        patientRepository.save(patient);

        if (medicalRecord.getFileUrl() != null) {
            fileStorageService.deleteFile(medicalRecord.getFileUrl());
        }

        medicalRecordRepository.deleteById(id);
    }

    @Override
    public List<MedicalRecordResponse> readMedicalRecordsByDoctorId(String doctorId) {
        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        List<MedicalRecordEntity> medicalRecords = medicalRecordRepository.findByDoctorId(doctorId);
        return medicalRecords.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicalRecordResponse> readMedicalRecordsByPatientId(String patientId) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        List<MedicalRecordEntity> medicalRecords = medicalRecordRepository.findByPatientId(patientId);
        return medicalRecords.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private MedicalRecordEntity convertToEntity(MedicalRecordRequest request) {
        return MedicalRecordEntity.builder()
                .date(request.getDate())
                .description(request.getDescription())
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .build();
    }

    private MedicalRecordResponse convertToResponse(MedicalRecordEntity entity) {
        return MedicalRecordResponse.builder()
                .id(entity.getId())
                .date(entity.getDate())
                .description(entity.getDescription())
                .patientId(entity.getPatientId())
                .doctorId(entity.getDoctorId())
                .fileUrl(entity.getFileUrl())
                .build();
    }
}

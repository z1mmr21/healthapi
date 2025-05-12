package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.entity.PatientEntity;
import in.z1mmr.healthapi.repository.PatientRepository;
import in.z1mmr.healthapi.request.PatientRequest;
import in.z1mmr.healthapi.request.PatientResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final S3Client s3Client;
    private final PatientRepository patientRepository;
    private final FileStorageService fileStorageService;

    @Override
    public String uploadFile(MultipartFile file) {
        return fileStorageService.uploadFile(file);
    }

    @Override
    public PatientResponse addPatient(PatientRequest request, MultipartFile file) {
        PatientEntity newPatientEntity = convertToEntity(request);
        String imageUrl = uploadFile(file);
        newPatientEntity.setImageUrl(imageUrl);
        newPatientEntity = patientRepository.save(newPatientEntity);
        return convertToResponse(newPatientEntity);
    }

    @Override
    public List<PatientResponse> readPatients() {
        List<PatientEntity> patients = patientRepository.findAll();
        return patients.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public PatientResponse readPatient(String id) {
        PatientEntity patientEntity = patientRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found id:"+id));
        return convertToResponse(patientEntity);
    }

    @Override
    public PatientResponse updatePatient(String id, PatientRequest request) {
        PatientEntity existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        existingPatient.setFirstName(request.getFirstName());
        existingPatient.setLastName(request.getLastName());
        existingPatient.setEmail(request.getEmail());
        existingPatient.setPhone(request.getPhone());
        existingPatient.setGender(request.getGender());
        existingPatient.setBirthdate(request.getBirthdate());
        PatientEntity updated = patientRepository.save(existingPatient);
        return convertToResponse(updated);
    }

    @Override
    public boolean deleteFile(String file) {
        return fileStorageService.deleteFile(file);
    }

    @Override
    public void deletePatient(String id) {
        PatientResponse response = readPatient(id);
        String imageUrl = response.getImageUrl();
        if(imageUrl != null) {
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            boolean isFileDeleted = deleteFile(filename);
            if (isFileDeleted) {
                patientRepository.deleteById(id);
            }
        }
    }

    @Override
        public PatientResponse updatePatientAvatar(String id, MultipartFile file) {
            PatientEntity existingPatient = patientRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found id:"+id));
            String imageUrl = uploadFile(file);
            if(!imageUrl.isEmpty()) {
                String oldImageUrl = existingPatient.getImageUrl();
                String filename = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1);
                boolean isFileDeleted = deleteFile(filename);
                if(isFileDeleted) {
                    existingPatient.setImageUrl(imageUrl);
                    PatientEntity updated = patientRepository.save(existingPatient);
                    return convertToResponse(updated);
                }
            }
            return convertToResponse(existingPatient);
    }

    private PatientEntity convertToEntity(PatientRequest request) {
        return PatientEntity.builder()
                //.id(request.getId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .gender(request.getGender())
                .birthdate(request.getBirthdate())
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

    }

    private PatientResponse convertToResponse(PatientEntity entity) {
        return PatientResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .gender(entity.getGender())
                .birthdate(entity.getBirthdate())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .imageUrl(entity.getImageUrl())
                .medicalRecordIds(entity.getMedicalRecordIds())
                .build();
    }
}

package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.entity.DoctorEntity;
import in.z1mmr.healthapi.repository.DoctorRepository;
import in.z1mmr.healthapi.request.DoctorRequest;
import in.z1mmr.healthapi.request.DoctorResponse;
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
public class DoctorServiceImpl implements DoctorService {

    private final S3Client s3Client;
    private final DoctorRepository doctorRepository;
    private final FileStorageService fileStorageService;



    @Override
    public String uploadFile(MultipartFile file) {
        return fileStorageService.uploadFile(file);
    }

    @Override
    public DoctorResponse addDoctor(DoctorRequest request, MultipartFile file) {
        DoctorEntity newDoctorEntity = convertToEntity(request);
        String imageUrl = uploadFile(file);
        newDoctorEntity.setImageUrl(imageUrl);
        newDoctorEntity = doctorRepository.save(newDoctorEntity);
        return convertToResponse(newDoctorEntity);
    }

    @Override
    public List<DoctorResponse> readDoctors() {
        List<DoctorEntity> doctors = doctorRepository.findAll();
        return doctors.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public DoctorResponse readDoctor(String id) {
        DoctorEntity entity = doctorRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found id:"+id));
        return convertToResponse(entity);
    }

    @Override
    public DoctorResponse updateDoctor(String id, DoctorRequest request) {
        DoctorEntity existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        existingDoctor.setFirstName(request.getFirstName());
        existingDoctor.setLastName(request.getLastName());
        existingDoctor.setSpecialization(request.getSpecialization());
        existingDoctor.setEmail(request.getEmail());
        existingDoctor.setPhone(request.getPhone());
        DoctorEntity updatedDoctor = doctorRepository.save(existingDoctor);
        return convertToResponse(updatedDoctor);
    }

    @Override
    public boolean deleteFile(String file) {
        return fileStorageService.deleteFile(file);
    }

    @Override
    public void deleteDoctor(String id) {
        DoctorResponse response = readDoctor(id);
        String imageUrl = response.getImageUrl();
        if(imageUrl != null) {
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            boolean isFileDeleted = deleteFile(filename);
            if (isFileDeleted) {
                doctorRepository.deleteById(id);
            }
        }
    }

    @Override
    public DoctorResponse updateDoctorAvatar(String id, MultipartFile file) {
        DoctorEntity existingDoctor = doctorRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found id:"+id));
        String imageUrl = uploadFile(file);
        if(!imageUrl.isEmpty()) {
            String oldImageUrl = existingDoctor.getImageUrl();
            String filename = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1);
            boolean isFileDeleted = deleteFile(filename);
            if (isFileDeleted) {
                existingDoctor.setImageUrl(imageUrl);
                DoctorEntity updatedDoctor = doctorRepository.save(existingDoctor);
                return convertToResponse(updatedDoctor);
            }
        }
        return convertToResponse(existingDoctor);
    }

    private DoctorEntity convertToEntity(DoctorRequest request) {
        return DoctorEntity.builder()
                //.id(request.getId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .specialization(request.getSpecialization())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

    }

    private DoctorResponse convertToResponse(DoctorEntity entity) {
        return DoctorResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .imageUrl(entity.getImageUrl())
                .specialization(entity.getSpecialization())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .medicalRecordIds(entity.getMedicalRecordIds())
                .build();
    }
}
package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.request.DoctorRequest;
import in.z1mmr.healthapi.request.DoctorResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DoctorService {
    String uploadFile(MultipartFile file);

    DoctorResponse addDoctor(DoctorRequest request, MultipartFile file);
    List<DoctorResponse> readDoctors();
    DoctorResponse readDoctor(String id);
    DoctorResponse updateDoctor(String id, DoctorRequest request);
    boolean deleteFile(String file);
    void deleteDoctor(String id);
    DoctorResponse updateDoctorAvatar(String id, MultipartFile file);
}

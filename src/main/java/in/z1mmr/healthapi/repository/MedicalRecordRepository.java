package in.z1mmr.healthapi.repository;

import in.z1mmr.healthapi.entity.MedicalRecordEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends MongoRepository<MedicalRecordEntity, String> {
    @Query("{ 'doctorId': ?0 }")
    List<MedicalRecordEntity> findByDoctorId(String doctorId);

    @Query("{ 'patientId': ?0 }")
    List<MedicalRecordEntity> findByPatientId(String patientId);
}

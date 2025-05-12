package in.z1mmr.healthapi.repository;

import in.z1mmr.healthapi.entity.PatientEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends MongoRepository<PatientEntity, String> {
}

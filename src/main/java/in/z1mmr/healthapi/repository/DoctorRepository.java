package in.z1mmr.healthapi.repository;

import in.z1mmr.healthapi.entity.DoctorEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends MongoRepository<DoctorEntity, String> {
}

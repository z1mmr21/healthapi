package in.z1mmr.healthapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "medical_records")
public class MedicalRecordEntity {
    @Id
    private String id;
    private Date date;
    private String description;
    @Field("patient_id")
    private String patientId;
    @Field("doctor_id")
    private String doctorId;
    @Field("file_url")
    private String fileUrl;
}

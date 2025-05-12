package in.z1mmr.healthapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "doctors")
public class DoctorEntity {
    @Id
    private String id;
    @Field("first_name")
    private String firstName;
    @Field("last_name")
    private String lastName;
    private String specialization;
    @Field("image_url")
    private String imageUrl;
    private String email;
    private String phone;
    @Field("medical_record_ids")
    private List<String> medicalRecordIds = new ArrayList<>();;
}

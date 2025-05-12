package in.z1mmr.healthapi.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String specialization;
    private String imageUrl;
    private String email;
    private String phone;
    private List<String> medicalRecordIds;
}

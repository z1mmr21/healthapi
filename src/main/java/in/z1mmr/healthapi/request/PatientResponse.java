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
public class PatientResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String gender;
    private String birthdate;
    private String email;
    private String phone;
    private String imageUrl;
    private List<String> medicalRecordIds;
}

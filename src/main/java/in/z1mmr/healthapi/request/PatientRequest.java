package in.z1mmr.healthapi.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientRequest {
    //private String id;

    private String firstName;
    private String lastName;
    private String gender;
    private String birthdate;
    private String email;
    private String phone;
    //private String imageUrl;
}

package in.z1mmr.healthapi.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorRequest {
    //private String id;

    private String firstName;
    private String lastName;
    private String specialization;
    //private String imageUrl;
    private String email;
    private String phone;
}

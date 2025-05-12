package in.z1mmr.healthapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@Document(collection = "clinics")
public class ClinicEntity {
    //@Id
    //private String id;
    private String name;
    private String address;
    private String phone;
    private String email;
}

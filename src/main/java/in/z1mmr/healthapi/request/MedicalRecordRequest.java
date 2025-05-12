package in.z1mmr.healthapi.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalRecordRequest {
    //private String id;
    private Date date;
    private String description;
    private String patientId;
    private String doctorId;
    //private String fileUrl;
}

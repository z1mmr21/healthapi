package in.z1mmr.healthapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.z1mmr.healthapi.request.PatientRequest;
import in.z1mmr.healthapi.request.PatientResponse;
import in.z1mmr.healthapi.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/patients")
@AllArgsConstructor
@Tag(name = "Пацієнти", description = "Операції для керування інформацією про пацієнтів")
public class PatientController {
    private final PatientService patientService;

    @Operation(
            summary = "Додати нового пацієнта",
            description = "Створює нового пацієнта з інформацією та зображенням профілю"
    )
    @PostMapping
    public PatientResponse addPatient(@RequestPart("patient") String patientString, @RequestPart("file") MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();
        PatientRequest request = null;
        try{
            request = objectMapper.readValue(patientString, PatientRequest.class);
        }catch (JsonProcessingException ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        return patientService.addPatient(request, file);
    }

    @Operation(
            summary = "Отримати всіх пацієнтів",
            description = "Повертає список усіх зареєстрованих пацієнтів"
    )
    @GetMapping
    public List<PatientResponse> readPatients() {
        return patientService.readPatients();
    }

    @Operation(
            summary = "Отримати пацієнта за ID",
            description = "Повертає детальну інформацію про пацієнта за його ідентифікатором"
    )
    @GetMapping("/{id}")
    public PatientResponse readPatient(@PathVariable("id") String id) {
        return patientService.readPatient(id);
    }

    @Operation(
            summary = "Видалити пацієнта",
            description = "Видаляє пацієнта з бази даних за вказаним ID"
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatient(@PathVariable("id") String id) {
        patientService.deletePatient(id);
    }

    @Operation(
            summary = "Оновити пацієнта",
            description = "Оновлює особисту інформацію пацієнта за його ID"
    )
    @PutMapping
    public PatientResponse updatePatient(@RequestPart("id") String id, @RequestPart("patient") String patientString) {
        ObjectMapper objectMapper = new ObjectMapper();
        PatientRequest request = null;
        try{
            request = objectMapper.readValue(patientString, PatientRequest.class);
        }catch (JsonProcessingException ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        return patientService.updatePatient(id, request);
    }

    @Operation(
            summary = "Оновити зображення пацієнта",
            description = "Оновлює аватар пацієнта за його ID"
    )
    @PostMapping("/update-image")
    public PatientResponse updatePatientImage(@RequestPart("id") String id, @RequestPart("file") MultipartFile file) {
        /*
        ObjectMapper objectMapper = new ObjectMapper();
        String _id = null;
        try{
            _id = objectMapper.readValue(id, String.class);
        }catch (JsonProcessingException ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        */
        return patientService.updatePatientAvatar(id, file);
    }
}

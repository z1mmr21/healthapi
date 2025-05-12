package in.z1mmr.healthapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.z1mmr.healthapi.request.DoctorRequest;
import in.z1mmr.healthapi.request.DoctorResponse;
import in.z1mmr.healthapi.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/doctors")
@AllArgsConstructor
@Tag(name = "Лікарі", description = "Операції для керування лікарями: створення, читання, оновлення та видалення лікарів")
public class DoctorController {

    private final DoctorService doctorService;

    @Operation(summary = "Додати нового лікаря", description = "Додає нового лікаря з інформацією та зображенням.")
    @PostMapping
    public DoctorResponse addDoctor(@RequestPart("doctor") String doctorString, @RequestPart("file") MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();
        DoctorRequest request = null;
        try{
            request = objectMapper.readValue(doctorString, DoctorRequest.class);
        }catch (JsonProcessingException ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        return doctorService.addDoctor(request, file);
    };

    @Operation(summary = "Отримати список усіх лікарів", description = "Повертає повний список усіх лікарів у системі.")
    @GetMapping
    public List<DoctorResponse> readDoctors() {
        return doctorService.readDoctors();
    }

    @Operation(summary = "Отримати інформацію про конкретного лікаря", description = "Повертає детальну інформацію про лікаря за вказаним ID.")
    @GetMapping("/{id}")
    public DoctorResponse readDoctor(@PathVariable String id) {
        return doctorService.readDoctor(id);
    }

    @Operation(summary = "Видалити лікаря", description = "Видаляє лікаря з бази даних за вказаним ID.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteDoctor(@PathVariable String id) {
        doctorService.deleteDoctor(id);
    }

    @Operation(summary = "Оновити інформацію про лікаря", description = "Оновлює основну інформацію про лікаря.")
    @PutMapping
    public DoctorResponse updateDoctor(@RequestPart("id") String id, @RequestPart("doctor") String doctorString) {
        ObjectMapper objectMapper = new ObjectMapper();
        DoctorRequest request = null;
        try{
            request = objectMapper.readValue(doctorString, DoctorRequest.class);
        }catch (JsonProcessingException ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        return doctorService.updateDoctor(id, request);
    }

    @Operation(summary = "Оновити зображення лікаря", description = "Оновлює фото лікаря за його ID.")
    @PostMapping("/update-image")
    public DoctorResponse updateDoctorImage(@RequestPart("id") String id, @RequestPart("file") MultipartFile file) {
        /*ObjectMapper objectMapper = new ObjectMapper();
        String _id = null;
        try{
            _id = objectMapper.readValue(id, String.class);
        }catch (JsonProcessingException ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }*/
        return doctorService.updateDoctorAvatar(id, file);
    }
}

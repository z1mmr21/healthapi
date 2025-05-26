package in.z1mmr.healthapi.controller;

import in.z1mmr.healthapi.request.MedicalRecordRequest;
import in.z1mmr.healthapi.request.MedicalRecordResponse;
import in.z1mmr.healthapi.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@AllArgsConstructor
@Tag(name = "Медичні записи", description = "Операції для керування медичними записами пацієнтів і лікарів")
public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;

    @Operation(
            summary = "Створити медичний запис",
            description = "Додає новий медичний запис для конкретного пацієнта та лікаря."
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'REGISTRAR', 'ADMIN')")
    public ResponseEntity<MedicalRecordResponse> createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequest request) {
        MedicalRecordResponse savedMedicalRecord = medicalRecordService.addMedicalRecord(request.getDoctorId(), request.getPatientId(), request);
        return ResponseEntity.ok(savedMedicalRecord);
    }

    @Operation(
            summary = "Отримати медичний запис за ID",
            description = "Повертає детальний медичний запис за його унікальним ідентифікатором."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'REGISTRAR', 'NURSE', 'ADMIN')")
    public ResponseEntity<MedicalRecordResponse> readMedicalRecordById(@PathVariable String id) {
        MedicalRecordResponse medicalRecord = medicalRecordService.getMedicalRecordById(id);
        return ResponseEntity.ok(medicalRecord);
    }

    @Operation(
            summary = "Отримати всі медичні записи",
            description = "Повертає список усіх медичних записів у системі."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'REGISTRAR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<MedicalRecordResponse>> getAllMedicalRecords() {
        List<MedicalRecordResponse> medicalRecords = medicalRecordService.getAllMedicalRecords();
        return ResponseEntity.ok(medicalRecords);
    }

    @Operation(
            summary = "Оновити медичний запис",
            description = "Оновлює існуючий медичний запис за ID."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<MedicalRecordResponse> updateMedicalRecord(
            @PathVariable String id,
            @Valid @RequestBody MedicalRecordRequest request) {
        MedicalRecordResponse updatedMedicalRecord = medicalRecordService.updateMedicalRecord(id, request);
        return ResponseEntity.ok(updatedMedicalRecord);
    }

    @Operation(
            summary = "Видалити медичний запис",
            description = "Видаляє медичний запис з бази даних за вказаним ID."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteMedicalRecord(@PathVariable String id) {
        medicalRecordService.deleteMedicalRecord(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Отримати записи за ID лікаря",
            description = "Повертає список медичних записів, пов’язаних із вказаним лікарем."
    )
    @GetMapping("/doctors/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'REGISTRAR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<MedicalRecordResponse>> readMedicalRecordsByDoctorId(@PathVariable String doctorId) {
        List<MedicalRecordResponse> medicalRecords = medicalRecordService.readMedicalRecordsByDoctorId(doctorId);
        return ResponseEntity.ok(medicalRecords);
    }

    @Operation(
            summary = "Отримати записи за ID пацієнта",
            description = "Повертає список медичних записів, пов’язаних із вказаним пацієнтом."
    )
    @GetMapping("/patients/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'REGISTRAR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<MedicalRecordResponse>> readMedicalRecordsByPatientId(@PathVariable String patientId) {
        List<MedicalRecordResponse> medicalRecords = medicalRecordService.readMedicalRecordsByPatientId(patientId);
        return ResponseEntity.ok(medicalRecords);
    }
}

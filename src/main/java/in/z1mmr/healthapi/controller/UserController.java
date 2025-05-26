package in.z1mmr.healthapi.controller;


import in.z1mmr.healthapi.entity.Role;
import in.z1mmr.healthapi.entity.UserEntity;
import in.z1mmr.healthapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Secured("ROLE_ADMIN")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get user by OAuth ID", description = "Fetch a user by their OAuth ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{oauthId}")
    public ResponseEntity<UserEntity> getUserByOauthId(@PathVariable String oauthId) {
        return userService.findUserByOauthId(oauthId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all users", description = "Fetch all registered users")
    @ApiResponse(responseCode = "200", description = "List of users")
    @GetMapping
    public List<UserEntity> getUsers() {
        return userService.findAllUsers();
    }

    @Operation(summary = "Update user role", description = "Обновляет роль пользователя по ID")
    @GetMapping("/change-role/{userId}/{role}")
    public ResponseEntity<UserEntity> updateUserRole(
            @PathVariable("userId") String userId,
            @PathVariable("role") String  role) {
        UserEntity updatedUser = userService.updateUserRole(userId, Role.valueOf(role.toUpperCase()));
        System.out.println(updatedUser);
        return ResponseEntity.ok(updatedUser);
    }
}

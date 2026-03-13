package com.heang.koriaibackend.domain.users.controller;

import com.heang.koriaibackend.common.api.ApiResponse;
import com.heang.koriaibackend.common.api.Code;
import com.heang.koriaibackend.domain.users.dto.CreateUserRequest;
import com.heang.koriaibackend.domain.users.dto.UpdatePreferredModelRequest;
import com.heang.koriaibackend.domain.users.dto.UpdateUserProfileRequest;
import com.heang.koriaibackend.domain.users.dto.UserResponse;
import com.heang.koriaibackend.domain.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody CreateUserRequest req) {
        if (userService.existsByEmail(req.email())) {
            return ApiResponse.error(Code.EMAIL_ALREADY_EXISTS, Map.of("message", "Email already registered"));
        }
        return ApiResponse.success(UserResponse.from(userService.create(req)));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> findById(@PathVariable Long id) {
        return userService.findById(id)
                .<ApiResponse<?>>map(user -> ApiResponse.success(UserResponse.from(user)))
                .orElseGet(() -> ApiResponse.error(Code.NOT_FOUND, Map.of("message", "User not found")));
    }

    @PutMapping("/{id}/profile")
    public ApiResponse<?> updateProfile(@PathVariable Long id, @Valid @RequestBody UpdateUserProfileRequest req) {
        return userService.updateProfile(id, req)
                .<ApiResponse<?>>map(user -> ApiResponse.success(UserResponse.from(user)))
                .orElseGet(() -> ApiResponse.error(Code.NOT_FOUND, Map.of("message", "User not found")));
    }

    @PutMapping("/{id}/preferred-model")
    public ApiResponse<?> updatePreferredModel(@PathVariable Long id, @Valid @RequestBody UpdatePreferredModelRequest req) {
        return userService.updatePreferredModel(id, req)
                .<ApiResponse<?>>map(user -> ApiResponse.success(UserResponse.from(user)))
                .orElseGet(() -> ApiResponse.error(Code.NOT_FOUND, Map.of("message", "User not found")));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Boolean>> delete(@PathVariable Long id) {
        boolean deleted = userService.deleteById(id);
        if (!deleted) {
            return ApiResponse.error(Code.NOT_FOUND, Map.of("deleted", false));
        }
        return ApiResponse.success(Map.of("deleted", true));
    }
}

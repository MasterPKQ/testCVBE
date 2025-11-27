package com.example.identity.controller;

import com.example.identity.dto.request.UserCreationRequest;
import com.example.identity.dto.request.UserUpdateRequest;
import com.example.identity.dto.response.UserResponse;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Set;
import java.util.Date;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;




@Slf4j
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserResponse createSampleUserResponse() {
        return UserResponse.builder()
                .id("U001")
                .username("vinhquach")
                .firstName("Vinh")
                .lastName("Quách")
                //.dob(LocalDate.of(2003, 5, 15))
                .roles(Set.of("ADMIN"))
                .build();
    }

    private UserCreationRequest createSampleUserCreationRequest() {
        return UserCreationRequest.builder()
                .username("vinhquach")
                .firstName("Vinh")
                .lastName("Quách")
                .dob(LocalDate.of(2003, 5, 15))
                .roles(Set.of("ADMIN"))
                .build();
    }

    private UserUpdateRequest createSampleUserUpdateRequest() {
        return UserUpdateRequest.builder()
                .firstName("QuyUpdate")
                .lastName("PhanUpdate")
                .dob(LocalDate.of(2003, 11, 24))
                .build();
    }
    @Test
    @DisplayName("Should create user successfully")
    public void createUser() throws Exception {
        // Given
        UserCreationRequest userRequest = createSampleUserCreationRequest();
        UserResponse userResponse = createSampleUserResponse();

        when(userService.createUser(any(UserCreationRequest.class))).thenReturn(userResponse);

        // WHEN/THEN
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.id").value("U001"))
                .andExpect(jsonPath("$.result.username").value("vinhquach"))
                .andExpect(jsonPath("$.result.firstName").value("Vinh"))
                .andExpect(jsonPath("$.result.dob").value("2003-05-15"))
                .andExpect(jsonPath("$.result.roles").value("ADMIN"))
                .andExpect(jsonPath("code").value("1000"));
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    public void getUserById_whenUserExists() throws Exception {
        // Given
        String userId = "U001";
        UserResponse userResponse = createSampleUserResponse();
        when(userService.getUser(userId)).thenReturn(userResponse);

        // When Then

        mockMvc.perform(get("/users/{userId}", userId))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.id").value(userId))
                .andExpect(jsonPath("$.result.username").value("vinhquach"));
    }

    @Test
    @DisplayName("Should return 500 when user not found (as per service logic)")
    public void getUserById_whenUserNotFound() throws Exception {
        // Given
        String nonExistentUserId = "U999";

        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        when(userService.getUser(nonExistentUserId))
                .thenThrow(new RuntimeException("User not found"));

        // When Then

        mockMvc.perform(get("/users/{userId}", nonExistentUserId))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message").value(errorCode.getMessage()));
    }

    @Test
    @DisplayName("Should update user successfully")
    public void updateUser() throws Exception {
        // Given
        String userId = "U001";
        UserUpdateRequest updateRequest = createSampleUserUpdateRequest();

        LocalDate localDate = LocalDate.of(2003, 11, 24);
        Date updatedDob = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        UserResponse updatedUserResponse = UserResponse.builder()
                .id(userId)
                .username("QuyPhan")
                .firstName("QuyUpdate")
                .lastName("PhanUpdate")
                .dob(LocalDate.of(2003, 11, 24)).roles(Set.of("ADMIN"))
                .build();

        when(userService.updateUser(eq(userId), any(UserUpdateRequest.class)))
                .thenReturn(updatedUserResponse);

        // When Then
        mockMvc.perform(put("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.id").value(userId))
                .andExpect(jsonPath("$.result.firstName").value("QuyUpdate"))
                .andExpect(jsonPath("$.result.lastName").value("PhanUpdate"));
    }
}

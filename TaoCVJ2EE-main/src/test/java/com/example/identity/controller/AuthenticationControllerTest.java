package com.example.identity.controller;

import com.example.identity.dto.request.AuthenticationRequest;
import com.example.identity.dto.request.IntrospectRequest;
import com.example.identity.dto.request.LogoutRequest;
import com.example.identity.dto.request.RefreshTokenRequest;
import com.example.identity.dto.response.AuthenticationResponse;
import com.example.identity.dto.response.IntrospectResponse;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthenticationController.class)

@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;


    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Should login successfully with correct credentials")
    void authenticate_loginSuccess() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest("testuser", "password123");
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("dummy.jwt.token")
                .authenticated(true)
                .build();


        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenReturn(response);

        // When
            mockMvc.perform(post("/log-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Then (Thì)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.token").value("dummy.jwt.token"))
                .andExpect(jsonPath("$.result.authenticated").value(true));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized with incorrect credentials")
    void authenticate_loginFailed() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest("testuser", "wrongpassword");
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;


         when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new AppException(errorCode));

        // When
        mockMvc.perform(post("/log-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Then
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message").value(errorCode.getMessage()));
    }

    @Test
    @DisplayName("Should introspect a valid token")
    void introspect_validToken() throws Exception {
        // Given
        IntrospectRequest request = new IntrospectRequest("valid.token");
        IntrospectResponse response = IntrospectResponse.builder().valid(true).build();

        when(authenticationService.introspect(any(IntrospectRequest.class)))
                .thenReturn(response);

        // When/Then
        mockMvc.perform(post("/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.valid").value(true));
    }

    @Test
    @DisplayName("Should introspect an invalid token")
    void introspect_invalidToken() throws Exception {
        // Given
        IntrospectRequest request = new IntrospectRequest("invalid.token");
        IntrospectResponse response = IntrospectResponse.builder().valid(false).build();

        when(authenticationService.introspect(any(IntrospectRequest.class)))
                .thenReturn(response);

        // When/Then
        mockMvc.perform(post("/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.valid").value(false));
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void refreshToken_success() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("valid.refresh.token");
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("new.jwt.token")
                .authenticated(true)
                .build();

        when(authenticationService.refreshToken(any(RefreshTokenRequest.class)))
                .thenReturn(response);

        // When/Then
        mockMvc.perform(post("/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.token").value("new.jwt.token"));
    }

    @Test
    @DisplayName("Should logout successfully")
    void logout_success() throws Exception {
        // Given
        LogoutRequest request = new LogoutRequest("token.to.invalidate");


        doNothing().when(authenticationService).logout(any(LogoutRequest.class));
        // When/Then
        mockMvc.perform(post("/log-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }
}
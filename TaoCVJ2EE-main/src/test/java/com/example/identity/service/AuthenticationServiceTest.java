package com.example.identity.service;

import com.example.identity.dto.request.AuthenticationRequest;
import com.example.identity.dto.request.IntrospectRequest;
import com.example.identity.dto.request.LogoutRequest;
import com.example.identity.dto.request.RefreshTokenRequest;
import com.example.identity.dto.response.AuthenticationResponse;
import com.example.identity.dto.response.IntrospectResponse;
import com.example.identity.entity.InvalidatedToken;
import com.example.identity.entity.User;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.repository.InvalidatedTokenRepository;
import com.example.identity.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private InvalidatedTokenRepository tokenRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    // Dữ liệu mẫu
    private User user;
    private AuthenticationRequest authRequest_Success;
    private AuthenticationRequest authRequest_Fail;
    private final String testPassword = "password123";
    private String testPasswordHash;
    private final String signerKey = "e4b7c29a8f0d3d7f5c12a9e74d53b8f6a7c4e1b2d9f0c3a87e5d1b9f6a2c4d8e";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY", signerKey);
        ReflectionTestUtils.setField(authenticationService, "VALIDATION_DURATION", 3600L);
        ReflectionTestUtils.setField(authenticationService, "REFRESHABLE_DURATION", 36000L);

        // Tạo hash mật khẩu thật
        testPasswordHash = passwordEncoder.encode(testPassword);

        user = User.builder()
                .id("user-123")
                .username("testuser")
                .password(testPasswordHash)
                .roles(Set.of("USER"))
                .build();

        // Request đăng nhập đúng
        authRequest_Success = AuthenticationRequest.builder()
                .username("testuser")
                .password(testPassword)
                .build();

        // Request đăng nhập sai
        authRequest_Fail = AuthenticationRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();
    }

    @Test
    @DisplayName("Test authenticate - Đăng nhập thành công")
    void authenticate_success() {
        // Given
        // Ra lệnh: Khi repo.findByUsername -> trả về user (với hash thật)
        when(userRepository.findByUsername(authRequest_Success.getUsername()))
                .thenReturn(Optional.of(user));

        // When
        AuthenticationResponse response = authenticationService.authenticate(authRequest_Success);

        // Then
        assertThat(response.isAuthenticated()).isTrue();
        assertThat(response.getToken()).isNotNull();
    }

    @Test
    @DisplayName("Test authenticate - Lỗi sai mật khẩu (UNAUTHENTICATED)")
    void authenticate_wrongPassword() {
        // Given
        // Ra lệnh: Khi repo.findByUsername -> vẫn trả về user (với hash thật)
        when(userRepository.findByUsername(authRequest_Fail.getUsername()))
                .thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(authRequest_Fail))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHENTICATED);
    }

    @Test
    @DisplayName("Test authenticate - Lỗi USER_NOT_EXISTED")
    void authenticate_userNotFound() {
        // Given
        // Ra lệnh: Khi repo.findByUsername -> trả về rỗng
        when(userRepository.findByUsername(authRequest_Success.getUsername()))
                .thenReturn(Optional.empty()); //

        // When Then
        assertThatThrownBy(() -> authenticationService.authenticate(authRequest_Success))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTED);
    }

    @Test
    @DisplayName("Test introspect - Token hợp lệ")
    void introspect_validToken() throws ParseException, JOSEException {
        // Given
        String token = authenticationService.generateToken(user);
        IntrospectRequest request = IntrospectRequest.builder().token(token).build();

        // 2. Ra lệnh: Khi repo.existsById (kiểm tra token có bị logout) -> trả về false
        when(tokenRepository.existsById(anyString())).thenReturn(false);

        // When
        IntrospectResponse response = authenticationService.introspect(request);

        // Then
        assertThat(response.isValid()).isTrue();
    }

    @Test
    @DisplayName("Test introspect - Token đã bị logout (invalidated)")
    void introspect_invalidatedToken() throws ParseException, JOSEException {
        // Given
        String token = authenticationService.generateToken(user);
        IntrospectRequest request = IntrospectRequest.builder().token(token).build();

        // Ra lệnh: Khi repo.existsById -> TRẢ VỀ TRUE (token đã bị logout)
        when(tokenRepository.existsById(anyString())).thenReturn(true);

        // When Then
        IntrospectResponse response = authenticationService.introspect(request);
        assertThat(response.isValid()).isFalse();
    }

    @Test
    @DisplayName("Test logout - Đăng xuất thành công")
    void logout_success() throws ParseException, JOSEException {
        // Given
        String token = authenticationService.generateToken(user);
        LogoutRequest request = new LogoutRequest(token);

        // Ra lệnh: Khi repo.existsById (kiểm tra token có bị logout) -> trả về false
        when(tokenRepository.existsById(anyString())).thenReturn(false);

        // When
        authenticationService.logout(request);

        // Then

        ArgumentCaptor<InvalidatedToken> captor = ArgumentCaptor.forClass(InvalidatedToken.class);
        verify(tokenRepository).save(captor.capture());

        assertThat(captor.getValue()).isNotNull();
        assertThat(captor.getValue().getId()).isNotNull();
    }

    @Test
    @DisplayName("Test refreshToken - Làm mới token thành công")
    void refreshToken_success() throws ParseException, JOSEException {
        // Given
        String oldToken = authenticationService.generateToken(user);
        RefreshTokenRequest request = new RefreshTokenRequest(oldToken);

        // Ra lệnh: Khi repo.existsById (kiểm tra token cũ) -> trả về false (hợp lệ)
        when(tokenRepository.existsById(anyString())).thenReturn(false);
        // Ra lệnh: Khi repo.findByUsername -> trả về user
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        // When
        AuthenticationResponse response = authenticationService.refreshToken(request);

        // Then
        // 1. Token mới được tạo ra
        assertThat(response.isAuthenticated()).isTrue();
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getToken()).isNotEqualTo(oldToken);

        // 2. Token cũ đã bị lưu vào blacklist
        ArgumentCaptor<InvalidatedToken> captor = ArgumentCaptor.forClass(InvalidatedToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue()).isNotNull();
    }
}
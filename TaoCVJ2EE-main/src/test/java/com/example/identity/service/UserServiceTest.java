package com.example.identity.service;

import com.example.identity.dto.request.UserCreationRequest;
import com.example.identity.dto.response.UserResponse;
import com.example.identity.entity.User;
import com.example.identity.enums.Role;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.mapper.UserMapper;
import com.example.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Set;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {


    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;


    @InjectMocks
    private UserService userService;


    private UserCreationRequest userCreationRequest;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        // Dữ liệu đầu vào
        userCreationRequest = UserCreationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .dob(LocalDate.of(2000, 1, 1))
                .build();

        // Dữ liệu  mà mapper sẽ trả về
        user = User.builder()
                .id("12345")
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        // Dữ liệu mong muốn ở đầu ra
        userResponse = UserResponse.builder()
                .id("12345")
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .roles(Set.of(Role.USER.name()))
                .build();
    }

    @Test
    @DisplayName("Test createUser - Trường hợp thành công (Happy Path)")
    void createUser_success() {
        // Given

        // 1. Khi repo kiểm tra username -> không tồn tại (return false)
        when(userRepository.existsByUsername(userCreationRequest.getUsername()))
                .thenReturn(false);

        // 2. Khi repo kiểm tra email -> không tồn tại (return false)
        when(userRepository.existsByEmail(userCreationRequest.getEmail()))
                .thenReturn(false);

        // 3. Khi mapper chuyển đổi request -> trả về User entity
        when(userMapper.toUser(userCreationRequest)).thenReturn(user);

        // 4. Khi mã hóa password -> trả về một chuỗi đã mã hóa
        when(passwordEncoder.encode(userCreationRequest.getPassword()))
                .thenReturn("encodedPassword123");

        // 5. Khi repo lưu User -> trả về User (đã được lưu)
        when(userRepository.save(any(User.class))).thenReturn(user);

        // 6. Khi mapper chuyển đổi User (entity) sang Response -> trả về UserResponse
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // When
        UserResponse actualResponse = userService.createUser(userCreationRequest);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo("12345");
        assertThat(actualResponse.getUsername()).isEqualTo("testuser");
        assertThat(actualResponse.getRoles()).contains(Role.USER.name());



        // 1. Tạo một ArgumentCaptor cho đối tượng User
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // 2. Kiểm tra xem hàm save đã được gọi, và "bắt" lấy đối tượng User
        verify(userRepository).save(userCaptor.capture());

        // 3. Lấy đối tượng User đã bị bắt
        User savedUser = userCaptor.getValue();

        // 4. Kiểm tra xem password của user *bị bắt* có đúng là chuỗi đã mã hóa không
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword123");
        assertThat(savedUser.getRoles()).contains(Role.USER.name());
    }

    @Test
    @DisplayName("Test createUser - Lỗi khi Username đã tồn tại")
    void createUser_usernameExisted() {
        // Given

        when(userRepository.existsByUsername(userCreationRequest.getUsername()))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(userCreationRequest))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_EXISTED);

        // Đảm bảo rằng các hàm sau KHÔNG BAO GIỜ được gọi
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userMapper, never()).toUser(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test createUser - Lỗi khi Email đã tồn tại")
    void createUser_emailExisted() {
        // Given
        // 1. Khi repo kiểm tra username -> không tồn tại
        when(userRepository.existsByUsername(userCreationRequest.getUsername()))
                .thenReturn(false);
        // 2. Khi repo kiểm tra email -> BÁO LÀ ĐÃ TỒN TẠI
        when(userRepository.existsByEmail(userCreationRequest.getEmail()))
                .thenReturn(true);

        // When  Then
        assertThatThrownBy(() -> userService.createUser(userCreationRequest))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_EXISTED);


        verify(userRepository, never()).save(any());
    }
}
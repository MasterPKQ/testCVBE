package com.example.identity.service;

import com.example.identity.OAuth2.CustomOAuth2User;
import com.example.identity.entity.User;
import com.example.identity.enums.Provider;
import com.example.identity.enums.Role;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest; 
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    UserRepository userRepository;
    AuthenticationService authenticationService;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        //  Xác định nhà cung cấp
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Provider provider;
        try {
            provider = Provider.valueOf(registrationId.toUpperCase()); 
        } catch (IllegalArgumentException e) {
            throw new OAuth2AuthenticationException("Provider không được hỗ trợ: " + registrationId);
        }

        //  Khai báo biến chung
        String email = null;
        String name = oAuth2User.getAttribute("name");
        String avatar = null;
        Date birthday = null;

        //  Xử lý thông tin riêng cho từng nhà cung cấp
        if (provider == Provider.FACEBOOK) {
           
            Map<String, Object> picture = (Map<String, Object>) oAuth2User.getAttribute("picture");
            if (picture != null && picture.get("data") instanceof Map data) {
                avatar = (String) data.get("url");
            }
            email = oAuth2User.getAttribute("email");
            birthday = oAuth2User.getAttribute("user_birthday");
            log.info("Facebook Login - email: {}, name: {}, birthday: {}, avatar: {}", email, name, birthday, avatar);
            

        } else if (provider == Provider.GOOGLE) {
            
            email = oAuth2User.getAttribute("email");
            avatar = oAuth2User.getAttribute("picture");
            log.info("Google Login - email: {}, name: {}, avatar: {}", email, name, avatar);
           
        }
        
        
        //  XỬ LÝ LOGIC CHUNG 
        

        if (email == null || email.isEmpty()) {
            throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
        }

        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        User user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();

            // So sánh động nhà cung cấp
            if (user.getProvider() != provider) {
                log.warn("User email {} đã tồn tại với nhà cung cấp {}, nhưng đang cố đăng nhập bằng {}",
                        email, user.getProvider(), provider);
                throw new AppException(ErrorCode.USER_EXISTED);
            }

            log.info("User đã tồn tại, đăng nhập lại qua {}: {}", provider, user.getEmail());
        } else {
            // Tạo user mới với nhà cung cấp động
            var roles = new HashSet<String>();
            roles.add(Role.USER.name());
            user = User.builder()
                    .username(name) 
                    .firstName(name)
                    .lastName("")
                    .email(email)
                    .dob(birthday) 
                    .avatar(avatar)
                    .provider(provider) 
                    .roles(roles)
                    .password(UUID.randomUUID().toString())
                    .build();

            userRepository.save(user);
            log.info("Tạo mới user từ {}: {}", provider, user.getEmail());
        }

        // Sinh token JWT
        String token = authenticationService.generateToken(user);

        // Trả về CustomOAuth2User có token
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(oAuth2User);
        customOAuth2User.setToken(token);

        log.info("JWT token created for {} login: {}", provider, token);

        return customOAuth2User;
    }

}
//package com.example.identity.OAuth2;
//
//import com.example.identity.dto.request.ApiResponse;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//@Slf4j
//public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        Authentication authentication)
//            throws IOException, ServletException {
//
//        CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();
//        String token = customUser.getToken();
//
//        log.info("Facebook login success - returning JWT as JSON");
//
//        response.setContentType("application/json;charset=UTF-8");
//        response.setStatus(HttpServletResponse.SC_OK);
//
//        ApiResponse<String> api = ApiResponse.<String>builder()
//                .message("Facebook login success")
//                .result(token)
//                .build();
//
//        // ghi JSON ra body
//        new ObjectMapper().writeValue(response.getWriter(), api);
//    }
//}

// Fix login with facebook
package com.example.identity.OAuth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // Thêm import này
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy; // Thêm import này
import org.springframework.security.web.RedirectStrategy; // Thêm import này
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder; // Thêm import này

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    // Tiêm (inject) danh sách URI từ file application.properties/.yml
    @Value("${app.oauth2.authorized-redirect-uris}")
    private List<String> authorizedRedirectUris;

    // Khởi tạo chiến lược redirect
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // 1. Lấy token từ custom user
        CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();
        String token = customUser.getToken();

        // 2. Kiểm tra xem URI đã được cấu hình chưa
        if (authorizedRedirectUris == null || authorizedRedirectUris.isEmpty()) {
            log.error("Chưa cấu hình 'app.oauth2.authorized-redirect-uris' trong application.properties/.yml");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi cấu hình server.");
            return;
        }

        // 3. Lấy URI đầu tiên trong danh sách (ví dụ: http://localhost:3000/auth/callback)
        String targetUrl = authorizedRedirectUris.get(0);

        // 4. Xây dựng URL đích, gắn token vào làm query parameter
        // Kết quả sẽ là: http://localhost:3000/auth/callback?token=...JWT...
        String redirectUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build().toUriString();

        if (response.isCommitted()) {
            log.debug("Response đã được committed. Không thể redirect tới {}", redirectUrl);
            return;
        }

        // 5. Thực hiện redirect
        log.info("Đăng nhập OAuth2 thành công. Đang chuyển hướng tới: {}", redirectUrl);
        redirectStrategy.sendRedirect(request, response, redirectUrl);
    }
}
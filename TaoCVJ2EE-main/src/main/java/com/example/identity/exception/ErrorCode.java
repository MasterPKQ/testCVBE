package com.example.identity.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error" , HttpStatus.INTERNAL_SERVER_ERROR),
    UNSIGNED_EXCEPTION(9999, "Unsigned exception", HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed" , HttpStatus.CONFLICT),
    USERNAME_INVALID(1003, "Username must be at least 3 characters" , HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least 8 characters" , HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed" , HttpStatus.NOT_FOUND),
    INVALID_USERNAME_OR_PASSWORD(1005, "Invalid username or password" , HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "Unauthenticated" , HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have Permission" , HttpStatus.FORBIDDEN),
    EMAIL_NOT_FOUND(1008, "Email not found" , HttpStatus.NOT_FOUND),

    // Template
    TEMPLATE_NOT_FOUND(1009, "Template not found" , HttpStatus.NOT_FOUND),

    // CV
    CV_NOT_FOUND(1010, "CV not found" , HttpStatus.NOT_FOUND),

    //SECTION
    SECTION_NOT_FOUND(1011, "Section not found" , HttpStatus.NOT_FOUND),
    
    // Template System
    TEMPLATE_SAVE_FAILED(1012, "Failed to save template file" , HttpStatus.INTERNAL_SERVER_ERROR),
    TEMPLATE_RENDER_FAILED(1013, "Failed to render template" , HttpStatus.INTERNAL_SERVER_ERROR),
    TEMPLATE_FILE_NOT_FOUND(1014, "Template file not found" , HttpStatus.NOT_FOUND),
    FEATURE_NOT_IMPLEMENTED(1015, "Feature not yet implemented" , HttpStatus.NOT_IMPLEMENTED),
    ;


    int code;
    String message;
    HttpStatusCode statusCode;

}
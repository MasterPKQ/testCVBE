package com.example.identity.service;

import com.example.identity.dto.request.CVRequest;
import com.example.identity.dto.response.CVResponse;
import com.example.identity.entity.CV;
import com.example.identity.entity.CVSection;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.mapper.CVMapper;
import com.example.identity.repository.CVRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CVService {
    CVRepository cvRepository;
    CVMapper cvMapper;
    UserService userService;
    TemplateService templateService;
    RenderService renderService;
    TemplateRenderingService templateRenderingService;

    public List<CVResponse> getAllMyCV() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return cvRepository.findAllByUserUsername(name).stream()
                .map(cvMapper::toCVResponse)
                .toList();
    }

    public CVResponse getCVById(Long id) {
        return getAllMyCV().stream()
                .filter(cv -> cv.getId().equals(id))
                .findFirst().orElseThrow(() -> new AppException(ErrorCode.CV_NOT_FOUND));
    }

    public CVResponse createCV(CVRequest cvRequest) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        // khởi tạo CV
        CV cv = cvMapper.toCV(cvRequest);
        System.out.println(cv.toString());

        // Lấy user
        cv.setUser(userService.getUserByUsername(name));

        // Lấy template
        if (cvRequest.getTemplateId() != null) {
            cv.setTemplate(templateService.getTemplateById(cvRequest.getTemplateId()));
        }

        // gắn section
        for (CVSection section : cv.getSections()) {
            section.setCv(cv);
        }


        return cvMapper.toCVResponse(cvRepository.save(cv));
    }

    public CVResponse updateCV(Long cvId, CVRequest cvRequest) {
        CV cv = cvRepository.findById(cvId).orElseThrow(() -> new AppException(ErrorCode.CV_NOT_FOUND));
        cvMapper.updateCV(cv, cvRequest);
        return cvMapper.toCVResponse(cvRepository.save(cv));
    }

    public Boolean deleteCV(Long cvId) {
        cvRepository.deleteById(cvId);
        return true;
    }

    public CVResponse duplicateCV(Long cvId) {
        CV cvOld = cvRepository.findById(cvId).orElseThrow(() -> new AppException(ErrorCode.CV_NOT_FOUND));
        CV cvNew = new CV();
        cvMapper.duplicateCV(cvNew, cvOld);
        return cvMapper.toCVResponse(cvRepository.save(cvNew));
    }

    /**
     * Render CV as HTML using Thymeleaf template
     */
    public String renderCVAsHtml(Long cvId) {
        CV cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new AppException(ErrorCode.CV_NOT_FOUND));
        
        if (cv.getTemplate() == null) {
            throw new AppException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        
        return templateRenderingService.renderCV(cv, cv.getTemplate());
    }
}

package com.example.identity.service;

import com.example.identity.entity.CV;
import com.example.identity.entity.Template;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class TemplateRenderingService {

    final TemplateEngine templateEngine;
    final ObjectMapper objectMapper;

    @Value("${template.storage.path:src/main/resources/templates/cv}")
    String templateStoragePath;

    /**
     * Render CV với template
     * @param cv - CV entity chứa cvData
     * @param template - Template entity chứa compiledFilePath
     * @return HTML string đã render
     */
    public String renderCV(CV cv, Template template) {
        log.info("Rendering CV {} with template {}", cv.getId(), template.getId());

        // Step 1: Prepare Thymeleaf context
        Context context = new Context();
        
        // Step 2: Add CV data to context
        Map<String, Object> cvDataMap = convertJsonNodeToMap(cv.getCvData());
        context.setVariable("cvData", cvDataMap);
        
        // Step 3: Add customization to context
        if (cv.getCustomization() != null) {
            Map<String, Object> customizationMap = convertJsonNodeToMap(cv.getCustomization());
            context.setVariable("customization", customizationMap);
        }
        
        // Step 4: Add template config to context
        if (template.getTemplateConfig() != null) {
            Map<String, Object> templateConfigMap = convertJsonNodeToMap(template.getTemplateConfig());
            context.setVariable("templateConfig", templateConfigMap);
        }

        // Step 5: Get template name (remove .html extension for Thymeleaf)
        String templateName = getTemplateNameForThymeleaf(template.getCompiledFilePath());

        // Step 6: Render with Thymeleaf
        try {
            String renderedHtml = templateEngine.process(templateName, context);
            log.info("CV rendered successfully");
            return renderedHtml;
        } catch (Exception e) {
            log.error("Failed to render CV {} with template {}", cv.getId(), template.getId(), e);
            throw new AppException(ErrorCode.TEMPLATE_RENDER_FAILED);
        }
    }

    /**
     * Render CV preview với sample data
     * Used for template preview in gallery
     */
    public String renderTemplatePreview(Template template) {
        log.info("Rendering template preview: {}", template.getId());

        Context context = new Context();
        
        // Add sample CV data
        Map<String, Object> sampleData = createSampleCVData();
        context.setVariable("cvData", sampleData);
        
        // Add template config
        if (template.getTemplateConfig() != null) {
            Map<String, Object> templateConfigMap = convertJsonNodeToMap(template.getTemplateConfig());
            context.setVariable("templateConfig", templateConfigMap);
        }

        String templateName = getTemplateNameForThymeleaf(template.getCompiledFilePath());

        try {
            String renderedHtml = templateEngine.process(templateName, context);
            log.info("Template preview rendered successfully");
            return renderedHtml;
        } catch (Exception e) {
            log.error("Failed to render template preview {}", template.getId(), e);
            throw new AppException(ErrorCode.TEMPLATE_RENDER_FAILED);
        }
    }

    /**
     * Export CV as PDF (placeholder for future implementation)
     */
    public byte[] exportCVAsPdf(CV cv, Template template) {
        String html = renderCV(cv, template);
        // TODO: Implement HTML to PDF conversion
        // Use libraries like Flying Saucer, OpenPDF, or iText
        log.warn("PDF export not yet implemented");
        throw new AppException(ErrorCode.FEATURE_NOT_IMPLEMENTED);
    }

    // ================= HELPER METHODS =================

    /**
     * Convert JsonNode to Map for Thymeleaf
     */
    private Map<String, Object> convertJsonNodeToMap(JsonNode jsonNode) {
        try {
            return objectMapper.convertValue(jsonNode, Map.class);
        } catch (Exception e) {
            log.error("Failed to convert JsonNode to Map", e);
            return new HashMap<>();
        }
    }

    /**
     * Get template name for Thymeleaf
     * Thymeleaf expects "cv/template_name" without .html extension
     */
    private String getTemplateNameForThymeleaf(String compiledFilePath) {
        if (compiledFilePath == null) {
            throw new AppException(ErrorCode.TEMPLATE_FILE_NOT_FOUND);
        }
        
        // Remove .html extension if exists
        String templateName = compiledFilePath.replaceAll("\\.html$", "");
        
        // Prefix with cv/ to match Thymeleaf template resolver
        return "cv/" + templateName;
    }

    /**
     * Create sample CV data for template preview
     */
    private Map<String, Object> createSampleCVData() {
        Map<String, Object> data = new HashMap<>();
        
        // User info
        Map<String, Object> user = new HashMap<>();
        user.put("name", "John Doe");
        user.put("title", "Senior Software Engineer");
        user.put("email", "john.doe@example.com");
        user.put("phone", "+1 234 567 8900");
        user.put("location", "San Francisco, CA");
        user.put("linkedin", "linkedin.com/in/johndoe");
        user.put("github", "github.com/johndoe");
        data.put("user", user);
        
        // Summary
        data.put("summary", "Experienced software engineer with 5+ years in full-stack development...");
        
        // Skills (array example)
        data.put("skills", new String[]{"Java", "Spring Boot", "React", "PostgreSQL", "Docker"});
        
        // Experiences (array of objects)
        Map<String, Object> exp1 = new HashMap<>();
        exp1.put("position", "Senior Software Engineer");
        exp1.put("company", "Tech Corp");
        exp1.put("duration", "2020 - Present");
        exp1.put("description", "Led development of microservices architecture...");
        data.put("experiences", new Object[]{exp1});
        
        // Education
        Map<String, Object> edu1 = new HashMap<>();
        edu1.put("degree", "Bachelor of Computer Science");
        edu1.put("school", "University of Technology");
        edu1.put("year", "2015 - 2019");
        data.put("education", new Object[]{edu1});
        
        return data;
    }

    /**
     * Validate template file exists
     */
    public boolean validateTemplateFile(String compiledFilePath) {
        if (compiledFilePath == null) {
            return false;
        }
        
        Path filePath = Paths.get(templateStoragePath).resolve(compiledFilePath);
        return Files.exists(filePath);
    }
}

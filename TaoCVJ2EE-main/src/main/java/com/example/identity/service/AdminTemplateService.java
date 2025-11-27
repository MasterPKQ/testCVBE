package com.example.identity.service;

import com.example.identity.dto.request.AdminTemplateUploadRequest;
import com.example.identity.dto.response.TemplateResponse;
import com.example.identity.entity.Template;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.mapper.TemplateMapper;
import com.example.identity.repository.TemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class AdminTemplateService {
    
    final TemplateRepository templateRepository;
    final TemplateMapper templateMapper;
    final TemplateParserService templateParserService;
    final ObjectMapper objectMapper;

    @Value("${template.storage.path:src/main/resources/templates/cv}")
    String templateStoragePath;

    /**
     * Tạo template mới từ HTML do Admin upload
     * Flow:
     * 1. Clean & validate HTML
     * 2. Convert HTML -> Thymeleaf
     * 3. Save Thymeleaf file to disk
     * 4. Save metadata to database
     */
    @Transactional
    public TemplateResponse createTemplateFromHtml(AdminTemplateUploadRequest request) {
        log.info("Creating template from HTML: {}", request.getName());

        // Step 1: Clean HTML
        String cleanedHtml = templateParserService.cleanHtml(request.getBaseHtml());

        // Step 2: Convert to Thymeleaf
        String thymeleafContent = templateParserService.parseHtmlToThymeleaf(cleanedHtml);

        // Step 3: Save file to disk
        String fileName = generateTemplateFileName(request.getName());
        String filePath = saveTemplateFile(fileName, thymeleafContent);

        // Step 4: Create Template entity
        Template template = Template.builder()
                .name(request.getName())
                .category(request.getCategory())
                .style(request.getStyle())
                .thumbnailUrl(request.getThumbnailUrl())
                .templateConfig(request.getTemplateConfig())
                .sectionsDefinition(request.getSectionsDefinition())
                .baseHtml(cleanedHtml)
                .compiledFilePath(fileName) // Store relative path
                .isPremium(request.getIsPremium() != null ? request.getIsPremium() : false)
                .isActive(true)
                .version(1)
                .createdBy(request.getCreatedBy())
                .build();

        Template saved = templateRepository.save(template);
        log.info("Template created successfully with ID: {}", saved.getId());

        return templateMapper.toDtoRes(saved);
    }

    /**
     * Update template HTML
     */
    @Transactional
    public TemplateResponse updateTemplateHtml(Long id, AdminTemplateUploadRequest request) {
        log.info("Updating template HTML: {}", id);

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND));

        // Clean and convert HTML
        String cleanedHtml = templateParserService.cleanHtml(request.getBaseHtml());
        String thymeleafContent = templateParserService.parseHtmlToThymeleaf(cleanedHtml);

        // Delete old file if exists
        if (template.getCompiledFilePath() != null) {
            deleteTemplateFile(template.getCompiledFilePath());
        }

        // Save new file
        String fileName = generateTemplateFileName(request.getName());
        saveTemplateFile(fileName, thymeleafContent);

        // Update entity
        template.setName(request.getName());
        template.setCategory(request.getCategory());
        template.setStyle(request.getStyle());
        template.setThumbnailUrl(request.getThumbnailUrl());
        template.setTemplateConfig(request.getTemplateConfig());
        template.setSectionsDefinition(request.getSectionsDefinition());
        template.setBaseHtml(cleanedHtml);
        template.setCompiledFilePath(fileName);
        template.setVersion(template.getVersion() + 1);

        Template updated = templateRepository.save(template);
        log.info("Template updated successfully: {}", id);

        return templateMapper.toDtoRes(updated);
    }

    /**
     * Test compile HTML to Thymeleaf without saving
     */
    public String testHtmlToThymeleaf(String html) {
        String cleanedHtml = templateParserService.cleanHtml(html);
        return templateParserService.parseHtmlToThymeleaf(cleanedHtml);
    }

    /**
     * Delete template
     */
    @Transactional
    public void deleteTemplate(Long id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND));

        // Delete file
        if (template.getCompiledFilePath() != null) {
            deleteTemplateFile(template.getCompiledFilePath());
        }

        templateRepository.delete(template);
        log.info("Template deleted: {}", id);
    }

    /**
     * Toggle active status
     */
    @Transactional
    public TemplateResponse toggleActiveStatus(Long id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND));

        template.setIsActive(!template.getIsActive());
        Template updated = templateRepository.save(template);

        log.info("Template {} active status changed to: {}", id, updated.getIsActive());
        return templateMapper.toDtoRes(updated);
    }

    // ================= HELPER METHODS =================

    /**
     * Generate unique filename for template
     * Format: template_<name>_<timestamp>.html
     */
    private String generateTemplateFileName(String templateName) {
        String sanitized = templateName
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_");
        
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        return String.format("template_%s_%s.html", sanitized, timestamp);
    }

    /**
     * Save Thymeleaf template to disk
     */
    private String saveTemplateFile(String fileName, String content) {
        try {
            Path directory = Paths.get(templateStoragePath);
            
            // Create directory if not exists
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
                log.info("Created template directory: {}", directory);
            }

            Path filePath = directory.resolve(fileName);
            Files.writeString(filePath, content);
            
            log.info("Template file saved: {}", filePath);
            return fileName; // Return relative path
            
        } catch (IOException e) {
            log.error("Failed to save template file: {}", fileName, e);
            throw new AppException(ErrorCode.TEMPLATE_SAVE_FAILED);
        }
    }

    /**
     * Delete template file from disk
     */
    private void deleteTemplateFile(String fileName) {
        try {
            Path filePath = Paths.get(templateStoragePath).resolve(fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Template file deleted: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete template file: {}", fileName, e);
            // Don't throw exception, just log warning
        }
    }
}

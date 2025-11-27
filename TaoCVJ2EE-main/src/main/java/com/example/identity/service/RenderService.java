package com.example.identity.service;

import com.example.identity.entity.CV;
import com.example.identity.entity.CVSection;
import com.example.identity.entity.Template;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RenderService {
    
    @Qualifier("cvTemplateEngine")
    private final SpringTemplateEngine templateEngine;
    
    private final ObjectMapper objectMapper;
    
    private final RedisTemplate<String, String> redisTemplate;
    
    /**
     * Render CV với data thật của user
     */
    public String renderCV(CV cv) {
        if (cv.getTemplate() == null) {
            throw new IllegalArgumentException("CV must have a template");
        }
        
        if (cv.getTemplate().getCompiledFilePath() == null) {
            throw new IllegalArgumentException("Template compiled file path is null");
        }
        
        // Step 1: Check cache
        String cacheKey = buildCacheKey(cv);
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache HIT for CV {}", cv.getId());
            return cached;
        }
        
        log.debug("Cache MISS for CV {}", cv.getId());
        
        // Step 2: Merge configs
        JsonNode mergedConfig = mergeConfigs(
            cv.getTemplate().getTemplateConfig(),
            cv.getCustomization()
        );
        
        // Step 3: Build model
        Map<String, Object> model = buildModelFromCV(cv, mergedConfig);
        
        // Step 4: Render
        try {
            Context context = new Context(Locale.getDefault(), model);
            String templateName = extractTemplateName(cv.getTemplate().getCompiledFilePath());
            String html = templateEngine.process(templateName, context);
            
            // Step 5: Cache result
            redisTemplate.opsForValue().set(cacheKey, html, Duration.ofMinutes(15));
            
            return html;
        } catch (Exception e) {
            log.error("Failed to render CV {}: {}", cv.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to render CV: " + e.getMessage(), e);
        }
    }
    
    /**
     * Render template với data trống (cho user xem structure)
     */
    public String renderEmptyTemplate(Template template) {
        if (template.getCompiledFilePath() == null) {
            throw new IllegalArgumentException("Template compiled file path is null");
        }
        
        try {
            Map<String, Object> emptyModel = new HashMap<>();
            emptyModel.put("sections", Collections.emptyList());
            emptyModel.put("config", objectMapper.convertValue(template.getTemplateConfig(), Map.class));
            
            Context context = new Context(Locale.getDefault(), emptyModel);
            String templateName = extractTemplateName(template.getCompiledFilePath());
            
            return templateEngine.process(templateName, context);
        } catch (Exception e) {
            log.error("Failed to render empty template {}: {}", template.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to render empty template: " + e.getMessage(), e);
        }
    }
    
    /**
     * Invalidate cache for specific CV
     */
    public void invalidateCVCache(Long cvId) {
        String pattern = "rendered:cv:" + cvId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Invalidated cache for CV {}, deleted {} keys", cvId, keys.size());
        }
    }
    
    /**
     * Build model từ CV entity
     */
    private Map<String, Object> buildModelFromCV(CV cv, JsonNode mergedConfig) {
        Map<String, Object> model = new HashMap<>();
        
        // User info (từ CV.cvData hoặc CV.user)
        if (cv.getCvData() != null && cv.getCvData().has("user")) {
            model.put("user", objectMapper.convertValue(cv.getCvData().get("user"), Map.class));
        } else if (cv.getUser() != null) {
            // Fallback: lấy từ User entity
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("firstName", cv.getUser().getFirstName());
            userMap.put("lastName", cv.getUser().getLastName());
            userMap.put("email", cv.getUser().getEmail());
            userMap.put("avatar", cv.getUser().getAvatar() != null ? cv.getUser().getAvatar() : "");
            model.put("user", userMap);
        }
        
        // Sections (từ CVSection entities)
        if (cv.getSections() != null && !cv.getSections().isEmpty()) {
            List<Map<String, Object>> sections = cv.getSections().stream()
                .filter(section -> section.getIsVisible() != null && section.getIsVisible())
                .sorted(Comparator.comparing(CVSection::getOrderIndex, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(section -> {
                    Map<String, Object> sectionMap = new HashMap<>();
                    sectionMap.put("sectionType", section.getSectionType());
                    sectionMap.put("isVisible", section.getIsVisible());
                    
                    // Parse sectionData JSON to List/Map
                    if (section.getSectionData() != null) {
                        if (section.getSectionData().isArray()) {
                            sectionMap.put("sectionData", objectMapper.convertValue(section.getSectionData(), List.class));
                        } else {
                            sectionMap.put("sectionData", objectMapper.convertValue(section.getSectionData(), Map.class));
                        }
                    } else {
                        sectionMap.put("sectionData", Collections.emptyList());
                    }
                    
                    return sectionMap;
                })
                .collect(Collectors.toList());
            model.put("sections", sections);
        } else {
            model.put("sections", Collections.emptyList());
        }
        
        // Config (merged template + customization)
        model.put("config", objectMapper.convertValue(mergedConfig, Map.class));
        
        return model;
    }
    
    /**
     * Merge template config với user customization
     */
    private JsonNode mergeConfigs(JsonNode templateConfig, JsonNode customization) {
        if (templateConfig == null) {
            templateConfig = objectMapper.createObjectNode();
        }
        
        if (customization == null || customization.isNull() || customization.isEmpty()) {
            return templateConfig;
        }
        
        ObjectNode merged = templateConfig.deepCopy();
        Iterator<Map.Entry<String, JsonNode>> fields = customization.fields();
        
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            if (merged.has(entry.getKey()) && merged.get(entry.getKey()).isObject() && entry.getValue().isObject()) {
                // Deep merge for nested objects
                ObjectNode nestedMerged = (ObjectNode) merged.get(entry.getKey());
                entry.getValue().fields().forEachRemaining(nested ->
                    nestedMerged.set(nested.getKey(), nested.getValue())
                );
            } else {
                // Override for non-object values
                merged.set(entry.getKey(), entry.getValue());
            }
        }
        
        return merged;
    }
    
    /**
     * Build cache key từ CV data
     */
    private String buildCacheKey(CV cv) {
        StringBuilder dataStr = new StringBuilder();
        dataStr.append(cv.getId());
        dataStr.append(cv.getTemplate().getId());
        
        if (cv.getCvData() != null) {
            dataStr.append(cv.getCvData().toString());
        }
        
        if (cv.getCustomization() != null) {
            dataStr.append(cv.getCustomization().toString());
        }
        
        if (cv.getSections() != null) {
            cv.getSections().forEach(section -> {
                dataStr.append(section.getSectionType());
                if (section.getSectionData() != null) {
                    dataStr.append(section.getSectionData().toString());
                }
            });
        }
        
        String dataHash = DigestUtils.md5Hex(dataStr.toString());
        return "rendered:cv:" + cv.getId() + ":" + dataHash;
    }
    
    /**
     * Extract template name từ file path
     */
    private String extractTemplateName(String filePath) {
        return Paths.get(filePath).getFileName().toString().replace(".html", "");
    }
}
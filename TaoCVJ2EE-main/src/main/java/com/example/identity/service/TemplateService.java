package com.example.identity.service;

import com.example.identity.dto.TemplateFilterDTO;
import com.example.identity.dto.request.TemplateRequest;
import com.example.identity.dto.response.TemplateResponse;
import com.example.identity.entity.Template;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.mapper.TemplateMapper;
import com.example.identity.repository.TemplateRepository;
import com.example.identity.specification.TemplateSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TemplateService {
    TemplateRepository templateRepository;
    TemplateMapper templateMapper;
    TemplateSpecification templateSpecification;

    public List<TemplateResponse> findAll() {
        log.info("Fetching all templates from database");
        return templateRepository.findAll().stream()
                .map(templateMapper::toDtoRes)
                .toList();
    }

    @Cacheable(value = "templates", key = "#id")
    public TemplateResponse findById(Long id) {
        log.info("Fetching template from database with id: {}", id);
        return templateRepository.findById(id).map(templateMapper::toDtoRes)
                .orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND));
    }

    @CacheEvict(value = { "templates", "templates-list" }, allEntries = true)
    public TemplateResponse createTemplate(TemplateRequest templateRequest) {
        log.info("Creating new template and clearing cache");
        Template template = templateMapper.toEntity(templateRequest);
        return templateMapper.toDtoRes(templateRepository.save(template));
    }

    @Caching(evict = {
            @CacheEvict(value = "templates", key = "#id"),
            @CacheEvict(value = "templates-list", allEntries = true)
    })
    public TemplateResponse editTemplate(Long id, TemplateRequest templateRequest) {
        log.info("Editing template with id: {} and clearing cache", id);
        Template old = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND));
        templateMapper.updateTemplate(old, templateRequest);
        return templateMapper.toDtoRes(templateRepository.save(old));
    }

    public List<TemplateResponse> filter(TemplateFilterDTO filter) {
        Specification<Template> spec = templateSpecification.filterBy(filter);
        return templateRepository.findAll(spec).stream()
                .map(templateMapper::toDtoRes)
                .toList();
    }

    @Cacheable(value = "templates-entity", key = "#id")
    public Template getTemplateById(Long id) {
        log.info("Fetching template entity from database with id: {}", id);
        return templateRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND));
    }

    @Caching(evict = {
            @CacheEvict(value = "templates", key = "#id"),
            @CacheEvict(value = "templates-entity", key = "#id"),
            @CacheEvict(value = "templates-list", allEntries = true)
    })
    public void clearCache(Long id) {
        log.info("Manually clearing cache for template id: {}", id);
    }

    @CacheEvict(value = { "templates", "templates-entity", "templates-list" }, allEntries = true)
    public void clearAllCache() {
        log.info("Manually clearing all templates cache");
    }
}

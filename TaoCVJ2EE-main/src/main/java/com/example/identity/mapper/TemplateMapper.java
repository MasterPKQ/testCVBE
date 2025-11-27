package com.example.identity.mapper;

import com.example.identity.dto.request.CVRequest;
import com.example.identity.dto.request.TemplateRequest;
import com.example.identity.dto.response.TemplateResponse;
import com.example.identity.entity.CV;
import com.example.identity.entity.Template;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TemplateMapper{
    Template toEntity(TemplateRequest templateRequest);
    TemplateRequest toDtoReq(Template template);
    TemplateResponse toDtoRes(Template template);
    void updateTemplate(@MappingTarget Template template, TemplateRequest templateRequest);
}

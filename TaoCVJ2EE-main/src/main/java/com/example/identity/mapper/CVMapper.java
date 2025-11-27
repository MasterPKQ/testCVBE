package com.example.identity.mapper;

import com.example.identity.dto.request.CVRequest;
import com.example.identity.dto.response.CVResponse;
import com.example.identity.entity.CV;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CVMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "shareToken", ignore = true)
    @Mapping(target = "qrCodeUrl", ignore = true)
    @Mapping(target = "sectionOrder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastAccessedAt", ignore = true)
    CV toCV(CVRequest cvRequest);

    @Mapping(target = "templateId", expression = "java(cv.getTemplate() != null ? cv.getTemplate().getId() : null)")
    CVResponse toCVResponse(CV cv);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "shareToken", ignore = true)
    @Mapping(target = "qrCodeUrl", ignore = true)
    @Mapping(target = "sectionOrder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastAccessedAt", ignore = true)
    void updateCV(@MappingTarget CV cv, CVRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastAccessedAt", ignore = true)
    void duplicateCV(@MappingTarget CV cvNew, CV cvOld);
}

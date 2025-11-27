package com.example.identity.mapper;

import com.example.identity.entity.CVSection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CVSectionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cv", ignore = true)
    void updateSection(@MappingTarget CVSection cvSection1, CVSection cvSection2);
}

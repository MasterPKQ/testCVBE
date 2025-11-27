package com.example.identity.service;

import com.example.identity.dto.request.CVSectionRequestReorder;
import com.example.identity.entity.CV;
import com.example.identity.entity.CVSection;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.mapper.CVSectionMapper;
import com.example.identity.repository.CVRepository;
import com.example.identity.repository.CVSectionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CVSectionService {
    CVSectionRepository cvSectionRepository;
    CVSectionMapper cvSectionMapper;
    CVRepository cvRepository;

    public CVSection getSectionById(Long sectionId) {
        return cvSectionRepository.findById(sectionId).orElse(null);
    }

    public List<CVSection> getListSectionByCVIdSorted(Long cvId) {
        List<CVSection> list = cvSectionRepository.findAllByCvId(cvId);
        //sort order index
        list.sort(Comparator.comparingInt(CVSection::getOrderIndex));
        return list;
    }

    public List<CVSection> reorderCVSection(Long cvId, List<CVSectionRequestReorder> cvSectionRequestReorder) {
        CV cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new AppException(ErrorCode.CV_NOT_FOUND));
        List<CVSection> sections = cv.getSections();
        for (CVSection section : sections) {
            cvSectionRequestReorder.stream()
                    .filter(r -> r.getId().equals(section.getId()))
                    .findFirst()
                    .ifPresent(r -> section.setOrderIndex(r.getOrderIndex()));
        }
        cvSectionRepository.saveAll(sections);
        sections.sort(Comparator.comparingInt(CVSection::getOrderIndex));
        return sections;
    }

    public CVSection createCVSection(Long cvId, CVSection cvSection) {
        cvSection.setCv(cvRepository.findById(cvId).orElseThrow(() -> new AppException(ErrorCode.CV_NOT_FOUND)));
        return cvSectionRepository.save(cvSection);
    }

    public CVSection updateCVSection(Long sectionId, CVSection cvSection2) {
        CVSection cvSection1 = cvSectionRepository.findById(sectionId).orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));
        cvSectionMapper.updateSection(cvSection1, cvSection2);
        return cvSectionRepository.save(cvSection1);
    }

    public Boolean deleteCVSection(Long id) {
        cvSectionRepository.deleteById(id);
        return true;
    }
}

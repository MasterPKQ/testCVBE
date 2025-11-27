package com.example.identity.specification;

import com.example.identity.dto.TemplateFilterDTO;
import com.example.identity.entity.Template;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TemplateSpecification {

    public Specification<Template> filterBy(TemplateFilterDTO filter) {
        // Trả về một lambda (root, query, criteriaBuilder)
        return (root, query, cb) -> {

            // Tạo một danh sách các điều kiện (Predicate)
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter theo 'category'
            // Chỉ thêm vào nếu 'category' được cung cấp (khác null và không rỗng)
            if (filter.getCategory() != null && !filter.getCategory().isEmpty()) {
                // Thêm điều kiện: WHERE category LIKE '%filter.getCategory()%'
                // (Dùng 'like' để tìm kiếm tương đối, 'equal' để tìm chính xác)
                predicates.add(cb.like(
                        cb.lower(root.get("category")), // Lấy trường 'category' từ entity Template
                        "%" + filter.getCategory().toLowerCase() + "%"
                ));
            }

            // 2. Filter theo 'name'
            if (filter.getName() != null && !filter.getName().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"
                ));
            }

            // 3. Filter theo 'style' (ví dụ tìm chính xác)
            if (filter.getStyle() != null && !filter.getStyle().isEmpty()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("style")),
                        filter.getStyle().toLowerCase()
                ));
            }

            // Thêm các điều kiện filter khác của bạn ở đây...

            // Kết hợp tất cả các điều kiện lại bằng 'AND'
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
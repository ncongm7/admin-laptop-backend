package com.example.backendlaptop.service;

import com.example.backendlaptop.dto.CategoryResponse;
import com.example.backendlaptop.repository.DanhMucRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DanhMucService {

    private final DanhMucRepository danhMucRepository;

    public List<CategoryResponse> getAllCategories() {
        List<Object[]> results = danhMucRepository.findCategoriesWithProductCount();
        return results.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToCategoryResponse(Object[] row) {
        return CategoryResponse.builder()
                .id(UUID.fromString(row[0].toString()))
                .name((String) row[1])
                .slug((String) row[2])
                .iconUrl((String) row[3])
                .productCount(((Number) row[4]).longValue())
                .build();
    }
}

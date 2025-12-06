package com.example.backendlaptop.service.baohanh;

import com.example.backendlaptop.entity.LyDoBaoHanh;
import com.example.backendlaptop.model.response.baohanh.LyDoBaoHanhResponse;
import com.example.backendlaptop.repository.LyDoBaoHanhRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LyDoBaoHanhService {
    private final LyDoBaoHanhRepository lyDoBaoHanhRepository;

    public List<LyDoBaoHanhResponse> getAllLyDo() {
        List<LyDoBaoHanh> lyDoList = lyDoBaoHanhRepository.findByIsActiveTrueOrderByThuTuAsc();
        return lyDoList.stream()
                .map(LyDoBaoHanhResponse::new)
                .collect(Collectors.toList());
    }

    public List<LyDoBaoHanhResponse> getLyDoByLoai(String loai) {
        List<LyDoBaoHanh> lyDoList = lyDoBaoHanhRepository.findByLoaiLyDoAndIsActiveTrueOrderByThuTuAsc(loai);
        return lyDoList.stream()
                .map(LyDoBaoHanhResponse::new)
                .collect(Collectors.toList());
    }
}


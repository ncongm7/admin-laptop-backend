package com.example.backendlaptop.service.PhanQuyenSer;

import com.example.backendlaptop.dto.diaChi.TinhTPDto;
import com.example.backendlaptop.dto.diaChi.XaPhuongDto;
import com.example.backendlaptop.entity.LcSubdistrict;
import com.example.backendlaptop.repository.TinhTPRepository;
import com.example.backendlaptop.repository.XaPhuongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class XaPhuongService {
    @Autowired
    private XaPhuongRepository xaPhuongRepository;
    @Autowired
    private TinhTPRepository tinhTPRepository;

    public List<XaPhuongDto> findByTinh(Integer codeTinh) {
        return xaPhuongRepository.timTinh(codeTinh);
    }
    public List<TinhTPDto> getAllTinhTP() {
        return tinhTPRepository.findAllTinh();
    }
    public List<TinhTPDto> getTinhTPByTenTinh(String tenTinh) {
        return tinhTPRepository.findTenTinh(tenTinh);
    }


}

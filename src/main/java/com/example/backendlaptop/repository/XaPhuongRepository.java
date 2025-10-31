package com.example.backendlaptop.repository;

import com.example.backendlaptop.dto.diaChi.XaPhuongDto;
import com.example.backendlaptop.entity.LcSubdistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface XaPhuongRepository extends JpaRepository<LcSubdistrict, Integer> {
    List<LcSubdistrict> findByProvinceId(Integer provinceId);

    @Query("select new com.example.backendlaptop.dto.diaChi.XaPhuongDto(x.id, x.districtId, x.districtCode, x.provinceId, x.provinceCode, x.name, x.shortname, x.code )" +
            " from LcSubdistrict x where x.provinceId =:codeTinh ")
    List<XaPhuongDto> timTinh(Integer codeTinh);

}

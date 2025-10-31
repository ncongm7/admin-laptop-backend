package com.example.backendlaptop.repository;

import com.example.backendlaptop.dto.diaChi.TinhTPDto;
import com.example.backendlaptop.entity.LcProvince;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TinhTPRepository extends JpaRepository<LcProvince, Integer> {

    @Query("select new com.example.backendlaptop.dto.diaChi.TinhTPDto(t.id, t.name, t.code) " +
            "from LcProvince t ")
    List<TinhTPDto> findAllTinh();

    @Query("select new com.example.backendlaptop.dto.diaChi.TinhTPDto(t.id, t.name, t.code) " +
            " from LcProvince t where t.name like CONCAT('%',:tenTinh, '%') ")
    List<TinhTPDto> findTenTinh(@Param("tenTinh") String tenTinh);
}

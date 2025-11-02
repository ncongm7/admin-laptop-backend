package com.example.backendlaptop.dto.diaChi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class XaPhuongDto {
    private Integer id;
    private Integer districtId;
    private String districtCode;
    private Integer provinceId;
    private String provinceCode;
    private String name;
    private String shortname;
    private String code;
}

package com.example.backendlaptop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "lc_subdistrict")
public class LcSubdistrict {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "district_id", nullable = false)
    private Integer districtId;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "district_code", nullable = false, length = 20)
    private String districtCode;

    @NotNull
    @Column(name = "province_id", nullable = false)
    private Integer provinceId;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "province_code", nullable = false, length = 20)
    private String provinceCode;

    @Size(max = 100)
    @Nationalized
    @Column(name = "name", length = 100)
    private String name;

    @Size(max = 100)
    @Nationalized
    @Column(name = "shortname", length = 100)
    private String shortname;

    @Size(max = 36)
    @Nationalized
    @Column(name = "code", length = 36)
    private String code;

    @Size(max = 255)
    @Nationalized
    @Column(name = "description")
    private String description;

    @Size(max = 100)
    @Nationalized
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @Size(max = 100)
    @Nationalized
    @Column(name = "modified_by", length = 100)
    private String modifiedBy;

    @Column(name = "modified_date")
    private Instant modifiedDate;

}
package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "serial_da_ban")
public class SerialDaBan {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hoa_don_chi_tiet", nullable = false)
    private HoaDonChiTiet idHoaDonChiTiet;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_serial", nullable = false)
    private Serial idSerial;

    @Column(name = "ngay_tao")
    private Instant ngayTao;

}
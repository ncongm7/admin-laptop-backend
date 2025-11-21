package com.example.backendlaptop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class SanphamDanhmucId implements Serializable {
    private static final long serialVersionUID = -1088668502215643701L;
    @NotNull
    @Column(name = "san_pham_id", nullable = false)
    private UUID idSanPham;

    @NotNull
    @Column(name = "danh_muc_id", nullable = false)
    private UUID idDanhMuc;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SanphamDanhmucId entity = (SanphamDanhmucId) o;
        return Objects.equals(this.idDanhMuc, entity.idDanhMuc) &&
                Objects.equals(this.idSanPham, entity.idSanPham);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDanhMuc, idSanPham);
    }

}
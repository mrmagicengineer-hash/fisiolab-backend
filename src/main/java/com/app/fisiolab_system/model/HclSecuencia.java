package com.app.fisiolab_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hcl_secuencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HclSecuencia {

    @Id
    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private Integer ultimoNumero;

    @Version
    private Long version;
}

package com.example.mauro.devices_api.model;

import java.time.LocalDateTime;

import com.example.mauro.devices_api.dto.DeviceDTO;

import io.micrometer.common.lang.NonNull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@Builder
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @NonNull
    @Enumerated(EnumType.STRING)
    private DeviceState state;

    @NonNull
    private LocalDateTime creationTime;

    @Builder
    public Device(Long id, String name, Brand brand, DeviceState state, LocalDateTime creationTime) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.state = state;
        this.creationTime = creationTime != null ? creationTime : LocalDateTime.now();
    }

    public DeviceDTO convertToDTO() {
        String brandName = getBrand() == null ? null : getBrand().getName();
        return DeviceDTO.builder()
                .id(getId())
                .name(getName())
                .brand(brandName)
                .state(getState())
                .creationTime(getCreationTime())
                .build();
    }
}


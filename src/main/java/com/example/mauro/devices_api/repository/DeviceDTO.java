package com.example.mauro.devices_api.repository;

import java.time.LocalDateTime;

import com.example.mauro.devices_api.model.DeviceState;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceDTO {
    private Long id;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Brand is mandatory")
    private String brand;

    @NotBlank(message = "State is mandatory")
    private DeviceState state;

    private LocalDateTime creationTime;

    @Builder
    public DeviceDTO(Long id, String name, String brand, DeviceState state, LocalDateTime creationTime) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.state = state;
        this.creationTime = creationTime;
    }
}
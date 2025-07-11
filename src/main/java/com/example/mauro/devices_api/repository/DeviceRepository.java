package com.example.mauro.devices_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mauro.devices_api.model.Device;
import com.example.mauro.devices_api.model.DeviceState;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByName(String name);
    List<Device> findByBrandName(String brandName);
    List<Device> findByState(DeviceState stateName);
}
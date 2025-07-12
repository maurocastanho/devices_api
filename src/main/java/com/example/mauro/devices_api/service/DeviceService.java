package com.example.mauro.devices_api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mauro.devices_api.dto.DeviceDTO;
import com.example.mauro.devices_api.exception.ResourceAlreadyExistsException;
import com.example.mauro.devices_api.model.Brand;
import com.example.mauro.devices_api.model.Device;
import com.example.mauro.devices_api.model.DeviceState;
import com.example.mauro.devices_api.repository.BrandRepository;
import com.example.mauro.devices_api.repository.DeviceRepository;

@Service
public class DeviceService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private BrandRepository brandRepository;

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id);
    }

    public List<Device> getDevicesByBrand(String brandName) {
        List<Device> devices = deviceRepository.findByBrandName(brandName);
        return devices;
    }

    public List<Device> getDevicesByState(DeviceState state) {
        return deviceRepository.findByState(state);
    }

    public Device createDevice(Device device) {
        Optional<Device> olDevice = deviceRepository.findByName(device.getName());
        if (olDevice.isPresent()) {
            throw new ResourceAlreadyExistsException("There is already a device with name = " + device.getName());
        }
        String brandName = device.getBrand().getName();
        Brand brand = brandRepository.findByName(brandName)
                .orElseGet(() -> brandRepository.save(device.getBrand()));

        device.setBrand(brand);
        Device savedDevice = deviceRepository.save(device);
        return savedDevice;
    }

    public Device updateDevice(Long id, DeviceDTO deviceDTO) {
        Optional<Device> existingDeviceOpt = deviceRepository.findById(id);
        if (existingDeviceOpt.isEmpty()) {
            // device does not exist, create a new device
            Brand brand = brandRepository.findByName(deviceDTO.getBrand())
                    .orElseGet(() -> brandRepository.save(Brand.builder().name(deviceDTO.getBrand()).build()));
            Device device = Device.builder()
                    .name(deviceDTO.getName())
                    .brand(brand)
                    .state(deviceDTO.getState())
                    .build();
            return createDevice(device);
        }

        Device existingDevice = existingDeviceOpt.get();

        if (!existingDevice.getState().equals(DeviceState.IN_USE)) {
            // Do not update name and/or brand if device is in use
            // Maybe should be better ro return a failed status instead of ignoring those
            // fields...

            if (deviceDTO.getName() != null) {
                existingDevice.setName(deviceDTO.getName());
            }
            if (deviceDTO.getBrand() != null) {
                Brand brand = brandRepository.findByName(deviceDTO.getBrand())
                        .orElseGet(() -> brandRepository.save(existingDevice.getBrand()));
                existingDevice.setBrand(brand);
            }

        }
        existingDevice.setState(deviceDTO.getState());

        // creationTime is not updated
        return deviceRepository.save(existingDevice);
    }

    public boolean deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            return false;
        }
        deviceRepository.deleteById(id);
        return true;
    }

}
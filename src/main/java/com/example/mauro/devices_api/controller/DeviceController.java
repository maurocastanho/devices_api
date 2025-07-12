package com.example.mauro.devices_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mauro.devices_api.dto.DeviceDTO;
import com.example.mauro.devices_api.exception.ResourceAlreadyExistsException;
import com.example.mauro.devices_api.model.Brand;
import com.example.mauro.devices_api.model.Device;
import com.example.mauro.devices_api.model.DeviceState;
import com.example.mauro.devices_api.service.DeviceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/devices")
@Tag(name = "Devices API", description = "API for managing devices")
public class DeviceController {
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Operation(summary = "Get all devices", description = "Retrieve all devices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
    })
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices().stream().map(device -> device.convertToDTO()).toList());
    }

    @Operation(summary = "Get a device by ID", description = "Retrieves a device based on their unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device found"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable Long id) {
        return deviceService.getDeviceById(id)
                .map(device -> device.convertToDTO())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new device", description = "Creates a new device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created"),
            @ApiResponse(responseCode = "400", description = "A device with the same name already")
    })
    @PostMapping
    public ResponseEntity<DeviceDTO> createDevice(@RequestBody DeviceDTO deviceDTO) {

        Brand brand = Brand.builder().name(deviceDTO.getBrand()).build();
        Device device = Device.builder()
                .name(deviceDTO.getName())
                .brand(brand)
                .state(deviceDTO.getState())
                .build();

        try {
            Device created = deviceService.createDevice(device);
            return ResponseEntity.ok(created.convertToDTO());
        } catch (ResourceAlreadyExistsException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get devices by brand name", description = "Retrieves all devices from a given brand")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
    })
    @GetMapping("/brand/{brandName}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByBrandName(@PathVariable String brandName) {
        List<Device> devices = deviceService.getDevicesByBrand(brandName);
        List<DeviceDTO> deviceDTOs = devices.stream()
                .map(device -> device.convertToDTO()).toList();

        return ResponseEntity.ok(deviceDTOs);
    }

    @Operation(summary = "Get devices by state", description = "Retrieves all devices with a given state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
    })
    @GetMapping("/state/{state}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByState(@PathVariable DeviceState state) {
        return ResponseEntity
                .ok(deviceService.getDevicesByState(state).stream().map(device -> device.convertToDTO()).toList());
    }

    @Operation(summary = "Update a device by id", description = "Fully and/or partially update an existing device")
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(@PathVariable Long id, @Valid @RequestBody DeviceDTO deviceDTO) {
        Device updated = deviceService.updateDevice(id, deviceDTO);
        return ResponseEntity.ok(updated.convertToDTO());
    }

    @Operation(summary = "Delete a device by id", description = "Delete a device by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

}
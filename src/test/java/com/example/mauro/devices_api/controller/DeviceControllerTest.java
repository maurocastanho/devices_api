package com.example.mauro.devices_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.mauro.devices_api.dto.DeviceDTO;
import com.example.mauro.devices_api.model.Brand;
import com.example.mauro.devices_api.model.Device;
import com.example.mauro.devices_api.model.DeviceState;
import com.example.mauro.devices_api.service.DeviceService;

@WebMvcTest(DeviceController.class)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class DeviceControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private DeviceService deviceService;

        private Device device;
        private Brand brand;
        private LocalDateTime now;

        @BeforeEach
        void setUp() {
                now = LocalDateTime.now();
                brand = Brand.builder().id(1L).name("Dell").build();
                device = Device.builder()
                                .id(1L)
                                .name("Laptop")
                                .brand(brand)
                                .state(DeviceState.AVAILABLE)
                                .creationTime(now)
                                .build();
        }

        @Test
        void getAllDevices_ReturnsList() throws Exception {
                when(deviceService.getAllDevices()).thenReturn(Collections.singletonList(device));

                mockMvc.perform(get("/api/v1/devices")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Laptop"))
                                .andExpect(jsonPath("$[0].brand").value("Dell"));
        }

        @Test
        void getDeviceById_Exists_ReturnsDevice() throws Exception {
                when(deviceService.getDeviceById(1L)).thenReturn(Optional.of(device));

                mockMvc.perform(get("/api/v1/devices/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Laptop"))
                                .andExpect(jsonPath("$.brand").value("Dell"));
        }

        @Test
        void getDevicesByState_ReturnsDevices() throws Exception {
                Brand brand = Brand.builder().id(1L).name("Dell").build();
                Device device = Device.builder()
                                .id(1L)
                                .name("Laptop")
                                .brand(brand)
                                .state(DeviceState.AVAILABLE)
                                .creationTime(LocalDateTime.now())
                                .build();
                when(deviceService.getDevicesByState(DeviceState.AVAILABLE))
                                .thenReturn(Collections.singletonList(device));

                mockMvc.perform(get("/api/v1/devices/state/AVAILABLE")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Laptop"))
                                .andExpect(jsonPath("$[0].brand").value("Dell"))
                                .andExpect(jsonPath("$[0].state").value("AVAILABLE"));
        }

        @Test
        void getDevicesByBrand_ReturnsDevices() throws Exception {
                Brand brand = Brand.builder().id(1L).name("Dell").build();
                Device device = Device.builder()
                                .id(1L)
                                .name("Laptop")
                                .brand(brand)
                                .state(DeviceState.AVAILABLE)
                                .creationTime(LocalDateTime.now())
                                .build();
                when(deviceService.getDevicesByBrand("Dell")).thenReturn(Collections.singletonList(device));

                mockMvc.perform(get("/api/v1/devices/brand/Dell")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Laptop"))
                                .andExpect(jsonPath("$[0].brand").value("Dell"))
                                .andExpect(jsonPath("$[0].state").value("AVAILABLE"));
        }

        @Test
        void createDevice_ValidInput_ReturnsCreated() throws Exception {
                Device device = Device.builder()
                                .id(1L)
                                .name("Laptop")
                                .brand(brand)
                                .state(DeviceState.AVAILABLE)
                                .creationTime(LocalDateTime.now())
                                .build();
                when(deviceService.createDevice(any(Device.class))).thenReturn(device);

                String json = "{\"name\":\"Laptop\",\"brand\":\"Dell\",\"state\":\"AVAILABLE\"}";

                mockMvc.perform(post("/api/v1/devices")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Laptop"))
                                .andExpect(jsonPath("$.brand").value("Dell"));
        }

        @Test
        void updateDevice_ValidInput_UpdatesDeviceAndPreservesCreationTime() throws Exception {
                Brand brand = Brand.builder().id(1L).name("Apple").build();
                LocalDateTime originalCreationTime = LocalDateTime.now().minusDays(1);
                Device device = Device.builder()
                                .id(1L)
                                .name("Phone")
                                .brand(brand)
                                .state(DeviceState.IN_USE)
                                .creationTime(originalCreationTime)
                                .build();
                when(deviceService.updateDevice(eq(1L), any(DeviceDTO.class))).thenReturn(device);

                String json = "{\"name\":\"Phone\",\"brandName\":\"Apple\",\"state\":\"IN_USE\"}";

                mockMvc.perform(put("/api/v1/devices/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Phone"))
                                .andExpect(jsonPath("$.brand").value("Apple"))
                                .andExpect(jsonPath("$.state").value("IN_USE"));
        }

        @Test
        void deleteDevice_Exists_ReturnsNoContent() throws Exception {
                when(deviceService.deleteDevice(1L)).thenReturn(true);

                mockMvc.perform(delete("/api/v1/devices/1"))
                                .andExpect(status().isNoContent());

                verify(deviceService, times(1)).deleteDevice(1L);
        }

        @Test
        void deleteDevice_NonExistent_ReturnsNotFound() throws Exception {
                when(deviceService.deleteDevice(1L)).thenReturn(false);

                mockMvc.perform(delete("/api/v1/devices/1"))
                                .andExpect(status().isNotFound());

                verify(deviceService, times(1)).deleteDevice(1L);
        }
}
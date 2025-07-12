package com.example.mauro.devices_api.service;

import com.example.mauro.devices_api.dto.DeviceDTO;
import com.example.mauro.devices_api.model.Brand;
import com.example.mauro.devices_api.model.Device;
import com.example.mauro.devices_api.model.DeviceState;
import com.example.mauro.devices_api.repository.BrandRepository;
import com.example.mauro.devices_api.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeviceServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("restapi")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private BrandRepository brandRepository;

    @BeforeEach
    void setUp() {
        // Clear database before each test to ensure consistent state
        deviceRepository.deleteAll();
        brandRepository.deleteAll();
    }

    private void setupTestData() {
        // Initialize and persist common test data
        Brand brand1 = Brand.builder().name("Dell").build();
        Brand brand2 = Brand.builder().name("Apple").build();
        brand1 = brandRepository.save(brand1);
        brand2 = brandRepository.save(brand2);

        Device device1 = Device.builder()
                .name("Laptop")
                .brand(brand1)
                .state(DeviceState.AVAILABLE)
                .creationTime(LocalDateTime.now())
                .build();
        Device device2 = Device.builder()
                .name("Tablet")
                .brand(brand1)
                .state(DeviceState.AVAILABLE)
                .creationTime(LocalDateTime.now())
                .build();
        Device device3 = Device.builder()
                .name("Phone")
                .brand(brand2)
                .state(DeviceState.IN_USE)
                .creationTime(LocalDateTime.now())
                .build();

        deviceRepository.save(device1);
        deviceRepository.save(device2);
        deviceRepository.save(device3);
    }

    @Test
    void getAllDevices_EmptyDatabase_ReturnsEmptyList() {
        List<Device> devices = deviceService.getAllDevices();
        assertThat(devices).isEmpty();
    }

    @Test
    void getAllDevices_MultipleDevices_ReturnsAllDevices() {
        setupTestData();

        List<Device> devices = deviceService.getAllDevices();

        assertThat(devices).hasSize(3);
        assertThat(devices).extracting(Device::getName).containsExactlyInAnyOrder("Laptop", "Tablet", "Phone");
        assertThat(devices).extracting(device -> device.getBrand().getName()).containsExactlyInAnyOrder("Dell", "Dell",
                "Apple");
    }

    @Test
    void getDeviceById_Exists_ReturnsDevice() {
        Brand brand = Brand.builder().name("Dell").build();
        brand = brandRepository.save(brand);

        Device device = Device.builder()
                .name("Laptop")
                .brand(brand)
                .state(DeviceState.AVAILABLE)
                .creationTime(LocalDateTime.now())
                .build();
        Device savedDevice = deviceRepository.save(device);

        Optional<Device> retrievedDevice = deviceService.getDeviceById(savedDevice.getId());

        assertThat(retrievedDevice).isPresent();
        assertThat(retrievedDevice.get().getName()).isEqualTo("Laptop");
        assertThat(retrievedDevice.get().getBrand().getName()).isEqualTo("Dell");
        assertThat(retrievedDevice.get().getState()).isEqualTo(DeviceState.AVAILABLE);
    }

    @Test
    void getDeviceById_NonExistent_ReturnsEmpty() {
        Optional<Device> retrievedDevice = deviceService.getDeviceById(999L);
        assertThat(retrievedDevice).isEmpty();
    }

    @Test
    void getDevicesByState_MatchingState_ReturnsDevices() {
        setupTestData();

        List<Device> availableDevices = deviceService.getDevicesByState(DeviceState.AVAILABLE);

        assertThat(availableDevices).hasSize(2);
        assertThat(availableDevices).extracting(Device::getName).containsExactlyInAnyOrder("Laptop", "Tablet");
        assertThat(availableDevices).extracting(Device::getState).containsOnly(DeviceState.AVAILABLE);
    }

    @Test
    void getDevicesByState_NoMatchingState_ReturnsEmptyList() {
        setupTestData();

        List<Device> inactiveDevices = deviceService.getDevicesByState(DeviceState.INACTIVE);

        assertThat(inactiveDevices).isEmpty();
    }

    @Test
    void getDevicesByBrandName_MatchingBrand_ReturnsDevices() {
        setupTestData();

        List<Device> dellDevices = deviceService.getDevicesByBrand("Dell");

        assertThat(dellDevices).hasSize(2);
        assertThat(dellDevices).extracting(Device::getName).containsExactlyInAnyOrder("Laptop", "Tablet");
        assertThat(dellDevices).extracting(device -> device.getBrand().getName()).containsOnly("Dell");
    }

    @Test
    void getDevicesByBrandName_NonExistentBrand_ReturnsEmptyList() {
        setupTestData();

        List<Device> samsungDevices = deviceService.getDevicesByBrand("Samsung");

        assertThat(samsungDevices).isEmpty();
    }

    @Test
    void createDevice_SavesAndReturnsDeviceWithExistingBrand() {
        Brand brand = Brand.builder().name("Dell").build();
        brand = brandRepository.save(brand);

        Device device = Device.builder()
                .name("Laptop")
                .brand(brand)
                .state(DeviceState.AVAILABLE)
                .creationTime(LocalDateTime.now())
                .build();

        Device savedDevice = deviceService.createDevice(device);

        assertThat(savedDevice.getId()).isNotNull();
        assertThat(savedDevice.getName()).isEqualTo("Laptop");
        assertThat(savedDevice.getBrand().getName()).isEqualTo("Dell");
        assertThat(savedDevice.getState()).isEqualTo(DeviceState.AVAILABLE);

        Optional<Device> retrievedDevice = deviceRepository.findById(savedDevice.getId());
        assertThat(retrievedDevice).isPresent();
        assertThat(retrievedDevice.get().getName()).isEqualTo("Laptop");
        assertThat(retrievedDevice.get().getBrand().getName()).isEqualTo("Dell");
    }

    @Test
    void createDevice_NewBrand_CreatesBrandAndSavesDevice() {
        Device device = Device.builder()
                .name("Laptop")
                .state(DeviceState.AVAILABLE)
                .brand(Brand.builder().name("Dell").build())
                .creationTime(LocalDateTime.now())
                .build();

        Device savedDevice = deviceService.createDevice(device);

        assertThat(savedDevice.getId()).isNotNull();
        assertThat(savedDevice.getName()).isEqualTo("Laptop");
        assertThat(savedDevice.getBrand().getName()).isEqualTo("Dell");

        Brand retrievedBrand = brandRepository.findByName("Dell").orElseThrow();
        assertThat(retrievedBrand).isNotNull();
        assertThat(retrievedBrand.getName()).isEqualTo("Dell");
    }

    @Test
    void createDevice_NullState_SetsDefaultAvailable() {
        Device device = Device.builder()
                .name("Laptop")
                .brand(Brand.builder().name("Dell").build())
                .state(DeviceState.AVAILABLE)
                .creationTime(LocalDateTime.now())
                .build();

        Device savedDevice = deviceService.createDevice(device);

        assertThat(savedDevice.getId()).isNotNull();
        assertThat(savedDevice.getName()).isEqualTo("Laptop");
        assertThat(savedDevice.getBrand().getName()).isEqualTo("Dell");
        assertThat(savedDevice.getState()).isEqualTo(DeviceState.AVAILABLE);
    }

    @Test
    void updateDevice_UpdatesFieldsAndPreservesCreationTime() {
        Brand brand = Brand.builder().name("Dell").build();
        brand = brandRepository.save(brand);

        LocalDateTime originalCreationTime = LocalDateTime.now().minusDays(1);
        Device device = Device.builder()
                .name("Laptop")
                .brand(brand)
                .state(DeviceState.AVAILABLE)
                .creationTime(originalCreationTime)
                .build();
        Device savedDevice = deviceRepository.save(device);

        Brand newBrand = Brand.builder().name("Apple").build();
        newBrand = brandRepository.save(newBrand);

        Device updatedDevice = Device.builder()
                .name("Tablet")
                .brand(newBrand)
                .state(DeviceState.IN_USE)
                .creationTime(LocalDateTime.now()) // This should be ignored
                .build();

        DeviceDTO deviceDTO = updatedDevice.convertToDTO();

        Device result = deviceService.updateDevice(savedDevice.getId(), deviceDTO);

        result = deviceService.getDeviceById(savedDevice.getId()).get();

        assertThat(result.getId()).isEqualTo(savedDevice.getId());
        assertThat(result.getName()).isEqualTo("Tablet");
        assertThat(result.getBrand().getName()).isEqualTo("Apple");
        assertThat(result.getState()).isEqualTo(DeviceState.IN_USE);
        // Create ZoneId
        ZoneOffset zone = ZoneOffset.of("Z");
        // Compares using seconds because LocalDateTime loses some precision after it is saved and retrieved from the database
        assertThat(result.getCreationTime().toEpochSecond(zone)).isEqualTo(savedDevice.getCreationTime().toEpochSecond(zone));

        Optional<Device> retrievedDevice = deviceRepository.findById(result.getId());
        assertThat(retrievedDevice).isPresent();
        assertThat(retrievedDevice.get().getName()).isEqualTo("Tablet");
        assertThat(retrievedDevice.get().getBrand().getName()).isEqualTo("Apple");
        assertThat(retrievedDevice.get().getCreationTime().toEpochSecond(zone)).isEqualTo(savedDevice.getCreationTime().toEpochSecond(zone));
    }

    @Test
    void updateDevice_NonExistentId_ThrowsException() {
        Device updatedDevice = Device.builder()
                .name("Tablet")
                .state(DeviceState.IN_USE)
                .creationTime(LocalDateTime.now())
                .build();

        DeviceDTO deviceDTO = updatedDevice.convertToDTO();

        assertThrows(RuntimeException.class, () -> deviceService.updateDevice(999L, deviceDTO));
    }

    @Test
    void deleteDevice_Exists_RemovesDevice() {
        Brand brand = Brand.builder().name("Dell").build();
        brand = brandRepository.save(brand);

        Device device = Device.builder()
                .name("Laptop")
                .brand(brand)
                .state(DeviceState.AVAILABLE)
                .creationTime(LocalDateTime.now())
                .build();
        Device savedDevice = deviceRepository.save(device);

        boolean deleted = deviceService.deleteDevice(savedDevice.getId());

        assertThat(deleted).isTrue();
        Optional<Device> retrievedDevice = deviceRepository.findById(savedDevice.getId());
        assertThat(retrievedDevice).isNotPresent();
    }

    @Test
    void deleteDevice_NonExistent_ReturnsFalse() {
        boolean deleted = deviceService.deleteDevice(999L);
        assertThat(deleted).isFalse();
    }
}
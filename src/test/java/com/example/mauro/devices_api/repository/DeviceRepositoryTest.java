package com.example.mauro.devices_api.repository;

import com.example.mauro.devices_api.model.Brand;
import com.example.mauro.devices_api.model.Device;
import com.example.mauro.devices_api.model.DeviceState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeviceRepositoryTest {

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
    private DeviceRepository deviceRepository;

    @Autowired
    private BrandRepository brandRepository;

    private Brand brand1 = Brand.builder().name("Dell").build();
    private Brand brand2 = Brand.builder().name("Apple").build();

    @Test
    void saveDevice_PersistsAndRetrievesDevice() {

        brandRepository.save(brand1);
        brandRepository.save(brand2);

        Device device = Device.builder()
                .name("Laptop")
                .brand(brand1)
                .state(DeviceState.AVAILABLE)
                .creationTime(LocalDateTime.now())
                .build();

        Device savedDevice = deviceRepository.save(device);

        assertThat(savedDevice.getId()).isNotNull();
        assertThat(savedDevice.getName()).isEqualTo("Laptop");
        assertThat(savedDevice.getBrand().getName()).isEqualTo("Dell");
        assertThat(savedDevice.getState()).isEqualTo(DeviceState.AVAILABLE);
        assertThat(savedDevice.getCreationTime()).isNotNull();

        Optional<Device> retrievedDevice = deviceRepository.findById(savedDevice.getId());
        assertThat(retrievedDevice).isPresent();
        assertThat(retrievedDevice.get().getName()).isEqualTo("Laptop");
    }

    @Test
    void findAllDevices_ReturnsAllDevices() {

        brandRepository.save(brand1);
        brandRepository.save(brand2);

        Device device1 = Device.builder()
                .name("Laptop")
                .brand(brand1)
                .state(DeviceState.AVAILABLE)
                .creationTime(LocalDateTime.now())
                .build();
        Device device2 = Device.builder()
                .name("Phone")
                .brand(brand2)
                .state(DeviceState.IN_USE)
                .creationTime(LocalDateTime.now())
                .build();

        deviceRepository.save(device1);
        deviceRepository.save(device2);

        List<Device> devices = deviceRepository.findAll();

        assertThat(devices).hasSize(2);
        assertThat(devices).extracting(Device::getName).containsExactlyInAnyOrder("Laptop", "Phone");
    }
}
package io.github.iamnicknack.pi4j.client;

import com.pi4j.context.Context;
import com.pi4j.io.IO;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiBus;
import com.pi4j.io.spi.SpiMode;
import io.github.iamnicknack.pi4j.client.test.ClientInstance;
import io.github.iamnicknack.pi4j.client.test.DefaultClientConfig;
import io.github.iamnicknack.pi4j.client.test.WiremockCaptureClientConfig;
import io.github.iamnicknack.pi4j.client.test.WiremockClientConfig;
import io.github.iamnicknack.pi4j.server.Pi4jServer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {
                Pi4jServer.class,
                WiremockCaptureClientConfig.class,
                WiremockClientConfig.class,
                DefaultClientConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"logging.level.nws=DEBUG"}
)
@ActiveProfiles("mock")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeviceRegistryTest {

    @TestFactory
    Stream<DynamicTest> canCreateAndRemoveDevices(
            @Autowired Context remotePi4j,
            @Autowired @ClientInstance Context localPi4j
    ) {
        final List<Supplier<IO<?, ?, ?>>> suppliers = List.of(
                () -> localPi4j.create(
                        DigitalOutputConfig.newBuilder(localPi4j)
                                .id("test-output")
                                .address(1)
                                .build()
                ),
                () -> localPi4j.create(
                        DigitalInputConfig.newBuilder(localPi4j)
                                .id("test-input")
                                .address(2)
                                .build()
                ),
                () -> localPi4j.create(
                        Pwm.newConfigBuilder(localPi4j)
                                .id("test-pwm")
                                .address(3)
                                .dutyCycle(0)
                                .frequency(0)
                                .shutdown(0)
                                .build()
                ),
                () -> localPi4j.create(
                        Spi.newConfigBuilder(localPi4j)
                                .id("test-pwm")
                                .address(4)
                                .baud(1_000_000)
                                .bus(SpiBus.getByNumber(1))
                                .mode(SpiMode.MODE_0)
                                .build()
                )
        );

        return suppliers.stream()
                .map(supplier -> {
                    var device = supplier.get();
                    return DynamicTest.dynamicTest(device.name(), () -> {
                        assertThat(localPi4j.registry().exists(device.id())).isTrue();
                        assertThat(remotePi4j.registry().exists(device.id())).isTrue();

                        var localDevice = localPi4j.registry().get(device.id());
                        assertThat(localDevice.id()).isEqualTo(device.id());
                        var remoteDevice = remotePi4j.registry().get(device.id());
                        assertThat(remoteDevice.id()).isEqualTo(device.id());

                        localPi4j.registry().remove(device.id());
                        assertThat(localPi4j.registry().exists(device.id())).isFalse();
                        assertThat(remotePi4j.registry().exists(device.id())).isFalse();
                    });
                });
    }
}
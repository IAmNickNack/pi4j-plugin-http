package io.github.iamnicknack.pi4j.client;

import com.pi4j.context.Context;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiBus;
import com.pi4j.io.spi.SpiMode;
import com.pi4j.plugin.mock.provider.spi.MockSpi;
import io.github.iamnicknack.pi4j.client.test.ClientInstance;
import io.github.iamnicknack.pi4j.client.test.DefaultClientConfig;
import io.github.iamnicknack.pi4j.client.test.WiremockCaptureClientConfig;
import io.github.iamnicknack.pi4j.client.test.WiremockClientConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;
import io.github.iamnicknack.pi4j.server.Pi4jServer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = {
                Pi4jServer.class,
                HttpSpiTest.Config.class,
                WiremockCaptureClientConfig.class,
                WiremockClientConfig.class,
                DefaultClientConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"logging.level.com.pi4j=WARN"}
)
@ActiveProfiles("mock")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpSpiTest {

    private final Spi localSpi;

    public HttpSpiTest(@Autowired @ClientInstance Context localPi4j) {
        this.localSpi = localPi4j.create(
                Spi.newConfigBuilder(localPi4j)
                        .id("test-spi")
                        .name("SPI")
                        .baud(1_000_000)
                        .bus(SpiBus.getByNumber(1))
                        .mode(SpiMode.MODE_0)
                        .address(0)
                        .build()
        );
    }

    @AfterEach
    void afterEach(@Autowired MockSpi remoteSpi) {
        remoteSpi.readEntireMockBuffer();
    }

    @Test
    void canWriteBytes(@Autowired MockSpi remoteSpi) {
        localSpi.write(new byte[]{1, 2, 3});
        var bytes = remoteSpi.readEntireMockBuffer();
        assertEquals(3, bytes.length);
    }

    @Test
    void canWriteSingleByte(@Autowired MockSpi remoteSpi) {
        localSpi.write(1);
        var bytes = remoteSpi.readEntireMockBuffer();
        assertEquals(1, bytes[0]);
    }

    @Test
    void canReadBytes(@Autowired MockSpi remoteSpi) {
        remoteSpi.write(new byte[]{1, 2, 3});
        var bytes = new byte[3];
        localSpi.read(bytes);

        for (int i = 0; i < bytes.length; i++) {
            assertEquals(i + 1, bytes[i]);
        }
    }

    @Test
    void canReadSingleByte(@Autowired MockSpi remoteSpi) {
        remoteSpi.write(new byte[]{1});
        assertEquals(1, localSpi.read());
    }

    @Configuration
    static class Config {
        @Bean
        @Lazy
        MockSpi remoteSpi(Context remotePi4j) {
            return remotePi4j.registry().get("test-spi");
        }
    }
}
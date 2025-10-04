package io.github.iamnicknack.pi4j.client;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.plugin.mock.provider.i2c.MockI2C;
import io.github.iamnicknack.pi4j.server.Pi4jServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = { Pi4jServer.class, HttpI2CTest.Config.class },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "logging.level.com.pi4j=WARN",
                "logging.level.nws=DEBUG"
        }
)
@ActiveProfiles("mock")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpI2CTest {

    private final Context localPi4j;

    private final I2C localI2C;

    public HttpI2CTest(@LocalServerPort int port) {
        String baseUrl = "http://localhost:" + port;
        this.localPi4j = Pi4J.newContextBuilder()
                .add(new HttpI2CProvider(baseUrl))
                .build();

        this.localI2C = localPi4j.create(
                I2C.newConfigBuilder(localPi4j)
                        .id("test-i2c")
                        .bus(0)
                        .device(0)
                        .build()
        );
    }

    @AfterAll
    void afterAll() {
        localPi4j.shutdown();
    }

    @AfterEach
    void afterEach(@Autowired MockI2C remoteI2C) {
        // read enough data to clear the mock i2c buffer
        remoteI2C.read(new byte[100]);
    }

    @Test
    void canWriteAndReadByte() {
        var result = localI2C.write(42);
        assertEquals(1, result);
        assertEquals(42, localI2C.read());
    }

    @Test
    void canWriteAndReadBytes() {
        var dataOut = new byte[] { 1, 2, 3 };
        var result = localI2C.write(dataOut);
        assertEquals(3, result);

        var dataIn = new byte[3];
        localI2C.read(dataIn);

        assertArrayEquals(dataOut, dataIn);
    }

    @Test
    void canReadBytesWithArgs() {
        var dataOut = new byte[] { 1, 2, 3 };
        var result = localI2C.write(dataOut);
        assertEquals(3, result);

        var dataIn = new byte[3];
        result = localI2C.read(dataIn, 0, 3);

        assertEquals(dataIn.length, result);
        assertArrayEquals(dataOut, dataIn);
    }

    @Test
    void canReadSlice() {
        var dataOut = new byte[] { 1, 2, 3, 4, 5 };
        var result = localI2C.write(dataOut);
        assertEquals(5, result);

        var dataIn = new byte[] { 6, 7, 8, 9, 10 };
        result = localI2C.read(dataIn, 1, 3);

        assertEquals(3, result);
        assertArrayEquals(new byte[] { 6, 1, 2, 3, 10 }, dataIn);
    }


    @Test
    void canWriteAndReadRegister() {
        var result = localI2C.writeRegister(1, 42);
        assertEquals(1, result);
        assertEquals(42, localI2C.readRegister(1));
    }

    @Test
    void canReadRegister(@Autowired MockI2C remoteI2C) {
        remoteI2C.writeRegister(2, 84);
        var registerContents = localI2C.readRegister(2);
        assertEquals(84, registerContents);
    }

    @Test
    void canReadRegisterSlice() {
        var dataOut = new byte[] { 1, 2, 3, 4, 5 };
        var result = localI2C.writeRegister(3, dataOut);
        assertEquals(5, result);

        var dataIn = new byte[] { 6, 7, 8, 9, 10 };
        result = localI2C.readRegister(3, dataIn, 1, 3);

        assertEquals(3, result);
        assertArrayEquals(new byte[] { 6, 1, 2, 3, 10 }, dataIn);
    }


    @Configuration
    static class Config {

        @Bean
        @Lazy
        MockI2C remoteI2C(Context remotePi4j) {
            return remotePi4j.registry().get("test-i2c");
        }
    }
}
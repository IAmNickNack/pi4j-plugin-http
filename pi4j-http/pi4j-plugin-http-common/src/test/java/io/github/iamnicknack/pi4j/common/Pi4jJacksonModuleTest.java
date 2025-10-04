package io.github.iamnicknack.pi4j.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmConfig;
import com.pi4j.io.pwm.PwmType;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiBus;
import com.pi4j.io.spi.SpiConfig;
import com.pi4j.io.spi.SpiMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Pi4jJacksonModuleTest {

    private final Context context = Pi4J.newContext();

    private final ObjectMapper objectMapper = Pi4jJacksonModule.pi4jObjectMapper(context);

    private final Context pi4j = Pi4J.newContext();

    @Test
    void canReadWritePwmConfig() throws JsonProcessingException {
        var config = Pwm.newConfigBuilder(pi4j)
                .id("refresh")
                .name("Refresh PWM")
                .address(2)
                .pwmType(PwmType.HARDWARE)
                .frequency(220)
                .dutyCycle(50)
                .shutdown(0)
                .build();

        var json = objectMapper.writeValueAsString(config);
        var otherConfig = objectMapper.readValue(json, PwmConfig.class);
        assertEquals(config.id(), otherConfig.id());
        assertEquals(config.address(), otherConfig.address());
        assertEquals(config.pwmType(), otherConfig.pwmType());
        assertEquals(config.frequency(), otherConfig.frequency());
        assertEquals(config.dutyCycle(), otherConfig.dutyCycle());
        assertEquals(config.shutdownValue(), otherConfig.shutdownValue());
    }

    @Test
    void canReadWriteDigitalOutputConfig() throws JsonProcessingException {
        var config = DigitalOutput.newConfigBuilder(pi4j)
                .id("reset")
                .name("Reset Pin")
                .address(5)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.HIGH)
                .onState(DigitalState.LOW)
                .build();

        var json = objectMapper.writeValueAsString(config);
        var otherConfig = objectMapper.readValue(json, DigitalOutputConfig.class);
        assertEquals(config.id(), otherConfig.id());
        assertEquals(config.initialState(), otherConfig.initialState());
        assertEquals(config.shutdownState(), otherConfig.shutdownState());
        assertEquals(config.onState(), otherConfig.onState());
    }


    @Test
    void canReadWriteDigitalInputConfig() throws JsonProcessingException {
        var config = DigitalInput.newConfigBuilder(pi4j)
                .id("input-pin")
                .name("Input Pin")
                .address(5)
                .debounce(1L)
                .pull(PullResistance.PULL_UP)
                .build();

        var json = objectMapper.writeValueAsString(config);
        var otherConfig = objectMapper.readValue(json, DigitalInputConfig.class);
        assertEquals(config.id(), otherConfig.id());
        assertEquals(config.address(), otherConfig.address());
        assertEquals(config.debounce(), otherConfig.debounce());
        assertEquals(config.pull(), otherConfig.pull());
    }

    @Test
    void canReadWriteSpiConfig() throws JsonProcessingException {
        var config =  Spi.newConfigBuilder(pi4j)
                .id("spi-bus-1")
                .name("SPI")
                .baud(1_000_000)
                .bus(SpiBus.getByNumber(1))
                .mode(SpiMode.MODE_1)
                .address(0)
                .build();

        var json = objectMapper.writeValueAsString(config);
        var otherConfig = objectMapper.readValue(json, SpiConfig.class);
        assertEquals(config.id(), otherConfig.id());
        assertEquals(SpiBus.BUS_1, otherConfig.bus());
        assertEquals(SpiMode.MODE_1, otherConfig.mode());
    }

    @Test
    void canReadWriteI2CConfig() throws JsonProcessingException {
        var config = I2C.newConfigBuilder(pi4j)
                .id("i2c-bus-1")
                .name("I2C")
                .bus(1)
                .device(42)
                .build();

        var json = objectMapper.writeValueAsString(config);
        var otherConfig = objectMapper.readValue(json, I2CConfig.class);
        assertEquals(config.id(), otherConfig.id());
        assertEquals(config.bus(), otherConfig.bus());
        assertEquals(config.device(), otherConfig.device());
    }
}


package io.github.iamnicknack.pi4j.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.pi4j.Pi4J;
import com.pi4j.config.ConfigBuilder;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmConfig;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiBus;
import com.pi4j.io.spi.SpiConfig;
import com.pi4j.io.spi.SpiMode;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Jackson module to allow pi4j configurations to be deserialized when only their abstract type is known.
 */
public class Pi4jJacksonModule extends SimpleModule {

    public static ObjectMapper pi4jObjectMapper(Context pi4j) {
        return configureMapper(new ObjectMapper())
                .registerModule(new Pi4jJacksonModule(pi4j));
    }

    public static ObjectMapper configureMapper(ObjectMapper mapper) {
        return mapper
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public Pi4jJacksonModule(Context pi4j) {
        super("Pi4jJacksonModule");
        addDeserializer(DigitalInputConfig.class, new ConfigDeserializer<>(() -> DigitalInput.newConfigBuilder(pi4j)));
        addDeserializer(DigitalOutputConfig.class, new ConfigDeserializer<>(() -> DigitalOutput.newConfigBuilder(pi4j), Pi4jJacksonModule::digitalOutputMapEntryMapper));
        addDeserializer(I2CConfig.class, new ConfigDeserializer<>(() -> I2C.newConfigBuilder(pi4j)));
        addDeserializer(PwmConfig.class, new ConfigDeserializer<>(() -> Pwm.newConfigBuilder(pi4j), Pi4jJacksonModule::pwmMapEntryMapper));
        addDeserializer(SpiConfig.class, new ConfigDeserializer<>(() -> Spi.newConfigBuilder(pi4j), Pi4jJacksonModule::spiMapEntryMapper));
    }

    public Pi4jJacksonModule() {
        this(Pi4J.newContext());
    }

    static class ConfigDeserializer<B extends ConfigBuilder<B, T>, T> extends JsonDeserializer<T> {

        private final Supplier<ConfigBuilder<B, T>> configBuilderSupplier;
        private final UnaryOperator<Map.Entry<String, String>> mapEntryMapper;

        public ConfigDeserializer(Supplier<ConfigBuilder<B, T>> configBuilderSupplier) {
            this.configBuilderSupplier = configBuilderSupplier;
            this.mapEntryMapper = e -> Map.entry(e.getKey(), e.getValue());
        }

        public ConfigDeserializer(
                Supplier<ConfigBuilder<B, T>> configBuilderSupplier,
                UnaryOperator<Map.Entry<String, String>> mapEntryMapper
        ) {
            this.configBuilderSupplier = configBuilderSupplier;
            this.mapEntryMapper = mapEntryMapper;
        }

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Map<String, String> map = p.readValueAs(new TypeReference<Map<String, String>>() {});

            // filter null values from the map
            var filteredMap = map.entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .map(mapEntryMapper)
                    .filter(e -> e.getValue() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            ConfigBuilder<B, T> builder = configBuilderSupplier.get().load(filteredMap);
            return builder.build();
        }
    }

    private static Map.Entry<String, String> digitalOutputMapEntryMapper(Map.Entry<String, String> e) {
        return switch (e.getKey()) {
            case "initialState" -> Map.entry(DigitalOutputConfig.INITIAL_STATE_KEY, e.getValue());
            case "shutdownState" -> Map.entry(DigitalOutputConfig.SHUTDOWN_STATE_KEY, e.getValue());
            case "onState" -> Map.entry(DigitalOutputConfig.ON_STATE_KEY, e.getValue());
            default -> e;
        };
    }

    private static Map.Entry<String, String> pwmMapEntryMapper(Map.Entry<String, String> e) {
        return switch (e.getKey()) {
            case "pwmType" -> Map.entry(PwmConfig.PWM_TYPE_KEY, e.getValue());
            case "dutyCycle" -> Map.entry(PwmConfig.DUTY_CYCLE_KEY, e.getValue());
            case "shutdownValue" -> Map.entry(PwmConfig.SHUTDOWN_VALUE_KEY, e.getValue());
            default -> e;
        };
    }

    private static Map.Entry<String, String> spiMapEntryMapper(Map.Entry<String, String> e) {
        return switch (e.getKey()) {
            case SpiConfig.BUS_KEY -> Map.entry(SpiConfig.BUS_KEY, SpiBus.valueOf(e.getValue()).getBus() + "");
            case SpiConfig.MODE_KEY -> Map.entry(SpiConfig.MODE_KEY, SpiMode.valueOf(e.getValue()).getMode() + "");
            default -> e;
        };
    }
}

package io.github.iamnicknack.pi4j.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.pi4j.config.Config;
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
import com.pi4j.io.spi.SpiConfig;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Jackson module for pi4j configurations
 */
public class Pi4jJacksonModule extends SimpleModule {

    /**
     * Construct and configure a new mapper with the default settings for the specified pi4j context.
     * @param pi4j the pi4j context.
     * @return a new mapper.
     */
    public static ObjectMapper pi4jObjectMapper(Context pi4j) {
        return configureMapper(new ObjectMapper())
                .registerModule(new Pi4jJacksonModule(pi4j));
    }

    /**
     * Configure the mapper with the default settings which are appropriate for pi4j configuration objects.
     * @param mapper the mapper to configure.
     * @return the configured mapper.
     */
    public static ObjectMapper configureMapper(ObjectMapper mapper) {
        return mapper
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    /**
     * Construct a new module with the default Jackson settings
     * @param pi4j the pi4j context used by the config builders.
     */
    public Pi4jJacksonModule(Context pi4j) {
        super("Pi4jJacksonModule");
        addDeserializer(DigitalInputConfig.class, new ConfigDeserializer<>(() -> DigitalInput.newConfigBuilder(pi4j)));
        addDeserializer(DigitalOutputConfig.class, new ConfigDeserializer<>(() -> DigitalOutput.newConfigBuilder(pi4j)));
        addDeserializer(I2CConfig.class, new ConfigDeserializer<>(() -> I2C.newConfigBuilder(pi4j)));
        addDeserializer(PwmConfig.class, new ConfigDeserializer<>(() -> Pwm.newConfigBuilder(pi4j)));
        addDeserializer(SpiConfig.class, new ConfigDeserializer<>(() -> Spi.newConfigBuilder(pi4j)));

        addSerializer(DigitalInputConfig.class, new ConfigSerializer<>());
        addSerializer(DigitalOutputConfig.class, new ConfigSerializer<>());
        addSerializer(I2CConfig.class, new ConfigSerializer<>());
        addSerializer(PwmConfig.class, new ConfigSerializer<>());
        addSerializer(SpiConfig.class, new ConfigSerializer<>());
    }

    /**
     * Does not serialise the config object directly, but serialises the property map instead.
     * This allows the deserialiser to use existing pi4j builders to re-create the config object.
     * @param <T> the config type.
     */
    static class ConfigSerializer<T extends Config<T>> extends JsonSerializer<T> {
        @Override
        public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeObject(value.properties());
        }
    }

    /**
     * Deserialises a config object from a map of properties, as would be provided the config object
     * @param <B> the config builder type.
     * @param <T> the config type.
     */
    static class ConfigDeserializer<B extends ConfigBuilder<B, T>, T> extends JsonDeserializer<T> {
        private final Supplier<ConfigBuilder<B, T>> configBuilderSupplier;

        public ConfigDeserializer(Supplier<ConfigBuilder<B, T>> configBuilderSupplier) {
            this.configBuilderSupplier = configBuilderSupplier;
        }

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            Map<String, String> map = p.readValueAs(new TypeReference<Map<String, String>>() {});
            return configBuilderSupplier.get().load(map).build();
        }
    }
}

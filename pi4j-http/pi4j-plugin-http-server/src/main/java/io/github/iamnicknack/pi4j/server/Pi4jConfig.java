package io.github.iamnicknack.pi4j.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.context.Context;
import com.pi4j.context.ContextBuilder;
import com.pi4j.plugin.ffm.providers.gpio.DigitalInputFFMProviderImpl;
import com.pi4j.plugin.ffm.providers.gpio.DigitalOutputFFMProviderImpl;
import com.pi4j.plugin.ffm.providers.i2c.I2CFFMProviderImpl;
import com.pi4j.plugin.ffm.providers.pwm.PwmFFMProviderImpl;
import com.pi4j.plugin.ffm.providers.spi.SpiFFMProviderImpl;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProviderImpl;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProviderImpl;
import com.pi4j.plugin.mock.provider.i2c.MockI2CProviderImpl;
import com.pi4j.plugin.mock.provider.pwm.MockPwmProviderImpl;
import com.pi4j.plugin.mock.provider.spi.MockSpiProviderImpl;
import io.github.iamnicknack.pi4j.client.*;
import io.github.iamnicknack.pi4j.common.Pi4jJacksonModule;
import io.github.iamnicknack.pi4j.server.spring.ConditionalOnRaspberryPi;
import io.github.iamnicknack.pi4j.server.spring.EnablePi4jContextBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@EnablePi4jContextBuilder
@EnableConfigurationProperties({Pi4jConfig.Pi4jHttpProperties.class})
public class Pi4jConfig {

    private final Pi4jHttpProperties httpProperties;

    public Pi4jConfig(Pi4jHttpProperties httpProperties) {
        this.httpProperties = httpProperties;
    }

    /**
     * Object mapper which is able to work with existing Pi4j configuration classes
     */
    @Bean
    ObjectMapper objectMapper(Context pi4j) {
        return Pi4jJacksonModule.pi4jObjectMapper(pi4j);
    }

    /**
     * Default pi4j auto context
     */
    @ConditionalOnRaspberryPi
    @Bean(destroyMethod = "shutdown")
    @Primary
    @Profile("default")
    Context pi4j(ContextBuilder builder) {
        return builder
                .autoDetect()
                .build();
    }

    /**
     * Pi4j FFM context
     */
    @ConditionalOnRaspberryPi
    @Bean(destroyMethod = "shutdown")
    @Primary
    @Profile("ffm")
    Context pi4jFFM(ContextBuilder builder) {
        return builder
                .add(new DigitalOutputFFMProviderImpl())
                .add(new DigitalInputFFMProviderImpl())
                .add(new PwmFFMProviderImpl())
                .add(new I2CFFMProviderImpl())
                .add(new SpiFFMProviderImpl())
                .build();
    }

    /**
     * Default pi4j context with providers for supported devices registered
     */
    @Bean(destroyMethod = "shutdown")
    @Primary
    @Profile("proxy")
    Context pi4jProxy(ContextBuilder builder) {
        return builder
                .add(new HttpDigitalInputProvider(httpProperties.baseUrl))
                .add(new HttpDigitalOutputProvider(httpProperties.baseUrl))
                .add(new HttpSpiProvider(httpProperties.baseUrl))
                .add(new HttpPwmProvider(httpProperties.baseUrl))
                .add(new HttpI2CProvider(httpProperties.baseUrl))
                .build();
    }

    /**
     * Mock pi4j context used when the server is not running on a Raspberry Pi
     */
    @ConditionalOnMissingBean
    @Bean(destroyMethod = "shutdown")
    @Primary
    Context pi4jMock(ContextBuilder builder) {
        return builder
                .add(new MockDigitalInputProviderImpl())
                .add(new MockDigitalOutputProviderImpl())
                .add(new MockSpiProviderImpl())
                .add(new MockPwmProviderImpl())
                .add(new MockI2CProviderImpl())
                .build();
    }

    @ConfigurationProperties(prefix = "pi4j.http")
    public record Pi4jHttpProperties(
            @DefaultValue("http://localhost:8080")
            String baseUrl
    ) {
    }
}

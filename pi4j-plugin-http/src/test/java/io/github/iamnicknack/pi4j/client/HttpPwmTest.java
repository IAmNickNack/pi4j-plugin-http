package io.github.iamnicknack.pi4j.client;

import com.pi4j.context.Context;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmType;
import com.pi4j.plugin.mock.provider.pwm.MockPwm;
import io.github.iamnicknack.pi4j.client.test.ClientInstance;
import io.github.iamnicknack.pi4j.client.test.DefaultClientConfig;
import io.github.iamnicknack.pi4j.client.test.WiremockCaptureClientConfig;
import io.github.iamnicknack.pi4j.client.test.WiremockClientConfig;
import io.github.iamnicknack.pi4j.server.Pi4jServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = {
                Pi4jServer.class,
                HttpPwmTest.Config.class,
                WiremockCaptureClientConfig.class,
                WiremockClientConfig.class,
                DefaultClientConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"logging.level.com.pi4j=WARN"}
)
@ActiveProfiles("mock")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpPwmTest {

    private final Pwm localPwm;

    public HttpPwmTest(@Autowired @ClientInstance Context localPi4j) {
        this.localPwm = localPi4j.create(
                Pwm.newConfigBuilder(localPi4j)
                        .id("test-pwm")
                        .address(2)
                        .pwmType(PwmType.HARDWARE)
                        .frequency(2)
                        .dutyCycle(50)
                        .shutdown(0)
                        .build()
        );
    }

    @Test
    void canEnablePwm(@Autowired MockPwm remotePwm) {
        localPwm.on();
        assertTrue(remotePwm.isOn());
        localPwm.off();
        assertFalse(remotePwm.isOn());
    }

    @Test
    void canSetFrequency(@Autowired MockPwm remotePwm) {
        localPwm.frequency(100);
        assertEquals(100, remotePwm.getFrequency());
        assertEquals(100, localPwm.getFrequency());
        localPwm.frequency(200);
        assertEquals(200, remotePwm.getFrequency());
        assertEquals(200, localPwm.getFrequency());
    }

    @Test
    void canSetDutyCycle(@Autowired MockPwm remotePwm) {
        localPwm.dutyCycle(50);
        assertEquals(50, remotePwm.getDutyCycle());
        assertEquals(50, localPwm.getDutyCycle());
        localPwm.dutyCycle(75);
        assertEquals(75, remotePwm.getDutyCycle());
        assertEquals(75, localPwm.getDutyCycle());
    }

    @Configuration
    static class Config {

        @Bean
        @Lazy
        MockPwm remotePwm(Context remotePi4j) {
            return remotePi4j.registry().get("test-pwm");
        }

    }
}
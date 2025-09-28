package io.github.iamnicknack.pi4j.client;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutput;
import io.github.iamnicknack.pi4j.client.test.ClientInstance;
import io.github.iamnicknack.pi4j.client.test.DefaultClientConfig;
import io.github.iamnicknack.pi4j.client.test.WiremockCaptureClientConfig;
import io.github.iamnicknack.pi4j.client.test.WiremockClientConfig;
import io.github.iamnicknack.pi4j.server.Pi4jServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@SpringBootTest(
        classes = {
                Pi4jServer.class,
                HttpDigitalOutputTest.Config.class,
                WiremockCaptureClientConfig.class,
                WiremockClientConfig.class,
                DefaultClientConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "logging.level.com.pi4j=WARN",
                "logging.level.nws=DEBUG"
        }
)
@ActiveProfiles("mock")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpDigitalOutputTest {

    /**
     * The local output to test against.
     */
    private final DigitalOutput localOutput;

    /**
     * Count the events that have been received.
     */
    private final CountDownLatch latch = new CountDownLatch(3);


    /**
     * Create a new test instance.
     */
    public HttpDigitalOutputTest(@Autowired @ClientInstance Context localPi4j) {
        this.localOutput = localPi4j.create(
                DigitalOutput.newConfigBuilder(localPi4j)
                .id("test-output")
                .address(0)
                .build()
        );

        this.localOutput.addListener(ignored -> latch.countDown());
    }

    @AfterAll
    void afterAll(@Autowired @ClientInstance Context localPi4j) throws InterruptedException {
        localPi4j.shutdown();
        // check that all events were received
        if (!latch.await(5, TimeUnit.SECONDS)) {
            Assertions.fail("Not all events were received: " + latch.getCount());
        }
    }

    /**
     * Assert that it is possible to read the value of the remote input.
     * @param remoteOutput the remote input to test against.
     * @return a stream of dynamic tests for all possible states.
     */
    @TestFactory
    Stream<DynamicTest> canWriteValue(@Autowired MockDigitalOutput remoteOutput) {
        return Stream.of(DigitalState.LOW, DigitalState.HIGH, DigitalState.LOW)
                .map(state ->
                        dynamicTest(state.name(), () -> {
                            localOutput.state(state);
                            assertEquals(state, remoteOutput.state());
                        })
                );
    }

    @Configuration
    static class Config {

        /**
         * The output as configured on the remote pi4j instance.
         * <p>Lazily initialised as it is dependent on the local output creation</p>
         * @param remotePi4j the remote pi4j instance
         * @return A {@link MockDigitalOutput} configured for the remote pi4j instance.
         */
        @Bean
        @Lazy
        MockDigitalOutput remoteOutput(Context remotePi4j) {
            return remotePi4j.registry().get("test-output");
        }

    }
}
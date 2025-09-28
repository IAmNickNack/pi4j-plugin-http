package io.github.iamnicknack.pi4j.client;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInput;
import io.github.iamnicknack.pi4j.client.test.DefaultClientConfig;
import io.github.iamnicknack.pi4j.client.test.WiremockCaptureClientConfig;
import io.github.iamnicknack.pi4j.client.test.ClientInstance;
import io.github.iamnicknack.pi4j.client.test.WiremockClientConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;
import io.github.iamnicknack.pi4j.server.Pi4jServer;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@SpringBootTest(
        classes = {
                Pi4jServer.class,
                HttpDigitalInputTest.Config.class,
                WiremockCaptureClientConfig.class,
                WiremockClientConfig.class,
                DefaultClientConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "logging.level.com.pi4j=WARN",
                "logging.level.nws=INFO"
        }
)
@ActiveProfiles("mock")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpDigitalInputTest {

    /**
     * The local input to test against.
     */
    private final DigitalInput localInput;

    /**
     * Count the events that have been received.
     */
    private final CountDownLatch latch = new CountDownLatch(3);
    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * Create a new test instance.
     */
    public HttpDigitalInputTest(@Autowired @ClientInstance Context localPi4j) {
        this.localInput = localPi4j.create(
                DigitalInput.newConfigBuilder(localPi4j)
                .id("test-input")
                .address(0)
                .build()
        );

        this.localInput.addListener(ignored -> {
            counter.incrementAndGet();
            latch.countDown();
        });
    }

    @AfterAll
    void afterAll(@Autowired @ClientInstance Context localPi4j) throws InterruptedException {
        localPi4j.shutdown();
        // check that all events were received
        if (!latch.await(5, TimeUnit.SECONDS)) {
            Assertions.fail("Not all events were received: " + latch.getCount());
        }

        System.out.println(counter.get() + " events received.");
    }

    /**
     * Assert that it is possible to read the value of the remote input.
     * @param remoteInput the remote input to test against.
     * @return a stream of dynamic tests for all possible states.
     */
    @TestFactory
    Stream<DynamicTest> canReadValue(@Autowired MockDigitalInput remoteInput) {
        return Arrays.stream(DigitalState.values())
                .map(state ->
                        dynamicTest(state.name(), () -> {
                            remoteInput.mockState(state);
                            assertEquals(state, localInput.state());
                        })
                );
    }

    @Configuration
    static class Config {

        /**
         * The input as configured on the remote pi4j instance.
         * <p>Lazily initialised as it is dependent on the local input creation</p>
         * @param remotePi4j the remote pi4j instance
         * @return A {@link MockDigitalInput} configured for the remote pi4j instance.
         */
        @Bean
        @Lazy
        MockDigitalInput remoteInput(Context remotePi4j) {
            return remotePi4j.registry().get("test-input");
        }
    }

}
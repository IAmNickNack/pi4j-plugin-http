package io.github.iamnicknack.pi4j.client.test;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import io.github.iamnicknack.pi4j.client.*;
import io.github.iamnicknack.pi4j.client.requests.OkHttpRequests;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class DefaultClientConfig {

    @Bean(destroyMethod = "shutdown")
    @Lazy
    @ClientInstance
    Context localPi4j(@LocalServerPort int port) {

        var baseUrl = "http://localhost:" + port;
        var httpRequests = OkHttpRequests.INSTANCE.conditionalOnEventsource(baseUrl);

        return Pi4J.newContextBuilder()
                .add(new HttpDigitalInputProvider(baseUrl, httpRequests))
                .add(new HttpDigitalOutputProvider(baseUrl, httpRequests))
                .add(new HttpSpiProvider(baseUrl, httpRequests))
                .add(new HttpPwmProvider(baseUrl, httpRequests))
                .add(new HttpI2CProvider(baseUrl, httpRequests))
                .build();
    }
}

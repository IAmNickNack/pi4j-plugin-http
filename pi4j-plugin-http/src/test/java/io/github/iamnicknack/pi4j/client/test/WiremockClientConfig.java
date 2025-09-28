package io.github.iamnicknack.pi4j.client.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import io.github.iamnicknack.pi4j.client.*;
import io.github.iamnicknack.pi4j.client.*;
import io.github.iamnicknack.pi4j.client.requests.HttpRequests;
import io.github.iamnicknack.pi4j.client.requests.OkHttpRequests;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("wiremock")
public class WiremockClientConfig {

    @Bean
    @Lazy
    WireMockServer wireMockServer() {
        var wiremockServer = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort()
        );
        wiremockServer.start();
        return wiremockServer;
    }

    @Bean
    @Lazy
    @ClientInstance
    HttpRequests httpRequests() {
        var client = new OkHttpClient.Builder().build();
        return new OkHttpRequests(client);
    }

    @Bean(destroyMethod = "shutdown")
    @Lazy
    @ClientInstance
    Context localPi4j(@Autowired WireMockServer wireMockServer, @ClientInstance HttpRequests httpRequests) {
        return Pi4J.newContextBuilder()
                .add(new HttpDigitalInputProvider("http://localhost:" + wireMockServer.port(), httpRequests))
                .add(new HttpDigitalOutputProvider("http://localhost:" + wireMockServer.port(), httpRequests))
                .add(new HttpSpiProvider("http://localhost:" + wireMockServer.port(), httpRequests))
                .add(new HttpPwmProvider("http://localhost:" + wireMockServer.port(), httpRequests))
                .add(new HttpI2CProvider("http://localhost:" + wireMockServer.port(), httpRequests))
                .build();
    }
}

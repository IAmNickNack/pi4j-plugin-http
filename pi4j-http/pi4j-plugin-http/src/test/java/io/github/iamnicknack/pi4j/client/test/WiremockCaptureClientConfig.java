package io.github.iamnicknack.pi4j.client.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import io.github.iamnicknack.pi4j.client.*;
import io.github.iamnicknack.pi4j.client.requests.HttpRequests;
import io.github.iamnicknack.pi4j.client.requests.OkHttpRequests;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Configuration
@Profile("capture")
public class WiremockCaptureClientConfig {

    @Bean
    @Lazy
    WireMockServer wireMockServer(@LocalServerPort int port) {
        var wiremockServer = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort()
        );
        wiremockServer.stubFor(any(anyUrl()).willReturn(aResponse().proxiedFrom("http://localhost:" + port)));
        wiremockServer.start();
        return wiremockServer;
    }

    @Bean(destroyMethod = "stopRecording")
    @Lazy
    WiremockStubWriter wiremockStubWriter(@Autowired WireMockServer wireMockServer, @LocalServerPort int port) {
        return new WiremockStubWriter() {
            @Override
            public void startRecording() {
                wireMockServer.startRecording("http://localhost:" + port);
            }

            @Override
            public void stopRecording() {
                wireMockServer.stopRecording();
            }
        };
    }

    @Bean
    @Lazy
    @ClientInstance
    HttpRequests httpRequests(WireMockServer wireMockServer) {
        var client = new OkHttpClient.Builder()
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", wireMockServer.port())))
                .build();
        return new OkHttpRequests(client);
    }

    @Bean(destroyMethod = "shutdown")
    @Lazy
    @ClientInstance
    Context localPi4j(@LocalServerPort int port, @ClientInstance HttpRequests httpRequests) {
        return Pi4J.newContextBuilder()
                .add(new HttpDigitalInputProvider("http://localhost:" + port, httpRequests))
                .add(new HttpDigitalOutputProvider("http://localhost:" + port, httpRequests))
                .add(new HttpSpiProvider("http://localhost:" + port, httpRequests))
                .add(new HttpPwmProvider("http://localhost:" + port, httpRequests))
                .add(new HttpI2CProvider("http://localhost:" + port, httpRequests))
                .build();
    }

    public interface WiremockStubWriter {
        void startRecording();

        void stopRecording();
    }
}

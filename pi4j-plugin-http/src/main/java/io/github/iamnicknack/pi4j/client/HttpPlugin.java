package io.github.iamnicknack.pi4j.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.exception.InitializeException;
import com.pi4j.extension.Plugin;
import com.pi4j.extension.PluginService;
import com.pi4j.provider.Provider;
import io.github.iamnicknack.pi4j.client.requests.HttpRequests;
import io.github.iamnicknack.pi4j.client.requests.OkHttpRequests;
import io.github.iamnicknack.pi4j.common.Pi4jJacksonModule;
import okhttp3.OkHttpClient;

public class HttpPlugin implements Plugin {

    @Override
    public void initialize(PluginService service) throws InitializeException {
        String baseUrl = service.context().properties().has("pi4j.http.base-url")
                ? service.context().properties().get("pi4j.http.base-url")
                : "http://localhost:8080";

        ObjectMapper objectMapper = Pi4jJacksonModule
                .configureMapper(new ObjectMapper())
                .registerModule(new Pi4jJacksonModule(service.context()));

        HttpRequests httpRequests = new OkHttpRequests(new OkHttpClient(), objectMapper);

        Provider<?, ?, ?>[] providers = {
                new HttpDigitalInputProvider(baseUrl, httpRequests),
                new HttpDigitalOutputProvider(baseUrl, httpRequests),
                new HttpSpiProvider(baseUrl, httpRequests),
                new HttpPwmProvider(baseUrl, httpRequests),
                new HttpI2CProvider(baseUrl, httpRequests)
        };

        service.register(providers);
    }
}

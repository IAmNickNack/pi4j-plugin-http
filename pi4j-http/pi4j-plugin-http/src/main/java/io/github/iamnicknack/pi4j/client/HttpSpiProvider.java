package io.github.iamnicknack.pi4j.client;

import com.pi4j.boardinfo.util.BoardInfoHelper;
import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiConfig;
import com.pi4j.io.spi.SpiProvider;
import com.pi4j.io.spi.SpiProviderBase;
import io.github.iamnicknack.pi4j.client.requests.HttpRequests;
import io.github.iamnicknack.pi4j.client.requests.OkHttpRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpSpiProvider extends SpiProviderBase {

    public static final String PROVIDER_ID = "http-spi-plugin";

    private final Logger logger = LoggerFactory.getLogger(HttpSpiProvider.class);
    private final Map<String, HttpSpi> constructedDevices = new HashMap<>();

    final String baseUrl;
    final HttpRequests httpRequests;

    public HttpSpiProvider(String baseUrl, HttpRequests httpRequests) {
        this.id = PROVIDER_ID;
        this.name = "HTTP SPI Provider";
        this.baseUrl = baseUrl;
        this.httpRequests = httpRequests;
    }

    public HttpSpiProvider(String baseUrl) {
        this(baseUrl, OkHttpRequests.INSTANCE);
    }

    @Override
    public int getPriority() {
        return BoardInfoHelper.runningOnRaspberryPi() ? 0 : 500;
    }

    @Override
    public SpiProvider shutdownInternal(Context context) throws ShutdownException {
        logger.info("Shutting down HTTP SPI Provider");
        httpRequests.deleteJson(baseUrl + "/api/config/spi", constructedDevices.keySet(), Void.class);
        return super.shutdownInternal(context);
    }

    @Override
    public SpiProvider initialize(Context context) throws InitializeException {
        // load all existing configurations from the server
        var response = httpRequests.getJson(baseUrl + "/api/config/spi", SpiConfig[].class);
        // create and register each configured instance
        Arrays.stream(response)
                .forEach(config -> {
                    logger.info("Registering existing device: {}", config.id());
                    context.registry().add(new HttpSpi(this, config));
                });
        return super.initialize(context);
    }

    @Override
    public Spi create(SpiConfig config) {
        var response = httpRequests.postJson(baseUrl + "/api/config/spi", config, SpiConfig.class);
        var device = new HttpSpi(this, response);
        context.registry().add(device);
        constructedDevices.put(device.config().id(), device);
        return device;
    }
}

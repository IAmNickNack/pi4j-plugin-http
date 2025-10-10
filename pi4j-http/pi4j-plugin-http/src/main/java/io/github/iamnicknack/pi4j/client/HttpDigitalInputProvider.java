package io.github.iamnicknack.pi4j.client;

import com.pi4j.boardinfo.util.BoardInfoHelper;
import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalInputProvider;
import com.pi4j.io.gpio.digital.DigitalInputProviderBase;
import io.github.iamnicknack.pi4j.client.event.DigitalEventSourceFactory;
import io.github.iamnicknack.pi4j.client.requests.HttpRequests;
import io.github.iamnicknack.pi4j.client.requests.OkHttpRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpDigitalInputProvider extends DigitalInputProviderBase {

    public static final String PROVIDER_ID = "http-digital-input-plugin";

    private final Logger logger = LoggerFactory.getLogger(HttpDigitalInputProvider.class);
    private final Map<String, HttpDigitalInput> constructedDevices = new HashMap<>();

    private final String baseUrl;
    private final HttpRequests httpRequests;
    private final DigitalEventSourceFactory digitalEventSourceFactory;

    public HttpDigitalInputProvider(String baseUrl, HttpRequests httpRequests) {
        this.id = PROVIDER_ID;
        this.name = "HTTP Digital Input Provider";
        this.baseUrl = baseUrl;
        this.httpRequests = httpRequests;
        this.digitalEventSourceFactory = new DigitalEventSourceFactory(baseUrl);
    }

    public HttpDigitalInputProvider(String baseUrl) {
        this(baseUrl, OkHttpRequests.INSTANCE);
    }

    @Override
    public int getPriority() {
        return BoardInfoHelper.runningOnRaspberryPi() ? 0 : 500;
    }

    @Override
    public DigitalInputProvider initialize(Context context) throws InitializeException {
        // load all existing digital input configurations from the server
        var response = httpRequests.getJson(baseUrl + "/api/config/digitalinput", DigitalInputConfig[].class);
        // create and register each configured digital input instance
        Arrays.stream(response)
                .forEach(config -> {
                    logger.info("Registering existing device: {}", config.id());
                    context.registry().add(createDevice(config));
                });
        return super.initialize(context);
    }

    @Override
    public DigitalInputProvider shutdownInternal(Context context) throws ShutdownException {
        logger.info("Shutting down HTTP Digital Input Provider");
        // remove all locally created devices from the server registry
        httpRequests.deleteJson(baseUrl + "/api/config/digitalinput", constructedDevices.keySet(), Void.class);
        digitalEventSourceFactory.close();
        return super.shutdownInternal(context);
    }

    @Override
    public DigitalInput create(DigitalInputConfig config) {
        var response = httpRequests.postJson(baseUrl + "/api/config/digitalinput", config, DigitalInputConfig.class);
        var device = createDevice(response);
        context.registry().add(device);
        this.constructedDevices.put(response.id(), device);
        return device;
    }

    private HttpDigitalInput createDevice(DigitalInputConfig config) {
        return new HttpDigitalInput(this, config, this.httpRequests.withBaseUrl(baseUrl), digitalEventSourceFactory);
    }
}

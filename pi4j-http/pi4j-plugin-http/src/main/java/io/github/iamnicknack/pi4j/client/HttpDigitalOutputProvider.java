package io.github.iamnicknack.pi4j.client;

import com.pi4j.boardinfo.util.BoardInfoHelper;
import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalOutputProvider;
import com.pi4j.io.gpio.digital.DigitalOutputProviderBase;
import io.github.iamnicknack.pi4j.client.event.DigitalEventSourceFactory;
import io.github.iamnicknack.pi4j.client.requests.HttpRequests;
import io.github.iamnicknack.pi4j.client.requests.OkHttpRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpDigitalOutputProvider extends DigitalOutputProviderBase {

    public static final String PROVIDER_ID = "http-digital-output-plugin";

    private final Logger logger = LoggerFactory.getLogger(HttpDigitalOutputProvider.class);
    private final Map<String, HttpDigitalOutput> constructedDevices = new HashMap<>();

    private final String baseUrl;
    private final HttpRequests httpRequests;
    private final DigitalEventSourceFactory digitalEventSourceFactory;

    public HttpDigitalOutputProvider(String baseUrl, HttpRequests httpRequests) {
        this.id = PROVIDER_ID;
        this.name = "HTTP Digital Output Provider";
        this.baseUrl = baseUrl;
        this.httpRequests = httpRequests;
        this.digitalEventSourceFactory = new DigitalEventSourceFactory(baseUrl);
    }

    public HttpDigitalOutputProvider(String baseUrl) {
        this(baseUrl, OkHttpRequests.INSTANCE);
    }

    @Override
    public int getPriority() {
        return BoardInfoHelper.runningOnRaspberryPi() ? 0 : 500;
    }

    @Override
    public DigitalOutputProvider initialize(Context context) throws InitializeException {
        // load all existing digital output configurations from the server
        var response = httpRequests.getJson(baseUrl + "/api/config/digitaloutput", DigitalOutputConfig[].class);
        // create and register each configured digital output instance
        Arrays.stream(response)
                .forEach(config -> {
                    logger.info("Registering existing device: {}", config.id());
                    context.registry().add(createDevice(config));
                });
        return super.initialize(context);
    }

    @Override
    public DigitalOutputProvider shutdownInternal(Context context) throws ShutdownException {
        logger.info("Shutting down HTTP Digital Output Provider");
        // remove all locally created devices from the server registry
        httpRequests.deleteJson(baseUrl + "/api/config/digitaloutput", constructedDevices.keySet(), Void.class);
        digitalEventSourceFactory.close();
        return super.shutdownInternal(context);
    }

    @Override
    public DigitalOutput create(DigitalOutputConfig config) {
        var response = httpRequests.postJson(baseUrl + "/api/config/digitaloutput", config, DigitalOutputConfig.class);
        var device = createDevice(response);
        context.registry().add(device);
        constructedDevices.put(device.config().id(), device);
        return device;
    }

    private HttpDigitalOutput createDevice(DigitalOutputConfig config) {
        return new HttpDigitalOutput(this, config, this.httpRequests.withBaseUrl(baseUrl), digitalEventSourceFactory);
    }
}

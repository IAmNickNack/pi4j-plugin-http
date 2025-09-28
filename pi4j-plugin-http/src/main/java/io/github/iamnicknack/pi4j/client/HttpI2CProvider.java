package io.github.iamnicknack.pi4j.client;

import com.pi4j.boardinfo.util.BoardInfoHelper;
import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import com.pi4j.io.i2c.I2CProviderBase;
import io.github.iamnicknack.pi4j.client.requests.HttpRequests;
import io.github.iamnicknack.pi4j.client.requests.OkHttpRequests;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpI2CProvider extends I2CProviderBase {

    public static final String PROVIDER_ID = "http-i2c-plugin";

    private final String baseUrl;
    private final HttpRequests httpRequests;
    private final Map<String, I2C> constructedDevices = new HashMap<>();


    public HttpI2CProvider(String baseUrl, HttpRequests httpRequests) {
        this.id = PROVIDER_ID;
        this.name = "HTTP I2C Provider";
        this.httpRequests = httpRequests;
        this.baseUrl = baseUrl;
    }

    public HttpI2CProvider(String baseUrl) {
        this(baseUrl, OkHttpRequests.INSTANCE);
    }

    @Override
    public int getPriority() {
        return BoardInfoHelper.runningOnRaspberryPi() ? 0 : 500;
    }

    @Override
    public I2CProvider initialize(Context context) throws InitializeException {
        // load all existing digital output configurations from the server
        var response = httpRequests.getJson(baseUrl + "/api/config/i2c", I2CConfig[].class);
        // create and register each configured digital output instance
        Arrays.stream(response)
                .forEach(config -> {
                    logger.info("Registering existing device: {}", config.id());
                    context.registry().add(createDevice(config));
                });
        return super.initialize(context);
    }


    @Override
    public I2CProvider shutdown(Context context) throws ShutdownException {
        logger.info("Shutting down I2C Provider");
        // remove all locally created devices from the server registry
        httpRequests.deleteJson(baseUrl + "/api/config/i2c", constructedDevices.keySet(), Void.class);
        return super.shutdown(context);
    }


    @Override
    public I2C create(I2CConfig config) {
        var response = httpRequests.postJson(baseUrl + "/api/config/i2c", config, I2CConfig.class);
        var device = this.createDevice(response);
        this.constructedDevices.put(response.id(), device);
        context.registry().add(device);
        return device;
    }

    private HttpI2C createDevice(I2CConfig config) {
        return new HttpI2C(
                this,
                config,
                new HttpI2C.HttpI2COperations(httpRequests.withBaseUrl(baseUrl + "/api/i2c"),config.id()),
                httpRequests.withBaseUrl(baseUrl)
        );
    }
}

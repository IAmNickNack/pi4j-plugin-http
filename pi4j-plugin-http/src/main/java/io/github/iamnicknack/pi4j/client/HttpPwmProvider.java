package io.github.iamnicknack.pi4j.client;

import com.pi4j.boardinfo.util.BoardInfoHelper;
import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmConfig;
import com.pi4j.io.pwm.PwmProvider;
import com.pi4j.io.pwm.PwmProviderBase;
import io.github.iamnicknack.pi4j.client.requests.HttpRequests;
import io.github.iamnicknack.pi4j.client.requests.OkHttpRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpPwmProvider extends PwmProviderBase {

    public static final String PROVIDER_ID = "http-pwm-plugin";

    private final Logger logger = LoggerFactory.getLogger(HttpPwmProvider.class);
    private final Map<String, HttpPwm> constructedDevices = new HashMap<>();

    final String baseUrl;
    final HttpRequests httpRequests;

    public HttpPwmProvider(String baseUrl, HttpRequests httpRequests) {
        this.id = PROVIDER_ID;
        this.name = "HTTP PWM Provider";
        this.baseUrl = baseUrl;
        this.httpRequests = httpRequests;
    }

    public HttpPwmProvider(String baseUrl) {
        this(baseUrl, OkHttpRequests.INSTANCE);
    }

    @Override
    public int getPriority() {
        return BoardInfoHelper.runningOnRaspberryPi() ? 0 : 500;
    }

    @Override
    public PwmProvider shutdown(Context context) throws ShutdownException {
        logger.info("Shutting down HTTP PWM Provider");
        httpRequests.deleteJson(baseUrl + "/api/config/pwm", constructedDevices.keySet(), Void.class);
        return super.shutdown(context);
    }

    @Override
    public PwmProvider initialize(Context context) throws InitializeException {
        // load all existing configurations from the server
        var response = httpRequests.getJson(baseUrl + "/api/config/pwm", PwmConfig[].class);
        // create and register each configured instance
        Arrays.stream(response)
                .forEach(config -> {
                    logger.info("Registering existing device: {}", config.id());
                    context.registry().add(new HttpPwm(this, config));
                });
        return super.initialize(context);
    }

    @Override
    public Pwm create(PwmConfig config) {
        var response = httpRequests.postJson(baseUrl + "/api/config/pwm", config, PwmConfig.class);
        var device = new HttpPwm(this, response);
        context.registry().add(device);
        constructedDevices.put(device.config().id(), device);
        return device;
    }
}

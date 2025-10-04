package io.github.iamnicknack.pi4j.client;

import com.pi4j.context.Context;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.exception.IOException;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmBase;
import com.pi4j.io.pwm.PwmConfig;
import io.github.iamnicknack.pi4j.client.requests.HttpRequests;

public class HttpPwm extends PwmBase {

    private final String baseUrl;
    private final HttpRequests httpRequests;

    public HttpPwm(HttpPwmProvider provider, PwmConfig config) {
        super(provider, config);
        this.baseUrl = provider.baseUrl;
        this.httpRequests = provider.httpRequests;
    }

    @Override
    public Pwm shutdownInternal(Context context) throws ShutdownException {
        super.shutdownInternal(context);
        try {
            httpRequests.deleteJson(baseUrl + "/api/config/pwm/" + this.getId(), Void.class);
        } catch (HttpRequests.HttpException e) {
            throw new ShutdownException(e);
        }
        return this;
    }

    @Override
    public Pwm on() throws IOException {
        httpRequests.putJson(baseUrl + "/api/pwm/" + config.id() + "/1", Integer.class);
        return this;
    }

    @Override
    public Pwm off() throws IOException {
        httpRequests.putJson(baseUrl + "/api/pwm/" + config.id() + "/0", Integer.class);
        return this;
    }

    @Override
    public void setDutyCycle(Integer dutyCycle) throws IOException {
        super.setDutyCycle(dutyCycle);
        httpRequests.putJson(baseUrl + "/api/pwm/" + config.id() + "/dutycycle/" + dutyCycle, Integer.class);
    }

    @Override
    public Integer getDutyCycle() throws IOException {
        return httpRequests.getJson(baseUrl + "/api/pwm/" + config.id() + "/dutycycle", Integer.class);
    }

    @Override
    public void setFrequency(int frequency) throws IOException {
        super.setFrequency(frequency);
        httpRequests.putJson(baseUrl + "/api/pwm/" + config.id() + "/frequency/" + frequency, Integer.class);
    }

    public int getFrequency() throws IOException {
        return httpRequests.getJson(baseUrl + "/api/pwm/" + config.id() + "/frequency", Integer.class);
    }
}

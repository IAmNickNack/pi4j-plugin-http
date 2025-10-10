package io.github.iamnicknack.pi4j.client;

import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputBase;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalState;
import io.github.iamnicknack.pi4j.client.event.DigitalEventSourceFactory;
import io.github.iamnicknack.pi4j.client.requests.HttpRequests;
import okhttp3.sse.EventSource;
import org.slf4j.LoggerFactory;

public class HttpDigitalInput extends DigitalInputBase {

    private final HttpRequests httpRequests;
    private final DigitalEventSourceFactory eventSourceFactory;
    private EventSource eventSource;

    HttpDigitalInput(
            HttpDigitalInputProvider provider,
            DigitalInputConfig config,
            HttpRequests httpRequests,
            DigitalEventSourceFactory eventSourceFactory
    ) {
        super(provider, config);
        this.logger = LoggerFactory.getLogger(HttpDigitalInput.class.getName() + "::" + config.id());
        this.httpRequests = httpRequests;
        this.eventSourceFactory = eventSourceFactory;
    }

    @Override
    public DigitalInput initialize(Context context) throws InitializeException {
        super.initialize(context);
        this.eventSource = eventSourceFactory.start(this, this.stateChangeEventManager::dispatch);
        return this;
    }

    @Override
    public DigitalInput shutdownInternal(Context context) throws ShutdownException {
        super.shutdownInternal(context);
        eventSource.cancel();
        try {
            httpRequests.deleteJson(String.format("/api/config/digitalinput/%s", this.config.id()), Void.class);
        } catch (HttpRequests.HttpException e) {
            throw new ShutdownException(e);
        }
        return this;
    }

    @Override
    public DigitalState state() {
        var response = httpRequests.getJson(String.format("/api/digitalinput/%s", this.config.id()), Integer.class);
        return DigitalState.state(response);
    }
}

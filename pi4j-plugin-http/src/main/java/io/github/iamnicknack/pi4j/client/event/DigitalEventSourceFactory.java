package io.github.iamnicknack.pi4j.client.event;

import com.pi4j.io.gpio.digital.Digital;
import com.pi4j.io.gpio.digital.DigitalConfig;
import com.pi4j.io.gpio.digital.DigitalProvider;
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Factory for creating and starting event sources for a given digital input device.
 */
public class DigitalEventSourceFactory implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build();

    /**
     * Custom event source factory that closes the connection after the last event source is cancelled.
     */
    private final EventSource.Factory eventSourceFactory = EventSources.createFactory(client);

    /**
     * The base url of the server.
     */
    private final String baseUrl;

    /**
     * Create a new event source factory for the given base url.
     * @param baseUrl the base url of the server.
     */
    public DigitalEventSourceFactory(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void close() {
        logger.info("Shutting down event source factory");
        client.dispatcher().executorService().shutdown();
    }

    /**
     * Start a new event source for the given device.
     * @param device the device for which to create the event source.
     * @param dispatchEvent the event dispatch function.
     * @return the event source.
     * @param <T> the device type.
     * @param <C> the configuration type.
     * @param <P> the provider type.
     */
    public <
            T extends Digital<T, C, P>,
            C extends DigitalConfig<C>,
            P extends DigitalProvider<P, T, C>
            >
    EventSource start(T device, Consumer<DigitalStateChangeEvent<T>> dispatchEvent) {

        logger.info("Starting event source for device {}", device.id());

        var sseRequest = new Request.Builder()
                .url(baseUrl + String.format("/api/events/%s", device.id()))
                .build();

        var listener = new DigitalStateChangeEventSourceListener<>(device, dispatchEvent);
        return eventSourceFactory.newEventSource(sseRequest, listener);
    }
}

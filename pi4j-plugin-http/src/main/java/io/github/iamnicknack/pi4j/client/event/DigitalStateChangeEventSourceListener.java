package io.github.iamnicknack.pi4j.client.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.io.gpio.digital.*;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Event source listener that listens for state change events sent by the server as SSE events.
 */
class DigitalStateChangeEventSourceListener<
        T extends Digital<T, C, P>,
        C extends DigitalConfig<C>,
        P extends DigitalProvider<P, T, C>
        > extends EventSourceListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final T device;
    private final Consumer<DigitalStateChangeEvent<T>> dispatchEvent;

    public DigitalStateChangeEventSourceListener(T device, Consumer<DigitalStateChangeEvent<T>> dispatchEvent) {
        this.device = device;
        this.dispatchEvent = dispatchEvent;
    }

    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        if (Optional.ofNullable(type).filter(s -> s.equals("state-change")).isPresent()) {
            DigitalState state;
            try {
                state = objectMapper.readValue(data, DigitalState.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            var changeEvent = new DigitalStateChangeEvent<>(device, state);

            if (logger.isDebugEnabled()) {
                logger.debug("Received event: {}", changeEvent);
            }

            dispatchEvent.accept(changeEvent);
        } else {
            logger.debug("Not a state change event: {}", data);
        }
    }

    @Override
    public void onOpen(EventSource eventSource, Response response) {
        logger.info("Event source opened");
        super.onOpen(eventSource, response);
    }

    @Override
    public void onClosed(EventSource eventSource) {
        logger.info("Event source closed");
        super.onClosed(eventSource);
    }

    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        if (t instanceof IOException e && e.getMessage().equalsIgnoreCase("canceled")) {
            logger.info("Event source canceled");
        }
        else {
            logger.error("Event source failure", t);
        }
    }
}

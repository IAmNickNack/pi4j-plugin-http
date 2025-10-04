package io.github.iamnicknack.pi4j.server.router;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/events")
public class DigitalEventsController implements ApplicationListener<ContextClosedEvent> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Emitters to clean up when the context is closed.
     */
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private final Context pi4j;

    public DigitalEventsController(Context pi4j) {
        this.pi4j = pi4j;
    }

    /**
     * Subscribe to an event stream for the given device.
     * @param id the device id.
     * @return a response entity containing the event stream.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SseEmitter> subscribeToDevice(@PathVariable("id") String id, HttpServletRequest request) {
        if (!pi4j.registry().exists(id)) {
            return ResponseEntity.notFound().build();
        }

        logger.info("Subscribing client to device {}", id);

        var device = pi4j.registry().<Digital<?, ?, ?>>get(id);
        var emitter = new SseEmitter(Long.MAX_VALUE);

        DigitalStateChangeListener listener = event -> {
            var events = SseEmitter.event()
                    .name("state-change")
                    .data(event.state())
                    .build();

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Sending state-change event to client: {}, {}:{}",
                        event.state(),
                        request.getRemoteAddr(),
                        request.getRemotePort()
                );
            }

            try {
                emitter.send(events);
            } catch (IOException e) {
                emitter.completeWithError(e);
                logger.error("Failed to send event to client", e);
                throw new RuntimeException(e);
            }
        };

        emitter.onError(error -> {
            logger.error("Error in client event stream", error);
            emitters.remove(emitter);
            device.removeListener(listener);
        });

        emitter.onCompletion(() -> {
            logger.info("Client unsubscribed from device {}", id);
            emitters.remove(emitter);
            device.removeListener(listener);
        });

        emitters.add(emitter);
        device.addListener(listener);

        return ResponseEntity.ok().body(emitter);
    }

    @Deprecated(forRemoval = true)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unsubscribeFromDevice(@PathVariable("id") String id) {
        logger.warn("[Deprecated API] Unsubscribed client from device {}", id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/application")
    public ResponseEntity<SseEmitter> subscribeToApplicationEvents() {
        var emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onError(error -> logger.error("Error in application event stream", error));
        emitter.onCompletion(() -> logger.info("Client unsubscribed from application events"));

        emitters.add(emitter);
        return ResponseEntity.ok().body(emitter);
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        logger.info("Stopping SSE event forwarders");
        emitters.forEach(SseEmitter::complete);
    }
}

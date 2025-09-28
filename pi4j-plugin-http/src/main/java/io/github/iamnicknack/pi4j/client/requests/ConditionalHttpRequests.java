package io.github.iamnicknack.pi4j.client.requests;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * {@link HttpRequests} implementation that executes requests only when the condition is met.
 */
class ConditionalHttpRequests implements HttpRequests {

    private final HttpRequests delegate;

    private final Supplier<Boolean> conditionSupplier;

    public ConditionalHttpRequests(HttpRequests delegate, Supplier<Boolean> conditionSupplier) {
        this.delegate = delegate;
        this.conditionSupplier = conditionSupplier;
    }

    @Override
    public <T> T getJson(String url, Class<T> responseType) {
        if (!conditionSupplier.get()) {
            return delegate.getJson(url, responseType);
        }
        return null;
    }

    @Override
    public <T> T putJson(String url, Class<T> responseType) {
        if (!conditionSupplier.get()) {
            return delegate.putJson(url, responseType);
        }
        return null;
    }

    @Override
    public <T> T postJson(String url, Object body, Class<T> responseType) {
        if (!conditionSupplier.get()) {
            return delegate.postJson(url, body, responseType);
        }
        return null;
    }

    @Override
    public <T> T deleteJson(String url, Class<T> responseType) {
        if (!conditionSupplier.get()) {
            return delegate.deleteJson(url, responseType);
        }
        return null;
    }

    @Override
    public <T> T deleteJson(String url, Object body, Class<T> responseType) {
        if (!conditionSupplier.get()) {
            return delegate.deleteJson(url, body, responseType);
        }
        return null;
    }

    /**
     * Listens for server events and sets the shutdown flag when the server is shut down.
     */
    static class ShutdownEventSourceListener extends EventSourceListener implements Supplier<Boolean> {

        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final AtomicBoolean isShutdown = new AtomicBoolean(false);

        ShutdownEventSourceListener(String baseUrl) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .build();
            EventSource.Factory eventSourceFactory = EventSources.createFactory(client);
            eventSourceFactory.newEventSource(new Request.Builder().url(baseUrl + "/api/events/application").build(), this);
        }

        @Override
        public Boolean get() {
            return isShutdown.get();
        }

        @Override
        public void onClosed(EventSource eventSource) {
            logger.info("Server event source closed");
            isShutdown.set(true);
        }
    }
}

package io.github.iamnicknack.pi4j.client.requests;

import io.github.iamnicknack.pi4j.common.ErrorResponsePayload;

import java.util.function.Supplier;

/**
 * HTTP request helper methods.
 */
public interface HttpRequests {

    /**
     * Gets the given url and read the response.
     * @param url the endpoint url.
     * @param responseType the expected response type.
     * @return the response value.
     * @param <T> the response type.
     */
    <T> T getJson(String url, Class<T> responseType);

    /**
     * Puts the given url and read the response.
     * @param url the endpoint url.
     * @param responseType the expected response type.
     * @return the response value.
     * @param <T> the response type.
     */
    <T> T putJson(String url, Class<T> responseType);

    /**
     * Posts the given body as JSON and read the response.
     * @param url the endpoint url.
     * @param body the body to post.
     * @param responseType the expected response type.
     * @return the response value.
     * @param <T> the response type.
     */
    <T> T postJson(String url, Object body, Class<T> responseType);

    /**
     * Deletes the given url and return the response type.
     * @param url the endpoint url.
     * @param responseType the expected response type.
     * @return the response value.
     * @param <T> the response type.
     */
    <T> T deleteJson(String url, Class<T> responseType);

    /**
     * Deletes the given body as JSON.
     * @param url the endpoint url.
     * @param body the body to delete.
     * @param responseType the expected response type.
     * @return the response value.
     * @param <T> the response type.
     */
    <T> T deleteJson(String url, Object body, Class<T> responseType);

    /**
     * Creates a new instance of {@link HttpRequests} which uses the given base url for all requests.
     * @param baseUrl the base url.
     * @return a new instance of {@link HttpRequests}
     */
    default HttpRequests withBaseUrl(String baseUrl) {
        return new HttpRequests() {
            @Override
            public <T> T getJson(String url, Class<T> responseType) {
                return HttpRequests.this.getJson(baseUrl + url, responseType);
            }

            @Override
            public <T> T putJson(String url, Class<T> responseType) {
                return HttpRequests.this.putJson(baseUrl + url, responseType);
            }

            @Override
            public <T> T postJson(String url, Object body, Class<T> responseType) {
                return HttpRequests.this.postJson(baseUrl + url, body, responseType);
            }

            @Override
            public <T> T deleteJson(String url, Class<T> responseType) {
                return HttpRequests.this.deleteJson(baseUrl + url, responseType);
            }

            @Override
            public <T> T deleteJson(String url, Object body, Class<T> responseType) {
                return HttpRequests.this.deleteJson(baseUrl + url, body, responseType);
            }

            @Override
            public HttpRequests withBaseUrl(String baseUrl) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Creates a new instance of {@link HttpRequests} which executes only when the given condition is true.
     * @param conditionSupplier the condition supplier.
     * @return a new instance of {@link HttpRequests}
     */
    default HttpRequests conditional(Supplier<Boolean> conditionSupplier) {
        return new ConditionalHttpRequests(this, conditionSupplier);
    }

    /**
     * Creates a new instance of {@link HttpRequests} which executes only when the given event source is shutdown.
     * @param baseUrl the base url for the event source.
     * @return a new instance of {@link HttpRequests}
     */
    default HttpRequests conditionalOnEventsource(String baseUrl) {
        return conditional(new ConditionalHttpRequests.ShutdownEventSourceListener(baseUrl));
    }

    /**
     * Exception thrown when the HTTP request fails.
     */
    class HttpException extends RuntimeException {
        private final int statusCode;
        private final ErrorResponsePayload errorResponse;

        public HttpException(int statusCode, ErrorResponsePayload errorResponse, Throwable cause) {
            super(errorResponse.message(), cause);
            this.statusCode = statusCode;
            this.errorResponse = errorResponse;
        }

        public HttpException(int statusCode, ErrorResponsePayload errorResponse) {
            this(statusCode, errorResponse, null);
        }

        public HttpException(int statusCode, String message) {
            this(statusCode, new ErrorResponsePayload(message), null);
        }

        public HttpException(int statusCode, Throwable cause) {
            this(statusCode, new ErrorResponsePayload(cause), cause);
        }

        public int getStatusCode() {
            return statusCode;
        }

        public ErrorResponsePayload getErrorResponse() {
            return errorResponse;
        }
    }
}

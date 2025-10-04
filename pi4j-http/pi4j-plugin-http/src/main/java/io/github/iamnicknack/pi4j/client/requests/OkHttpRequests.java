package io.github.iamnicknack.pi4j.client.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.Pi4J;
import io.github.iamnicknack.pi4j.common.ErrorResponsePayload;
import io.github.iamnicknack.pi4j.common.Pi4jJacksonModule;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class OkHttpRequests implements HttpRequests {

    public static final OkHttpRequests INSTANCE = new OkHttpRequests();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    private final Logger logger = LoggerFactory.getLogger(OkHttpRequests.class);

    private final ObjectMapper objectMapper;

    private final OkHttpClient client;

    public OkHttpRequests() {
        this(new OkHttpClient());
    }

    public OkHttpRequests(OkHttpClient client) {
        this.client = client;
        this.objectMapper = Pi4jJacksonModule
                .configureMapper(new ObjectMapper())
                .registerModule(new Pi4jJacksonModule(Pi4J.newContext()));
    }

    public OkHttpRequests(OkHttpClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T getJson(String url, Class<T> responseType) {
        var request = new Request.Builder().url(url).build();
        return sendRequest(request, responseType);
    }

    @Override
    public <T> T putJson(String url, Class<T> responseType) {
        var request = new Request.Builder().url(url).put(RequestBody.EMPTY).build();
        return sendRequest(request, responseType);
    }

    @Override
    public <T> T postJson(String url, Object body, Class<T> responseType) {
        try {
            var bytes = objectMapper.writeValueAsBytes(body);
            var requestBody = RequestBody.create(bytes, MEDIA_TYPE_JSON);
            var request = new Request.Builder().url(url).post(requestBody).build();
            return sendRequest(request, responseType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deleteJson(String url, Class<T> responseType) {
        var request = new Request.Builder().url(url).delete().build();
        return sendRequest(request, responseType);
    }

    @Override
    public <T> T deleteJson(String url, Object body, Class<T> responseType) {
        try {
            var bytes = objectMapper.writeValueAsBytes(body);
            var requestBody = RequestBody.create(bytes, MEDIA_TYPE_JSON);
            var request = new Request.Builder().url(url).delete(requestBody).build();
            return sendRequest(request, responseType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T sendRequest(Request request, Class<T> responseType) {
        try (var response = client.newCall(request).execute()) {

            logger.debug(
                    "Request: {}, {}, Response: Status: {}, Headers: {}",
                    request.method(),
                    request.url(),
                    response.code(),
                    response.headers()
            );

            if (response.isSuccessful()) {
                if (responseType != Void.class && isJsonResponse(response)) {
                    return objectMapper.readValue(response.body().byteStream(), responseType);
                }
            }
            else {
                if (isJsonResponse(response)) {
                    var jsonNode = objectMapper.readValue(response.body().byteStream(), JsonNode.class);
                    if (jsonNode.isObject()) {
                        ErrorResponsePayload errorResponse = objectMapper.convertValue(jsonNode, ErrorResponsePayload.class);
                        throw new HttpRequests.HttpException(response.code(), errorResponse);
                    }
                }
                else {
                    throw new HttpRequests.HttpException(response.code(), "HTTP error with status code " + response.code());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static boolean isJsonResponse(Response response) {
        return Optional.ofNullable(response.body().contentType())
                .filter(mediaType -> mediaType.equals(MEDIA_TYPE_JSON))
                .isPresent();
    }
}

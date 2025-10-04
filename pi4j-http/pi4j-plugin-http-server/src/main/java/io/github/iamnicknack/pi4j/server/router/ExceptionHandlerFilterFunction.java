package io.github.iamnicknack.pi4j.server.router;

import com.pi4j.exception.Pi4JException;
import com.pi4j.io.exception.IOAlreadyExistsException;
import com.pi4j.io.exception.IONotFoundException;
import io.github.iamnicknack.pi4j.common.ErrorResponsePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Component
public class ExceptionHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private final Logger logger = LoggerFactory.getLogger(ExceptionHandlerFilterFunction.class);

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        try {
            return next.handle(request);
        } catch (IOAlreadyExistsException e) {
            logger.warn(e.getMessage(), e);
            return ServerResponse.status(409).body(new ErrorResponsePayload(e));
        } catch (IONotFoundException e) {
            logger.warn(e.getMessage(), e);
            return ServerResponse.status(404).body(new ErrorResponsePayload(e));
        } catch (Pi4JException e) {
            logger.warn(e.getMessage(), e);
            return ServerResponse.badRequest().body(new ErrorResponsePayload(e));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ServerResponse.status(500).body(new ErrorResponsePayload(e));
        }
    }
}

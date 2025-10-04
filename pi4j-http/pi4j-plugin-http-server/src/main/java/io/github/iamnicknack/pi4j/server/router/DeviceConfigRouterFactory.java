package io.github.iamnicknack.pi4j.server.router;

import com.pi4j.config.Config;
import com.pi4j.context.Context;
import com.pi4j.io.IO;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Arrays;

/**
 * Factory which provides create, query and delete device configuration endpoints.
 * <p>
 * These operations are generic enough to work with any device type.
 * </p>
 */
@Component
public class DeviceConfigRouterFactory {

    private final Context pi4j;
    private final ExceptionHandlerFilterFunction exceptionHandler;

    public DeviceConfigRouterFactory(Context pi4j, ExceptionHandlerFilterFunction exceptionHandler) {
        this.pi4j = pi4j;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Creates a router function for the given device type.
     * @param baseUrl the base url for the device configuration endpoints.
     * @param deviceFactory the factory which creates the device instance.
     * @param deviceType the device type for which the endpoints should be created.
     * @param configType the configuration type for which the endpoints should be created.
     * @return a router function for the given device type.
     * @param <T> the device type.
     * @param <C> the configuration type.
     */
    public <T extends IO<?, ?, ?>, C extends Config<?>> RouterFunction<ServerResponse> routerFunction(
            String baseUrl,
            DeviceFactory<C, T> deviceFactory,
            Class<T> deviceType,
            Class<C> configType
    ) {

        var logger = LoggerFactory.getLogger(getClass() + "::" + deviceType.getSimpleName());

        return RouterFunctions.route()
                // list all device configurations
                .GET(baseUrl, _ -> {
                    var configs = pi4j.registry().allByType(deviceType).values().stream()
                            .map(IO::config)
                            .filter(configType::isInstance)
                            .map(configType::cast)
                            .toList();

                    return ServerResponse.ok().body(configs);
                })
                // get device configuration by id
                .GET(baseUrl + "/{id}", request -> {
                    var id = request.pathVariable("id");
                    var device = pi4j.registry().get(id);
                    return ServerResponse.ok().body(device.config());
                })
                // remove a device by id
                .DELETE(baseUrl + "/{id}", request -> {
                    logger.info("Removing {} device with id {}", deviceType.getSimpleName(), request.pathVariable("id"));
                    var id = request.pathVariable("id");
                    pi4j.registry().remove(id);
                    return ServerResponse.ok().build();
                })
                // remove a device by id
                .DELETE(baseUrl, request -> {
                    var devicesToRemove = request.body(String[].class);
                    logger.info("Removing {} devices with ids {}", deviceType.getSimpleName(), Arrays.toString(devicesToRemove));
                    Arrays.stream(devicesToRemove)
                            .filter(id -> pi4j.registry().exists(id))
                            .forEach(id -> pi4j.registry().remove(id));
                    return ServerResponse.ok().build();
                })
                // create a new device from configuration
                .POST(baseUrl, request -> {
                    var config = request.body(configType);
                    logger.info("Creating new {} device with id {}", deviceType.getSimpleName(), config.id());
                    var device = deviceFactory.create(pi4j, config);
                    return ServerResponse.ok().body(device.config());
                })
                .build()
                .filter(exceptionHandler);
    }

    public interface DeviceFactory<C, T> {
        T create(Context pi4j, C config);
    }
}

package io.github.iamnicknack.pi4j.server.router;

import com.pi4j.config.Config;
import com.pi4j.context.Context;
import com.pi4j.io.IO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class RegistryController {

    private final Context pi4j;

    public RegistryController(Context pi4j) {
        this.pi4j = pi4j;
    }

    @Bean
    RouterFunction<ServerResponse> registryRoutes(ExceptionHandlerFilterFunction exceptionHandler) {
        return RouterFunctions.route()
                .GET("/api/config/registry", _ -> {
                    var devices = pi4j.registry().all().values().stream()
                            .map(DeviceInfo::new)
                            .toList();
                    return ServerResponse.ok().body(devices);
                })
                .DELETE("/api/config/registry", _ -> {
                    pi4j.registry().all().keySet()
                            .forEach(id -> pi4j.registry().remove(id));
                    return ServerResponse.ok().build();
                })
                .DELETE("/api/config/registry/{id}", request -> {
                    var id = request.pathVariable("id");
                    pi4j.registry().remove(id);
                    return ServerResponse.ok().build();
                })
                .build()
                .filter(exceptionHandler);
    }

    public record DeviceInfo(String id, String type, Config<?> config) {
        DeviceInfo(IO<?, ?, ?> device) {
            this(device.id(), device.type().name(), device.config());
        }
    }
}

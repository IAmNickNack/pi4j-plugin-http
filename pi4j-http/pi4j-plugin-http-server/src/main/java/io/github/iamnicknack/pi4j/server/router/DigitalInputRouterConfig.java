package io.github.iamnicknack.pi4j.server.router;

import com.pi4j.context.Context;
import com.pi4j.internal.IOCreator;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInput;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class DigitalInputRouterConfig {

    private static final String TRUTHY_VALUES = "1,on,true,yes,high";

    private final Context pi4j;

    public DigitalInputRouterConfig(Context pi4j) {
        this.pi4j = pi4j;
    }

    @Bean
    RouterFunction<ServerResponse> digitalInputRoutes(ExceptionHandlerFilterFunction exceptionHandler) {
        return RouterFunctions.route()
                .GET("/api/digitalinput/{id}", request -> {
                    var id = request.pathVariable("id");
                    var device = pi4j.registry().<DigitalInput>get(id);
                    return ServerResponse.ok().body(device.state().value());
                })
                .PUT("/api/digitalinput/{id}/{value}", request -> {
                    var id = request.pathVariable("id");
                    var value = request.pathVariable("value");
                    var device = pi4j.registry().<MockDigitalInput>get(id);
                    device.mockState((TRUTHY_VALUES.contains(value.toLowerCase())) ? DigitalState.HIGH : DigitalState.LOW);
                    return ServerResponse.ok().build();
                })
                .build()
                .filter(exceptionHandler);
    }

    @Bean
    RouterFunction<ServerResponse> digitalInputConfigRoutes(DeviceConfigRouterFactory factory) {
        return factory.routerFunction(
                "/api/config/digitalinput",
                (pi4j1, config) -> pi4j1.create(config),
                DigitalInput.class,
                DigitalInputConfig.class
        );
    }
}

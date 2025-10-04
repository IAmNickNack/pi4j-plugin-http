package io.github.iamnicknack.pi4j.server.router;

import com.pi4j.context.Context;
import com.pi4j.internal.IOCreator;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class DigitalOutputRouterConfig {

    private static final String TRUTHY_VALUES = "1,on,true,yes,high";

    private final Context pi4j;

    public DigitalOutputRouterConfig(Context pi4j) {
        this.pi4j = pi4j;
    }

    @Bean
    RouterFunction<ServerResponse> digitalOutputRoutes(ExceptionHandlerFilterFunction exceptionHandler) {
        return RouterFunctions.route()
                .GET("/api/digitaloutput/{id}", request -> {
                    var id = request.pathVariable("id");
                    var device = pi4j.registry().<DigitalOutput>get(id);
                    return ServerResponse.ok().body(device.state().value());
                })
                .PUT("/api/digitaloutput/{id}/{value}", request -> {
                    var id = request.pathVariable("id");
                    var value = request.pathVariable("value");
                    var on = TRUTHY_VALUES.contains(value.toLowerCase());
                    var device = pi4j.registry().<DigitalOutput>get(id);
                    if (on) {
                        device.high();
                    } else {
                        device.low();
                    }
                    return ServerResponse.ok().body(on ? 1 : 0);
                })
                .build()
                .filter(exceptionHandler);
    }

    @Bean
    RouterFunction<ServerResponse> digitalOutputConfigRoutes(DeviceConfigRouterFactory factory) {
        return factory.routerFunction(
                "/api/config/digitaloutput",
                IOCreator::create,
                DigitalOutput.class,
                DigitalOutputConfig.class
        );
    }

}

package io.github.iamnicknack.pi4j.server.router;

import com.pi4j.context.Context;
import com.pi4j.internal.IOCreator;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class PwmRouterConfig {

    private static final String TRUTHY_VALUES = "1,on,true,yes";

    private final Context pi4j;

    public PwmRouterConfig(Context pi4j) {
        this.pi4j = pi4j;
    }

    @Bean
    RouterFunction<ServerResponse> pwmRoutes(ExceptionHandlerFilterFunction exceptionHandler) {
        return RouterFunctions.route()
                .PUT("/api/pwm/{id}/{enabled}", request -> {
                    var id = request.pathVariable("id");
                    var enabled = request.pathVariable("enabled");
                    var pwm = pi4j.registry().<Pwm>get(id);
                    var on = TRUTHY_VALUES.contains(enabled.toLowerCase());
                    if (on) {
                        pwm.on();
                    } else {
                        pwm.off();
                    }
                    return ServerResponse.ok().body(on ? 1 : 0);
                })
                .GET("/api/pwm/{id}/dutycycle", request -> {
                    var id = request.pathVariable("id");
                    var pwm = pi4j.registry().<Pwm>get(id);
                    var dutyCycle = pwm.dutyCycle();
                    return ServerResponse.ok().body(dutyCycle);
                })
                .PUT("/api/pwm/{id}/dutycycle/{dutycycle}", request -> {
                    var id = request.pathVariable("id");
                    var dutyCycle = request.pathVariable("dutycycle");
                    var pwm = pi4j.registry().<Pwm>get(id);
                    pwm.dutyCycle(Integer.parseInt(dutyCycle));
                    return ServerResponse.ok().body(dutyCycle);
                })
                .GET("/api/pwm/{id}/frequency", request -> {
                    var id = request.pathVariable("id");
                    var pwm = pi4j.registry().<Pwm>get(id);
                    var frequency = pwm.frequency();
                    return ServerResponse.ok().body(frequency);
                })
                .PUT("/api/pwm/{id}/frequency/{frequency}", request -> {
                    var id = request.pathVariable("id");
                    var frequency = request.pathVariable("frequency");
                    var pwm = pi4j.registry().<Pwm>get(id);
                    pwm.frequency(Integer.parseInt(frequency));
                    return ServerResponse.ok().body(frequency);
                })
                .build()
                .filter(exceptionHandler);
    }

    @Bean
    RouterFunction<ServerResponse> pwmConfigRoutes(DeviceConfigRouterFactory factory) {
        return factory.routerFunction(
                "/api/config/pwm",
                IOCreator::create,
                Pwm.class,
                PwmConfig.class
        );
    }
}

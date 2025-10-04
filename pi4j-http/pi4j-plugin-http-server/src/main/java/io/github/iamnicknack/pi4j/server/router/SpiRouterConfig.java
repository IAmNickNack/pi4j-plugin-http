package io.github.iamnicknack.pi4j.server.router;

import com.pi4j.context.Context;
import com.pi4j.internal.IOCreator;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class SpiRouterConfig {

    private final Context pi4j;

    public SpiRouterConfig(Context pi4j) {
        this.pi4j = pi4j;
    }

    @Bean
    RouterFunction<ServerResponse> spiRoutes(ExceptionHandlerFilterFunction exceptionHandler) {
        return RouterFunctions.route()
                .POST("/api/spi/{id}", request -> {
                    var id = request.pathVariable("id");
                    var transfer = request.body(SpiTransfer.class);
                    byte[] receivedData = new byte[transfer.bytes.length];
                    var spi = pi4j.registry().<Spi>get(id);
                    spi.transfer(transfer.bytes, receivedData);
                    return ServerResponse.ok().body(new SpiTransfer(receivedData));
                })
                .build()
                .filter(exceptionHandler);
    }

    @Bean
    RouterFunction<ServerResponse> spiConfigRoutes(DeviceConfigRouterFactory factory) {
        return factory.routerFunction(
                "/api/config/spi",
                IOCreator::create,
                Spi.class,
                SpiConfig.class
        );
    }

    public record SpiTransfer(byte[] bytes) {}
}

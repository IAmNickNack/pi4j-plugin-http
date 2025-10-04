package io.github.iamnicknack.pi4j.server.router;

import com.pi4j.context.Context;
import com.pi4j.internal.IOCreator;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import io.github.iamnicknack.pi4j.common.I2COperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class I2CRouterConfig {

    private final Context pi4j;

    private final Map<String, I2COperations> i2cOperations = new ConcurrentHashMap<>();

    public I2CRouterConfig(Context pi4j) {
        this.pi4j = pi4j;
    }

    @Bean
    RouterFunction<ServerResponse> i2cRoutes(ExceptionHandlerFilterFunction exceptionHandler) {

        return RouterFunctions.route()
                // send data to the I2C device
                .POST("/api/i2c/{id}", request -> {
                    var id = request.pathVariable("id");
                    var payload = request.body(I2COperations.Payload.class);
                    var result = fetchI2COperations(id).write(payload);
                    return ServerResponse.ok().body(result);
                })
                // read `length` bytes from the I2C device
                .GET("/api/i2c/{id}/length/{length}", request -> {
                    var id = request.pathVariable("id");
                    var length = Integer.parseInt(request.pathVariable("length"));
                    var result = fetchI2COperations(id).read(length);
                    return ServerResponse.ok().body(result);
                })
                // send data to the I2C register
                .POST("/api/i2c/{id}/register/{register}", request -> {
                    var id = request.pathVariable("id");
                    var register = Integer.parseInt(request.pathVariable("register"));
                    var payload = request.body(I2COperations.Payload.class);
                    var result = fetchI2COperations(id).writeRegister(register, payload);
                    return ServerResponse.ok().body(result);
                })
                // read `length` bytes from the I2C register
                .GET("/api/i2c/{id}/register/{register}/length/{length}", request -> {
                    var id = request.pathVariable("id");
                    var register = Integer.parseInt(request.pathVariable("register"));
                    var length = Integer.parseInt(request.pathVariable("length"));
                    var result = fetchI2COperations(id).readRegister(register, length);
                    return ServerResponse.ok().body(result);
                })
                .filter(exceptionHandler)
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> i2cConfigRoutes(DeviceConfigRouterFactory factory) {
        return factory.routerFunction(
                "/api/config/i2c",
                IOCreator::create,
                I2C.class,
                I2CConfig.class
        );
    }

    /**
     * Fetch or create the I2C operations for the given device id
     * @param deviceId the device id
     * @return the I2C operations for the given device id
     */
    private I2COperations fetchI2COperations(String deviceId) {
        return i2cOperations.computeIfAbsent(deviceId, id -> new I2CDeviceOperations(pi4j.registry().get(id)));
    }

    /**
     * I2C operations which delegate to a given {@link I2C} instance.
     */
    static class I2CDeviceOperations implements I2COperations {
        private final I2C i2c;

        public I2CDeviceOperations(I2C i2c) {
            this.i2c = i2c;
        }

        @Override
        public Payload.Result read(int length) {
            var data = new byte[length];
            var result = i2c.read(data);
            return new Payload(data).withResultCode(result);
        }

        @Override
        public int write(Payload payload) {
            return i2c.write(payload.data());
        }

        @Override
        public Payload.Result readRegister(int register, int length) {
            var data = new byte[length];
            var result = i2c.readRegister(register, data);
            return new Payload(data).withResultCode(result);
        }

        @Override
        public int writeRegister(int register, Payload payload) {
            return i2c.writeRegister(register, payload.data());
        }
    }
}

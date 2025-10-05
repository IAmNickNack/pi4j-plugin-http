package io.github.iamnicknack.pi4j.grpc.client;

import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.gpio.digital.*;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigPayload;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigServiceGrpc;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceRequest;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceType;
import io.grpc.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcDigitalInputProvider extends DigitalInputProviderBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Channel channel;
    private final DeviceConfigServiceGrpc.DeviceConfigServiceBlockingStub configStub;

    public GrpcDigitalInputProvider(Channel channel) {
        this.channel = channel;
        this.configStub = DeviceConfigServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public DigitalInputProvider initialize(Context context) throws InitializeException {
        return super.initialize(context);
    }

    @Override
    public DigitalInputProvider shutdown(Context context) throws ShutdownException {
        return super.shutdown(context);
    }

    @Override
    public DigitalInput create(DigitalInputConfig config) {
        logger.info("Creating new Digital Input device: {}", config.id());
        var payload = DeviceConfigPayload.newBuilder()
                .setDeviceType(DeviceType.DIGITAL_INPUT)
                .putAllConfig(config.properties())
                .build();

        var response = configStub.createDevice(payload);
        var responseConfig = DigitalInput.newConfigBuilder(this.context)
                .load(response.getConfigMap())
                .build();

        var device = new GrpcDigitalInput(channel, this, responseConfig);
        context.registry().add(device);
        return device;
    }

    /**
     * Remove a device from the pi4j context.
     * @param device the device to remove.
     */
    public void removeDevice(DigitalInput device) {
        logger.info("Removing Digital Input device: {}", device.id());
        var request = DeviceRequest.newBuilder()
                .setDeviceId(device.config().id())
                .setDeviceType(DeviceType.DIGITAL_INPUT)
                .build();

        var result = configStub.removeDevice(request);
        assert result != null;
    }
}

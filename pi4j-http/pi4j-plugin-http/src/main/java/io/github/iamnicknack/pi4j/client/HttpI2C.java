package io.github.iamnicknack.pi4j.client;

import com.pi4j.context.Context;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CBase;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CConfig;
import io.github.iamnicknack.pi4j.client.requests.HttpRequests;
import io.github.iamnicknack.pi4j.common.I2COperations;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class HttpI2C extends I2CBase<HttpI2C.NoopI2CBus> {

    private final I2COperations i2COperations;
    private final HttpRequests httpRequests;

    public HttpI2C(
            HttpI2CProvider provider,
            I2CConfig config,
            I2COperations i2COperations,
            HttpRequests httpRequests
    ) {
        super(provider, config, new NoopI2CBus());
        this.i2COperations = i2COperations;
        this.httpRequests = httpRequests;
//        this.i2COperations = new HttpI2COperations();
    }

    @Override
    public I2C shutdownInternal(Context context) throws ShutdownException {
        super.shutdownInternal(context);
        try {
            httpRequests.deleteJson(String.format("/api/config/i2c/%s", this.config.id()), Void.class);
        } catch (HttpRequests.HttpException e) {
            throw new ShutdownException(e);
        }
        return this;
    }

    @Override
    public int read() {
        var result = this.i2COperations.read(1);
        return (result.resultCode() >= 0) ? result.payload().data()[0] : result.resultCode();
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        var result = this.i2COperations.read(length);
        if (result.resultCode() >= 0) {
            System.arraycopy(result.payload().data(), 0, buffer, offset, length);
            return result.resultCode();
        }
        return result.resultCode();
    }

    @Override
    public int write(byte b) {
        var sendBuffer = new byte[] { b };
        this.write(sendBuffer, 0, 1);
        return 1;
    }

    @Override
    public int write(byte[] data, int offset, int length) {
        byte[] dataToSend = ((length - offset) == data.length)
                ? data
                : Arrays.copyOfRange(data, offset, offset + length);

        return this.i2COperations.write(dataToSend);
    }

    @Override
    public int readRegister(int register) {
        byte[] reg = new byte[] { (byte)register };
        byte[] buffer = new byte[1];
        this.readRegister(reg, buffer,0, buffer.length);
        return buffer[0] & 0xff;
    }

    @Override
    public int readRegister(int register, byte[] buffer, int offset, int length) {
        var reg = new byte[] { (byte)register };
        return this.readRegister(reg, buffer, offset, length);
    }

    @Override
    public int readRegister(byte[] register, byte[] buffer, int offset, int length) {
        var result = this.i2COperations.readRegister(register[0], length);

        if (result.resultCode() >= 0) {
            System.arraycopy(result.payload().data(), 0, buffer, offset, length);
            return result.payload().data().length;
        }

        return result.resultCode();
    }

    @Override
    public int writeRegister(int register, byte b) {
        return this.writeRegister(new byte[] { (byte)register }, new byte[] { b }, 0, 1);
    }

    @Override
    public int writeRegister(int register, byte[] data, int offset, int length) {
        return this.writeRegister(new byte[] { (byte)register }, data, offset, length);
    }

    @Override
    public int writeRegister(byte[] register, byte[] data, int offset, int length) {
        byte[] dataToSend = ((length - offset) == data.length)
                ? data
                : Arrays.copyOfRange(data, offset, offset + length);

        return this.i2COperations.writeRegister(register[0], dataToSend);
    }

    /**
     * I2C bus implementation that does nothing, but allows us to extend {@link I2CBase}
     */
    static class NoopI2CBus implements I2CBus {
        @Override
        public <R> R execute(I2C i2c, Callable<R> action) {
            throw new UnsupportedOperationException("HTTP I2C does not support bus operations");
        }
    }

    /**
     * I2C operations implementation using {@link HttpRequests}
     */
    public static class HttpI2COperations implements I2COperations {

        private final HttpRequests httpRequests;

        private final String deviceId;

        public HttpI2COperations(HttpRequests httpRequests, String deviceId) {
            this.httpRequests = httpRequests;
            this.deviceId = deviceId;
        }

        @Override
        public Payload.Result read(int length) {
            return httpRequests.getJson("/" + deviceId + "/length/" + length , Payload.Result.class);
        }

        @Override
        public int write(Payload payload) {
            return httpRequests.postJson("/" + deviceId, payload, Integer.class);
        }

        @Override
        public Payload.Result readRegister(int register, int length) {
            var url = String.format("/%s/register/%d/length/%d", deviceId, register, length);
            return httpRequests.getJson(url, Payload.Result.class);
        }

        @Override
        public int writeRegister(int register, Payload payload) {
            var url = String.format("/%s/register/%d", deviceId, register );
            return httpRequests.postJson(url, payload, Integer.class);
        }
    }
}

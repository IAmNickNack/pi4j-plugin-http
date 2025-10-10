package io.github.iamnicknack.pi4j.client;

import com.pi4j.context.Context;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiBase;
import com.pi4j.io.spi.SpiConfig;
import io.github.iamnicknack.pi4j.client.requests.HttpRequests;

import java.util.Arrays;

public class HttpSpi extends SpiBase {

    final String baseUrl;
    final HttpRequests httpRequests;

    public HttpSpi(HttpSpiProvider provider, SpiConfig config) {
        super(provider, config);
        this.baseUrl = provider.baseUrl;
        this.httpRequests = provider.httpRequests;
    }

    @Override
    public Spi shutdown(Context context) throws ShutdownException {
        try {
            httpRequests.deleteJson(baseUrl + "/api/config/spi/" + this.getId(), Void.class);
        } catch (HttpRequests.HttpException e) {
            throw new ShutdownException(e);
        }
        return super.shutdown(context);
    }

    @Override
    public int transfer(byte[] write, int writeOffset, byte[] read, int readOffset, int numberOfBytes) {
        var bytesToSend = (writeOffset == 0)
                ? write
                : Arrays.copyOfRange(write, writeOffset, writeOffset + numberOfBytes);

        // wrap data to send
        var transfer = new SpiTransfer(bytesToSend);
        // send data and receive response
        var result = httpRequests.postJson(baseUrl + "/api/spi/" + this.getId(), transfer, SpiTransfer.class);
        // copy response to read buffer
        System.arraycopy(result.bytes, 0, read, readOffset, numberOfBytes);

        return numberOfBytes;
    }

    @Override
    public int read() {
        var buffer = new byte[1];
        this.transfer(new byte[1], 0, buffer, 0, 1);
        return buffer[0];
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        return this.transfer(new byte[buffer.length], 0, buffer, offset, length);
    }

    @Override
    public int write(byte b) {
        var sendBuffer = new byte[] { b };
        var readBuffer = new byte[1];
        this.transfer(sendBuffer, 0, readBuffer, 0, 1);
        return readBuffer[0];
    }

    @Override
    public int write(byte[] data, int offset, int length) {
        return this.transfer(data, offset, new byte[length], 0, length);
    }

    /**
     * JSON-serializable record for SPI payload
     */
    public record SpiTransfer(byte[] bytes) {}
}

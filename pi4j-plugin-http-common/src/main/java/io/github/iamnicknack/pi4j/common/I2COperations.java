package io.github.iamnicknack.pi4j.common;

/**
 * High-level operations on I2C devices. Supports significant operations
 */
public interface I2COperations {
    /**
     * Read data from the I2C device
     * @param length the number of bytes to read
     * @return the result of the read operation
     */
    Payload.Result read(int length);

    /**
     * Write data to the remote I2C device
     * @param payload the data to write
     * @return the result of the write operation
     */
    int write(Payload payload);

    /**
     * Write data to the remote I2C device
     * @param data the data to write
     * @return the result of the write operation
     */
    default int write(byte[] data) {
        return write(new Payload(data));
    }

    /**
     * Read data from the remote I2C register
     * @param register the register to read from
     * @param length the number of bytes to read
     * @return the result of the read operation
     */
    Payload.Result readRegister(int register, int length);

    /**
     * Write data to the remote I2C register
     * @param register the register to write to
     * @param payload the data to write
     * @return the result of the write operation
     */
    int writeRegister(int register, Payload payload);

    /**
     * Write data to the remote I2C register
     * @param register the register to write to
     * @param data the data to write
     * @return the result of the write operation
     */
    default int writeRegister(int register, byte[] data) {
        return writeRegister(register, new Payload(data));
    }

    /**
     * I2C data payload. Contains the data to send or the data received.
     * This record is used by both the client and server to encapsulate a byte array and allow
     * consistent JSON serialization and deserialization possible for all APIs.
     *
     * @param data the data to send or the data received
     */
    record Payload(byte[] data) {

        /**
         * Create a new result with the given result code.
         * @param resultCode native result code.
         * @return a container for the result code and payload.
         */
        public Result withResultCode(int resultCode) {
            return new Result(resultCode, this);
        }

        /**
         * Container for the result of an I2C operation.
         * @param resultCode the result code.
         * @param payload the payload.
         */
        public record Result(int resultCode, Payload payload) { }
    }
}

package examples.i2c

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.io.i2c.I2C
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProviderImpl
import com.pi4j.plugin.mock.provider.i2c.MockI2CProviderImpl
import io.github.iamnicknack.pi4j.client.HttpDigitalOutputProvider
import io.github.iamnicknack.pi4j.client.HttpI2CProvider
import io.github.iamnicknack.pi4j.grpc.client.GrpcDigitalOutputProvider
import io.github.iamnicknack.pi4j.grpc.client.GrpcI2CProvider
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials


class BasicI2C : AutoCloseable {

    val host: String by lazy {
        System.getProperty("pi4j.host", "localhost:8080")
    }

    val pi4j: Context by lazy {
        when (System.getProperty("pi4j.plugin", "mock")) {
            "http" -> {
                Pi4J.newContextBuilder()
                    .add(HttpDigitalOutputProvider("http://$host"))
                    .add(HttpI2CProvider("http://$host"))
                    .build()
            }
            "grpc" -> {
                val channel = Grpc.newChannelBuilder(host, InsecureChannelCredentials.create()).build()
                Pi4J.newContextBuilder()
                    .add(GrpcDigitalOutputProvider(channel))
                    .add(GrpcI2CProvider(channel))
                    .build()
            }
            else -> Pi4J.newContextBuilder()
                .add(MockDigitalOutputProviderImpl())
                .add(MockI2CProviderImpl())
                .build()
        }
    }

    val i2c: I2C = pi4j.create(
        I2C.newConfigBuilder(pi4j)
            .id("mcp23008")
            .bus(1)
            .device(0x20)
            .build()
    )

    val reset: DigitalOutput = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .id("mcp-reset")
            .address(17)
            .initial(DigitalState.HIGH)
            .shutdown(DigitalState.LOW)
            .build()
    )


    override fun close() {
        pi4j.shutdown()
    }
}


fun main() {
    BasicI2C().use { basicI2C ->
        basicI2C.reset.low()
        basicI2C.reset.high()

        basicI2C.i2c.writeRegister(0, 0)     // mcp data direction
        basicI2C.i2c.writeRegister(9, 0x55)  // mcp output latch

        repeat(8) {
            val value = basicI2C.i2c.readRegister(9) xor 0xff
            basicI2C.i2c.writeRegister(9, value)
            Thread.sleep(500)
        }
    }
}
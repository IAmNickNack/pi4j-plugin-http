package examples.i2c

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.io.i2c.I2C
import io.github.iamnicknack.pi4j.client.HttpDigitalOutputProvider
import io.github.iamnicknack.pi4j.client.HttpI2CProvider


fun main() {
    val baseUrl = System.getenv("PI4J_BASE_URL") ?: "http://localhost:8080"

    val pi4j = Pi4J.newContextBuilder()
        .add(HttpI2CProvider(baseUrl))
        .add(HttpDigitalOutputProvider(baseUrl))
        .build()

    val i2c = pi4j.create(
        I2C.newConfigBuilder(pi4j)
            .id("mcp23008")
            .bus(1)
            .device(0x20)
            .build()
    )

    val reset = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .id("mcp-reset")
            .address(17)
            .initial(DigitalState.HIGH)
            .shutdown(DigitalState.LOW)
            .build()
    )

    reset.low()
    reset.high()

    i2c.writeRegister(0, 0)     // mcp data direction
    i2c.writeRegister(9, 0x55)  // mcp output latch

    repeat(8) {
        val value = i2c.readRegister(9) xor 0xff
        i2c.writeRegister(9, value)
        Thread.sleep(500)
    }

    pi4j.shutdown()
}
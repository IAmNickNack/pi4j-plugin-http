package examples.seven

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.io.pwm.Pwm
import com.pi4j.io.pwm.PwmType
import com.pi4j.io.spi.Spi
import com.pi4j.io.spi.SpiBus
import com.pi4j.io.spi.SpiMode
import io.github.iamnicknack.pi4j.client.HttpDigitalInputProvider
import io.github.iamnicknack.pi4j.client.HttpDigitalOutputProvider
import io.github.iamnicknack.pi4j.client.HttpPwmProvider
import io.github.iamnicknack.pi4j.client.HttpSpiProvider


fun main() {
    val baseUrl = System.getenv("PI4J_BASE_URL") ?: "http://localhost:8080"

    val pi4j = Pi4J.newContextBuilder()
        .add(HttpDigitalOutputProvider(baseUrl))
        .add(HttpDigitalInputProvider(baseUrl))
        .add(HttpPwmProvider(baseUrl))
        .add(HttpSpiProvider(baseUrl))
        .build()

    val resetPin = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .id("reset")
            .name("Reset Pin")
            .address(5)
            .shutdown(DigitalState.LOW)
            .initial(DigitalState.HIGH)
            .build()
    )

    val pwm = pi4j.create(
        Pwm.newConfigBuilder(pi4j)
            .id("refresh")
            .name("Refresh PWM")
            .address(2)
            .pwmType(PwmType.HARDWARE)
            .frequency(220)
            .dutyCycle(50)
            .shutdown(0)
            .build()
    )

    val spi = pi4j.create(
        Spi.newConfigBuilder(pi4j)
            .id("spi")
            .name("SPI")
            .baud(1000000)
            .bus(SpiBus.getByNumber(1))
            .mode(SpiMode.MODE_0)
            .address(0)
            .build()
    )

    resetPin.high()
    resetPin.low()
    resetPin.high()

    pwm.on()

    for (i in 0..10000) {
        spi.write(SevenSegment.toBytes(i))
    }

    pi4j.shutdown()
}

object SevenSegment {
    const val SEG_A: Int = 1
    val SEG_B: Int = 1 shl 2
    val SEG_C: Int = 1 shl 6
    val SEG_D: Int = 1 shl 4
    val SEG_E: Int = 1 shl 3
    val SEG_F: Int = 1 shl 1
    val SEG_G: Int = 1 shl 7
    val SEG_H: Int = 1 shl 5

    val SEVEN_SEGMENT_DIGITS: IntArray = intArrayOf(
        SEG_A or SEG_B or SEG_C or SEG_D or SEG_E or SEG_F,  // 0
        SEG_B or SEG_C,  // 1
        SEG_A or SEG_B or SEG_D or SEG_E or SEG_G,  // 2
        SEG_A or SEG_B or SEG_C or SEG_D or SEG_G,  // 3
        SEG_B or SEG_C or SEG_F or SEG_G,  // 4
        SEG_A or SEG_C or SEG_D or SEG_F or SEG_G,  // 5
        SEG_A or SEG_C or SEG_D or SEG_E or SEG_F or SEG_G,  // 6
        SEG_A or SEG_B or SEG_C,  // 7
        SEG_A or SEG_B or SEG_C or SEG_D or SEG_E or SEG_F or SEG_G,  // 8
        SEG_A or SEG_B or SEG_C or SEG_D or SEG_F or SEG_G,  // 9
        SEG_A or SEG_B or SEG_C or SEG_E or SEG_F or SEG_G,  // A
        SEG_C or SEG_D or SEG_E or SEG_F or SEG_G,  // b
        SEG_A or SEG_D or SEG_E or SEG_F,  // C
        SEG_B or SEG_C or SEG_D or SEG_E or SEG_G,  // d
        SEG_A or SEG_D or SEG_E or SEG_F or SEG_G,  // E
        SEG_A or SEG_E or SEG_F or SEG_G // F
    )

    fun toBytes(value: Int): ByteArray {
        return byteArrayOf(
            SEVEN_SEGMENT_DIGITS[(value % 10)].toByte(),
            SEVEN_SEGMENT_DIGITS[(value / 10) % 10].toByte(),
            SEVEN_SEGMENT_DIGITS[(value / 100) % 10].toByte(),
            SEVEN_SEGMENT_DIGITS[(value / 1000) % 10].toByte()
        )
    }
}
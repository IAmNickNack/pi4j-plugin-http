package examples.events

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import io.github.iamnicknack.pi4j.client.HttpDigitalOutputProvider
import io.github.iamnicknack.pi4j.client.requests.OkHttpRequests
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

fun main() {

    val logger = LoggerFactory.getLogger("main")
    val baseUrl = System.getenv("PI4J_BASE_URL") ?: "http://localhost:8080"

    val context: Context = Pi4J.newContextBuilder()
        .add(HttpDigitalOutputProvider(baseUrl, OkHttpRequests()))
        .build()

    val output = context.create(
        DigitalOutput.newConfigBuilder(context)
            .id("test-output")
            .address(5)
            .shutdown(DigitalState.LOW)
            .build()
    )

    val count = AtomicInteger(0)

    output.addListener({
        logger.info("############# Digital output state changed: ${it.state()} ############# ")
        count.incrementAndGet()
    })

    output.high()
    Thread.sleep(1000)

    output.low()
    Thread.sleep(1000)

    output.high()
    Thread.sleep(1000)

    context.shutdown()

    logger.info("############# Finished ############# ")
    logger.info("############# Count: ${count.get()} ############# ")
}

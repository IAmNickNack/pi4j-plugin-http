package io.github.iamnicknack.pi4j.grpc.server

import com.pi4j.Pi4J
import com.pi4j.context.Context
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionServiceV1
import org.slf4j.LoggerFactory

class Pi4jGrpc {

    private val logger = LoggerFactory.getLogger(Pi4jGrpc::class.java)

    private val pi4j: Context = Pi4J.newAutoContext()

    private val server: Server = ServerBuilder.forPort(System.getProperty("pi4j.server.port", "9090").toInt())
        .addService(DigitalInputService(pi4j))
        .addService(DigitalOutputService(pi4j))
        .addService(PwmService(pi4j))
        .addService(SpiService(pi4j))
        .addService(I2CService(pi4j))
        .addService(DeviceConfigService(pi4j))
        .addService(ProtoReflectionServiceV1.newInstance())
        .build()

    fun start() {
        server.start()
        logger.info("Server started, listening on ${server.port}")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.info("*** shutting down gRPC server since JVM is shutting down")
                this@Pi4jGrpc.stop()
                logger.info("*** server shut down")
            }
        )
    }

    fun stop() {
        pi4j.shutdown()
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}

fun main() {
    val server = Pi4jGrpc()
    server.start()
    server.blockUntilShutdown()
}

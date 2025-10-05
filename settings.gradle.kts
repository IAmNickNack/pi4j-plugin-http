rootProject.name = "pi4j-plugin-http"


// HTTP
include(":pi4j-plugin-http")
include(":pi4j-plugin-http-common")
include(":pi4j-plugin-http-server")

project(":pi4j-plugin-http").projectDir = file("pi4j-http/pi4j-plugin-http")
project(":pi4j-plugin-http-common").projectDir = file("pi4j-http/pi4j-plugin-http-common")
project(":pi4j-plugin-http-server").projectDir = file("pi4j-http/pi4j-plugin-http-server")

// GRPC
include(":pi4j-plugin-grpc")
include(":pi4j-plugin-grpc-proto")
include(":pi4j-plugin-grpc-server")

project(":pi4j-plugin-grpc").projectDir = file("pi4j-grpc/pi4j-plugin-grpc")
project(":pi4j-plugin-grpc-proto").projectDir = file("pi4j-grpc/pi4j-plugin-grpc-proto")
project(":pi4j-plugin-grpc-server").projectDir = file("pi4j-grpc/pi4j-plugin-grpc-server")

// Examples
include(":gpio-events")
include(":basic-i2c")
include(":seven-segment")

project(":gpio-events").projectDir = file("examples/gpio-events")
project(":basic-i2c").projectDir = file("examples/basic-i2c")
project(":seven-segment").projectDir = file("examples/seven-segment")

includeBuild("build-logic")

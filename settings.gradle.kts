rootProject.name = "pi4j-server"
include("pi4j-plugin-http")
include("pi4j-plugin-http-common")
include("pi4j-plugin-http-server")

include(":gpio-events")
include(":basic-i2c")
include(":seven-segment")
project(":gpio-events").projectDir = file("examples/gpio-events")
project(":basic-i2c").projectDir = file("examples/basic-i2c")
project(":seven-segment").projectDir = file("examples/seven-segment")

includeBuild("build-logic")

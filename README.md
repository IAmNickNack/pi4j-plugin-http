# PI4J HTTP Plugin

This plugin intends to provide remote development support for PI4J in the absence of 
[pigpiod](https://abyz.me.uk/rpi/pigpio/pigpiod.html) support for the Raspberry Pi v5. It assumes that convenience 
is more important than performance during the development phase of a project and intends make existing Pi4j APIs 
accessible via HTTP.

Current supported providers extend to:

* GPIO (+event listeners)
* PWM
* I2C
* SPI

The implementation focuses on providing a functional client plugin together with a draft OpenAPI specification for 
the REST API, intended to facilitate the implementation of alternative server and/or client implementations.

This project includes a reference implementation of the server application, which uses the existing Pi4j 
library to provide hardware integration.

## Install

There are currently no binary distributions available, but both the client and server-side components can be built 
from source for Java 21:

```bash
# Clone the repository and build the plugin
git clone https://github.com/iamnicknack/pi4j-plugin-http.git
# Build the plugin and publish it to the local Maven repository
./gradlew publishToMavenLocal   
```

### Maven

```xml
<dependency>
    <groupId>io.github.iamnicknack.pi4j</groupId>
    <artifactId>pi4j-plugin-http</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Gradle

```
implementation("io.github.iamnicknack.pi4j:pi4j-plugin-http:0.0.1")
```

## Server Implementation

The server is simply a REST API to proxy access to the Pi4j library. It is intended to be used in conjunction with 
the client plugin to provide remote development support for projects use Pi4j but do not have access to a Pi4j 
compatible device.

Once built, as described above, the server can be started by invoking the Spring Boot application:

```bash
java --enable-native-access=ALL-UNNAMED -jar ./pi4j-plugin-http-server/build/libs/pi4j-plugin-http-server.jar
```

If required, OpenAPI specifications then are available locally at at http://localhost:8080/swagger-ui/index.html.

Although OpenAPI specs are most often used to generate client code, the same specs can be used to build a compatible
server implementation.

## Examples

Examples contained in the `examples` directory should be reasonably self-explanatory and runnable as-is. 
At least within an IDE.
import io.github.iamnicknack.pi4j.client.HttpPlugin;

module pi4j.plugin.http {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.pi4j;
    requires okhttp3;
    requires okhttp3.sse;
    requires org.slf4j;

    requires pi4j.http.common;

    exports io.github.iamnicknack.pi4j.client;
    exports io.github.iamnicknack.pi4j.client.event;
    exports io.github.iamnicknack.pi4j.client.requests;

    provides com.pi4j.extension.Plugin
            with HttpPlugin;
}
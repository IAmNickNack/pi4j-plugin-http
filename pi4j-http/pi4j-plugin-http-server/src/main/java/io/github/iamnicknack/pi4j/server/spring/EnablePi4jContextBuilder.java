package io.github.iamnicknack.pi4j.server.spring;

import com.pi4j.Pi4J;
import com.pi4j.context.ContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.ConfigurableEnvironment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provide a Pi4j {@link ContextBuilder} with properties from the Spring context.
 * Converts application properties prefixed with `pi4j` to properties in the Pi4j context, with the prefix stripped.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(EnablePi4jContextBuilder.Pi4jContextBuilderConfig.class)
public @interface EnablePi4jContextBuilder {

    class Pi4jContextBuilderConfig {

        private final Map<String, String> pi4jProperties;

        public Pi4jContextBuilderConfig(ConfigurableEnvironment environment) {
            this.pi4jProperties = environment.getPropertySources().stream()
                    .filter(propertySource ->
                            propertySource.getName().startsWith("pi4j.") &&
                                    propertySource.getProperty(propertySource.getName()) != null
                    )
                    .map(propertySource ->
                            Map.entry(
                                    propertySource.getName().substring(5),
                                    Objects.requireNonNull(propertySource.getProperty(propertySource.getName())).toString()
                            )
                    )
                    .collect(Collectors.toMap(Map.Entry::getKey,  Map.Entry::getValue));
        }

        @Bean
        @Scope("prototype")
        ContextBuilder pi4jContextBuilder() {
            return Pi4J.newContextBuilder()
                    .properties(pi4jProperties);
        }
    }
}

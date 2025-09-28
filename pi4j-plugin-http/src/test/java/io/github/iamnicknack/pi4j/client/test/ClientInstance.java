package io.github.iamnicknack.pi4j.client.test;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Qualifier annotation to explicitly mark a bean as a client instance to avoid conflict with beans created by
 * the Spring Boot server instance.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientInstance {
}

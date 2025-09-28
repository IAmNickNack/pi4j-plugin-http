package io.github.iamnicknack.pi4j.common;

import java.lang.reflect.InvocationTargetException;

/**
 * Convey an error to the client
 * @param message the error message
 * @param type the type of the error
 * @param constructorArgs if reconstructable, provide constructor args
 * @param cause the cause of the error, if any
 */
public record ErrorResponsePayload(
        String message,
        Class<? extends Throwable> type,
        Object[] constructorArgs,
        ErrorResponsePayload cause
){
    public ErrorResponsePayload(String message) {
        this(message, null, null, null);
    }

    public ErrorResponsePayload(String message, Class<? extends Throwable> type, Object... constructorArgs) {
        this(message, type, constructorArgs, null);
    }

    public ErrorResponsePayload(Throwable t){
        this(
                t.getMessage(),
                t.getClass(),
                null,
                t.getCause() != null
                        ? new ErrorResponsePayload(t.getCause().getMessage(), t.getCause().getClass(), null, null)
                        : null
        );
    }

    /**
     * Reconstruct the exception on the client side, if possible
     * @return a new RuntimeException wrapping the reconstructed exception, or a generic RuntimeException if not possible
     */
    public RuntimeException reconstructException() {
        if (type == null) {
            return new RuntimeException(message);
        }
        try {
            if (constructorArgs != null) {
                var argTypes = java.util.Arrays.stream(constructorArgs)
                        .map(Object::getClass)
                        .toArray(Class[]::new);
                var constructor = type.getConstructor(argTypes);
                if (constructor != null) {
                    return new RuntimeException(constructor.newInstance(constructorArgs));
                }
            }
            return new RuntimeException(message);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return new RuntimeException(message, e);
        }
    }
}

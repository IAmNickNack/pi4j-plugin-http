package io.github.iamnicknack.pi4j.server.spring;

import com.pi4j.boardinfo.util.BoardInfoHelper;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * Conditional annotation for components that should only be created when running on Raspberry Pi.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(ConditionalOnRaspberryPi.RaspberryPiCondition.class)
public @interface ConditionalOnRaspberryPi {

    @Order(HIGHEST_PRECEDENCE + 100) // allow scope to be overridden
    class RaspberryPiCondition extends SpringBootCondition {
        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            var runningOnRaspberryPi = BoardInfoHelper.runningOnRaspberryPi();
            return (runningOnRaspberryPi)
                    ? ConditionOutcome.match("Running in Raspberry Pi")
                    : ConditionOutcome.noMatch("Not running in Raspberry Pi");
        }
    }

}

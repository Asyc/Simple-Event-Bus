package asyc.java.seb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Asyc
 * @since 11/13/18
 **/

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubscribeEvent {

    /**
     * This value is used to sort the list of methods in {@link EventBus#registry}.
     * This value will only be used if {@link EventBus#sorted} is true.
     * @return returns the priority of the method that is being annotated.
     */
    int priority() default 1;
}

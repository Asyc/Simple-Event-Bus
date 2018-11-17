package asyc.java.seb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Asyc
 * @since 11/13/18
 */
final class MethodData {

    private Object parent;
    private Method target;
    private int priority;

    /**
     * This is a data class to store details about methods.
     * @param parent The parent class of the specified method
     * @param target The method to be invoked
     */
    MethodData(Object parent, Method target) {
        this.parent = parent;
        this.target = target;
        this.priority = target.getAnnotation(SubscribeEvent.class).priority();
    }

    Object getParent() {
        return parent;
    }

    Method getTarget() {
        return target;
    }

    int getPriority() { return priority; }

    /**
     * Invokes the {@link MethodData#target} with the specified parameter and the method's parent class {@link MethodData#parent}.
     * @param parameter the object to invoke the method with
     */
    void invoke(Object parameter) {
        try {
            target.invoke(parent, parameter);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

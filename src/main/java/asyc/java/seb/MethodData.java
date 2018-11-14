package asyc.java.seb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class MethodData {

    private Object parent;
    private Method target;
    private int priority;

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

    void invoke(Object parameter) {
        try {
            target.invoke(parent, parameter);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

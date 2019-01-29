package me.asycc.seb.invocation;


import me.asycc.seb.annotation.EventSubscriber;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class Lambda {

	private Object parent;
	private Method method;
	private java.lang.reflect.Method reflectMethod;
	private int priority;

	/**
	 *
	 * Creates a {@link CallSite} for the specified method
	 *
	 * @param parent The instance of the object to invoke the method with
	 * @param target The {@link java.lang.reflect.Method} to unreflect
	 * @throws Throwable {@link CallSite#getTarget()#invoke(Object)} throws {@link Throwable}
	 */
	public Lambda(Object parent, java.lang.reflect.Method target) throws Throwable{

		MethodHandles.Lookup lookup = MethodHandles.lookup();

		MethodHandle handle = lookup.unreflect(target);
		CallSite callSite = LambdaMetafactory.metafactory(lookup,
				"invoke",
				MethodType.methodType(Method.class),
				handle.type().changeParameterType(0, Object.class).changeParameterType(1, Object.class),
				handle,
				handle.type()
		);

		this.method = (Method)callSite.getTarget().invoke();
		this.parent = parent;
		this.reflectMethod = target;
		this.priority = target.getDeclaredAnnotation(EventSubscriber.class).priority();
	}

	/**
	 * Invokes the method
	 * @param arg The argument to invoke the method with
	 */
	public void invoke(Object arg){
		method.invoke(parent, arg);
	}

	/**
	 * @return The original java reflect Method
	 */
	public java.lang.reflect.Method getReflectMethod() {
		return reflectMethod;
	}

	/**
	 * @return The instance of the object used to invoke the method
	 */
	public Object getParent() {
		return parent;
	}

	/**
	 * @return {@link EventSubscriber#priority()} used if the event bus is sorted
	 */
	public int getPriority() {
		return priority;
	}
}

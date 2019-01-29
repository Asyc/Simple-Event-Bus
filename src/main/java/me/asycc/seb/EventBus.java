package me.asycc.seb;


import me.asycc.seb.annotation.EventSubscriber;
import me.asycc.seb.invocation.Lambda;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Asyc
 * @since 11/13/2018
 */
public class EventBus {

	private Map<Class<?>, List<Lambda>> registry = new WeakHashMap<>();
	private Map<Object, Map<Class<?>, List<Lambda>>> cache = new WeakHashMap<>();
	private boolean sorted;

	/**
	 * Sets {@link EventBus#sorted} to false
	 */
	public EventBus(){
		this(false);
	}

	/**
	 * @param sorted Whether to sort the methods in the {@link EventBus#registry} map by their {@link EventSubscriber#priority()} which changes the invoking order
	 */
	public EventBus(boolean sorted){
		this.sorted = sorted;
	}

	/**
	 *
	 * This method uses {@link Class#getMethods()} to populate {@link EventBus#cache} and {@link EventBus#registry}
	 *
	 * @param obj The instance of the object to use
	 */
	public void register(Object obj){

		if(cache.containsKey(obj)){
			registry.putAll(cache.get(obj));
		}else{

			WeakHashMap<Class<?>, List<Lambda>> cacheEntry = new WeakHashMap<>();

			for(Method method : obj.getClass().getMethods()){
				if(!isMethodValid(method)) continue;

				Lambda lambda;
				Class<?> event = method.getParameters()[0].getType();
				try{
					lambda = new Lambda(obj, method);
				}catch (Throwable t){
					t.printStackTrace();
					continue;
				}

				registry.computeIfAbsent(event, ignored -> new ArrayList<>()).add(lambda);

				cacheEntry.computeIfAbsent(event, ignored-> new ArrayList<>()).add(lambda);
			}

			cache.put(obj.getClass(), cacheEntry);
		}
	}

	/**
	 *
	 * Adds one method to the {@link EventBus#registry} and {@link EventBus#cache}
	 *
	 * @param obj The instance of a class containing the method
	 * @param method The method to add to {@link EventBus#registry}
	 */

	public void register(Object obj, Method method){
		if(!isMethodValid(method)) return;
		if(!cache.containsKey(obj)){

			Lambda lambda;

			try{
				lambda = new Lambda(obj, method);
			}catch (Throwable t){
				t.printStackTrace();
				return;
			}

			cache.put(obj, new HashMap<>());
			cache.get(obj).put(method.getParameters()[0].getType(), new ArrayList<>());
			cache.get(obj).get(method.getParameters()[0].getType()).add(lambda);
			registry.computeIfAbsent(method.getParameters()[0].getType(), ignored -> new ArrayList<>()).add(lambda);
		}else{
			for(Lambda lambda : cache.get(obj).get(method.getParameters()[0].getType())){
				if(lambda.getReflectMethod().equals(method) && lambda.getParent().equals(obj)){
					registry.computeIfAbsent(method.getParameters()[0].getType(), ignored -> new ArrayList<>()).add(lambda);
				}
			}
		}

	}

	/**
	 *
	 * Removes all {@link Lambda} in {@link EventBus#registry} with the parent object matching the parameter
	 *
	 * @param obj The instance of the object
	 */
	public void unregister(Object obj){

		for(List<Lambda> list : registry.values()){
			List<Lambda> list1 = new ArrayList<>(list);

			list1.forEach((Lambda lambda) -> {
				if(lambda.getParent().equals(obj)){
					list.remove(lambda);
				}
			});
		}
	}

	/**
	 * Removes a specific method from {@link EventBus#registry}
	 *
	 * @param obj The instance of the object
	 * @param method The method to remove
	 */
	public void unregister(Object obj, Method method){
		if(!isMethodValid(method)) return;

		Class<?> event = method.getParameters()[0].getType();

		if(!registry.containsKey(event)) return;

		for(Lambda lambda : new ArrayList<>(registry.get(event))){
			if(lambda.getReflectMethod().equals(method) && lambda.getParent().equals(obj)){
				registry.get(event).remove(lambda);
			}
		}
	}

	/**
	 * @param obj The "event" to call all methods with the event as a parameter
	 */
	public void post(Object obj){
		List<Lambda> list = registry.get(obj.getClass());

		if(list == null) return;

		for(int i = 0; i < list.size(); i++){
			list.get(i).invoke(obj);
		}
	}

	/**
	 *
	 * @param obj The "event" to call all methods with the event as a parameter
	 * @param invokeCount The number of times to invoke each method
	 */
	public void post(Object obj, int invokeCount){
		List<Lambda> list = registry.get(obj.getClass());

		if(list == null) return;

		for(int i = 0; i < list.size(); i++){
			Lambda target = list.get(i);
			for(int j = 0; j < invokeCount; j++){
				target.invoke(obj);
			}
		}
	}

	/**
	 * Clears the {@link EventBus#registry} map
	 */
	public void unregisterAll(){
		registry.clear();
	}

	/**
	 * Sorts all the method lists by {@link EventSubscriber#priority()}
	 */
	private void sortAll(){
		for(List<Lambda> lambda : registry.values()){
			lambda.sort(Comparator.comparing(Lambda::getPriority));
		}
	}

	/**
	 * @param method The method to check
	 * @return Returns whether the method is able to be added to {@link EventBus#registry}
	 */
	private boolean isMethodValid(Method method){
		return method.getParameterCount() == 1 && method.isAnnotationPresent(EventSubscriber.class);
	}

}

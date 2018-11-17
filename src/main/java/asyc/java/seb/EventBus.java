package asyc.java.seb;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author Asyc
 * @since 11/13/18
 */
public class EventBus {

    /**
     * A list of methods to be invoked once a certain object is called at {@link #call(Object)}
     */
    private Map<Object, List<MethodData>> registry = new WeakHashMap<>();

    /**
     * A cache to improve register/unregister speeds
     */
    private Map<Object, HashMap<Object, List<MethodData>>> cache = new WeakHashMap<>();

    private boolean sorted;

    /**
     * Sets sorted to false by default
     */
    public EventBus(){
        this(false);
    }

    /**
     * @param sorted If this is set to true, it will sort all events by {@link SubscribeEvent#priority()}
     */
    public EventBus(boolean sorted){
        this.sorted = sorted;
    }

    /**
     * Adds all the methods in a class annotated with {@link SubscribeEvent} to a list.
     * @param obj is the object that will be added to the registry HashMap.
     */
    public void registerClass(Object obj){
        if(!cache.containsKey(obj)) {
            for (final Method method : obj.getClass().getDeclaredMethods()) {
                if (!isMethodValid(method)) continue;
                registry.computeIfAbsent(method.getParameters()[0].getType(), ignored -> new CopyOnWriteArrayList<>()).add(new MethodData(obj, method));
                cache.put(obj, new HashMap<>());
                cache.get(obj).computeIfAbsent(method.getParameters()[0].getType(), ignored -> new CopyOnWriteArrayList<>()).add(new MethodData(obj, method));
            }
            if (sorted) {
                sortAll();
            }
        }else{
            for(Map.Entry<Object, List<MethodData>> entry : cache.get(obj).entrySet()){
                registry.computeIfAbsent(entry.getKey(), ignored -> new CopyOnWriteArrayList<>()).addAll(entry.getValue());
            }
        }
    }

    /**
     * Removes all methods in a class annotated with the {@link SubscribeEvent} from the registry HashMap
     * @param obj all the methods in the object's class will be removed from the registry HashMap
     */
    public void unregisterClass(Object obj){
        for(List<MethodData> list : registry.values()){
            for(final MethodData data : new ArrayList<>(list)){
                if(data.getParent() == obj) list.remove(data);
            }
        }
        if(sorted){
            sortAll();
        }
    }

    /**
     * Adds a single method to the {@link EventBus#registry} to be invoked at {@link EventBus#call(Object)}
     * @param parent is the parent class of the method
     * @param method is the method to be added to {@link EventBus#registry}
     */
    public void registerMethod(Object parent, Method method){
        if(!isMethodValid(method)) return;
        MethodData data = new MethodData(parent, method);
        registry.computeIfAbsent(method.getParameters()[0].getType(), ignored -> new CopyOnWriteArrayList<>()).add(data);
        cache.computeIfAbsent(parent, ignored -> new HashMap<>()).computeIfAbsent(method.getParameters()[0].getType(), ignored -> new CopyOnWriteArrayList<>()).add(data);
        if(sorted){
            sort(method.getParameters()[0].getType());
        }
    }

    /**
     * Removes a single method from {@link EventBus#registry}
     * @param method
     */
    public void unregisterMethod(Method method){
        if(!isMethodValid(method)) return;
        List<MethodData> list = registry.get(method.getParameters()[0].getType());
        if(list == null) return;
        for(MethodData methodData : new ArrayList<>(list)){
            if(methodData.getTarget() == method){
                list.remove(methodData);
                return;
            }
        }
        if(sorted){
            sort(method.getParameters()[0].getType());
        }
    }

    /**
     * Gets a list of methods {@link EventBus#registry} to invoke from the object specified.
     * Loops through the list and invokes each of the methods.
     * @param event is the key to retrieve a list of methods from {@link EventBus#registry}
     */
    public void call(Object event){
        final List<MethodData> list = registry.get(event.getClass());
        if(list == null) return;
        for(int i = 0; i < list.size(); i++){
            list.get(i).invoke(event);
        }
    }


    /**
     * Removes all methods under the "event" specified in the {@link EventBus#registry} HashMap
     * @param event
     */
    public void unregisterEvent(Object event){
        List<MethodData> list = registry.get(event);
        if(list == null) return;
        list.clear();
    }

    /**
     * Removes all entries in {@link EventBus#registry}
     */
    public void unregisterAll(){
        registry.clear();
    }

    /**
     * Checks if a method has the required necessities to be added to {@link EventBus#registry}
     * @param method the method to be checked
     * @return Returns if the method has the {@link SubscribeEvent} annotation and the parameter is 1.
     */
    private boolean isMethodValid(Method method){
        return method.isAnnotationPresent(SubscribeEvent.class) && method.getParameters().length == 1;
    }

    /**
     * Sorts all the lists of {@link MethodData} by {@link SubscribeEvent#priority()}
     * @param event The method list to be sorted from {@link EventBus#registry}
     */
    private void sort(Object event){
        List<MethodData> list = registry.get(event.getClass());
        if(list == null || list.isEmpty()) return;
        list.sort(Comparator.comparing(MethodData::getPriority));
    }

    /**
     * Sorts all values in {@link #registry} by {@link SubscribeEvent#priority()}
     */
    private void sortAll(){
        for(List<MethodData> entry : registry.values()){
            entry.sort(Comparator.comparing(MethodData::getPriority));
        }
    }
}

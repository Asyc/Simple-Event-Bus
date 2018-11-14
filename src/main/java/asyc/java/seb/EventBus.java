package asyc.java.seb;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {

    private HashMap<Object, List<MethodData>> registry = new HashMap<>();
    private boolean sorted;

    public EventBus(){
        this(false);
    }

    public EventBus(boolean sorted){
        this.sorted = sorted;
    }

    public void registerClass(Object obj){
        for(final Method method : obj.getClass().getDeclaredMethods()){
            if(!isMethodValid(method)) continue;
            registry.computeIfAbsent(method.getParameters()[0].getType(), ignored -> new CopyOnWriteArrayList<>()).add(new MethodData(obj, method));
        }
        if(sorted){
            sortAll();
        }
    }

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

    public void registerMethod(Object parent, Method method){
        if(!isMethodValid(method)) return;
        registry.computeIfAbsent(method.getParameters()[0].getType(), ignored -> new CopyOnWriteArrayList<>()).add(new MethodData(parent, method));
        if(sorted){
            sort(method.getParameters()[0].getType());
        }
    }

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

    public void call(Object event){
        final List<MethodData> list = registry.get(event.getClass());
        if(list == null) return;
        for(int i = 0; i < list.size(); i++){
            list.get(i).invoke(event);
        }
    }

    public void unregisterEvent(Object object){
        List<MethodData> list = registry.get(object);
        if(list == null) return;
        list.clear();
    }

    public void unregisterAll(){
        registry.clear();
    }

    private boolean isMethodValid(Method method){
        return method.isAnnotationPresent(SubscribeEvent.class) && method.getParameters().length == 1;
    }

    private void sort(Object event){
        List<MethodData> list = registry.get(event.getClass());
        if(list == null || list.isEmpty()) return;
        list.sort(Comparator.comparing(MethodData::getPriority));
    }

    private void sortAll(){
        for(List<MethodData> entry : registry.values()){
            entry.sort(Comparator.comparing(MethodData::getPriority));
        }
    }
}

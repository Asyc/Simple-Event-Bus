package asyc.java.seb.testing;

import asyc.java.seb.EventBus;
import asyc.java.seb.SubscribeEvent;

public class Main {

    public static final EventBus EVENT_BUS = new EventBus();

    public static void main(String[] args){
        Main instance = new Main();
        EVENT_BUS.registerClass(instance);
        EVENT_BUS.call(new Event());
    }

    @SubscribeEvent
    public void onEvent(Event event){
        System.out.println("Called!");
    }

}

class Event{

}

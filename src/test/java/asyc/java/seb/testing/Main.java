package asyc.java.seb.testing;

import me.asycc.seb.EventBus;
import me.asycc.seb.annotation.EventSubscriber;


public class Main {

    public static class Event{}

    public static void main(String[] args){

        EventBus bus = new EventBus();

        Main instance = new Main();

        bus.register(instance);

        long lastMS = System.currentTimeMillis();

        int invokeCount = 1000000;

        bus.post(new Event(), invokeCount);

        System.out.println("Posted " + invokeCount + " events in " + (System.currentTimeMillis() - lastMS));
    }

    @EventSubscriber
    public void onEvent(Event event) {
    }
}

package asyc.java.seb.testing;

import asyc.java.seb.EventBus;
import asyc.java.seb.SubscribeEvent;

public class Main {

    private static final EventBus EVENT_BUS = new EventBus();

    public static void main(String[] args){
        Main instance = new Main();
        EVENT_BUS.register(instance);
        TestEvent event = new TestEvent();
        int loops = 1000000;
        long lastMS = System.currentTimeMillis();
        for(int i = 0; i < loops; i++){
            EVENT_BUS.call(event);
        }
        System.out.println("Called " + Integer.toString(loops) + " events in " + Long.toString(System.currentTimeMillis() - lastMS) + " ms");

    }

    @SubscribeEvent
    public void test(TestEvent event){
        System.out.println(1);
    }
}

class TestEvent {

}

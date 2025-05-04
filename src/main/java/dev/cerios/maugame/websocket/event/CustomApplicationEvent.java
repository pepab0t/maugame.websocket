package dev.cerios.maugame.websocket.event;

import org.springframework.context.ApplicationEvent;

public abstract class CustomApplicationEvent extends ApplicationEvent {
    public CustomApplicationEvent(Object source) {
        super(source);
    }

    public String showDetails() {
        return this + ", thread: " + Thread.currentThread().getName();
    }
}

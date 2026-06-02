package ar.edu.utn.frc.tup.piii.engine.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameEventPublisher {

    private final List<GameEvent> events = new ArrayList<>();

    public void publish(GameEvent event) {
        events.add(event);
    }

    public List<GameEvent> drain() {
        List<GameEvent> copy = new ArrayList<>(events);
        events.clear();
        return copy;
    }

    public List<GameEvent> peek() {
        return Collections.unmodifiableList(events);
    }

    public boolean hasEvents() {
        return !events.isEmpty();
    }
}

package EventService.Concurrency;

import com.google.gson.JsonArray;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe EventList data structure.
 */
public class EventList {
    private final ReentrantReadWriteLock lock;
    private final Map<Integer, Event> events;

    /**
     * Constructor of EventList.
     */
    public EventList() {
        this.lock = new ReentrantReadWriteLock();
        this.events = new HashMap<>();
    }

    /**
     * Synchronized add method to create an Event and put it into HashMap.
     *
     * @param eventName
     * @param createUserId
     * @param numtickets
     * @return int
     *      - eventId, -1 for fail
     */
    public int add(String eventName, int createUserId, int numtickets) {
        int eventId;

        try {
            this.lock.writeLock().lock();

            eventId = this.events.size();
            Event newEvent = new Event.EventBuilder().setEventId(eventId).setEventName(eventName)
                    .setCreateUserId(createUserId).setNumtickets(numtickets).build();
            this.events.put(eventId, newEvent);
        }
        catch (Exception ignore) {
            eventId = -1;
        }
        finally {
            this.lock.writeLock().unlock();
        }

        return eventId;
    }

    /**
     * Synchronized get method to get Event from HashMap.
     *
     * @param eventId
     * @return Event
     */
    public Event get(int eventId) {
        this.lock.readLock().lock();
        Event event = this.events.get(eventId);
        this.lock.readLock().unlock();

        return event;
    }

    /**
     * Synchronized toString method to get a list of Events from HashMap with JSON format.
     *
     * @return String
     *      - a list of Events
     */
    public String toString() {
        JsonArray array = new JsonArray();

        this.lock.readLock().lock();
        for (Event event : this.events.values()) {
            array.add(event.toJsonObject());
        }
        this.lock.readLock().unlock();

        return array.toString();
    }
}

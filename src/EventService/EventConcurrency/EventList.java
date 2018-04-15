package EventService.EventConcurrency;

import EventService.EventServiceDriver;
import com.google.gson.JsonArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe EventList data structure.
 */
public class EventList {
    private final ReentrantReadWriteLock lock;
    private final Map<Integer, Event> events;
    private final Map<String, int[]> committed; // Map<uuid, int[2] {timestamp, eventId}>

    /**
     * Constructor of EventList.
     */
    public EventList() {
        this.lock = new ReentrantReadWriteLock();
        this.events = new HashMap<>();
        this.committed = new HashMap<>();
    }

    /**
     * Synchronized add method to create an Event and put it into HashMap.
     *
     * @param uuid
     * @param eventName
     * @param createUserId
     * @param numtickets
     * @param timestamp
     * @return int
     *      - eventId, -1 for fail
     */
    public int add(String uuid, String eventName, int createUserId, int numtickets, List<Integer> timestamp) {
        int eventId;

        try {
            this.lock.writeLock().lock();

            if (!this.committed.containsKey(uuid)) {
                eventId = this.events.size();
                Event newEvent = new Event.EventBuilder().setEventId(eventId).setEventName(eventName)
                        .setCreateUserId(createUserId).setNumtickets(numtickets).build();
                this.events.put(eventId, newEvent);

                timestamp.add(EventServiceDriver.lamportTimestamps.incrementAndGet());
                this.committed.put(uuid, new int[] {timestamp.get(0), eventId});

                String result = "[EventList] Event #" + eventId +
                        " has been created and committed with timestamp #" + timestamp.get(0) +
                        " and uuid: " + uuid;
                System.out.println(result);
            }
            else {
                timestamp.add(this.committed.get(uuid)[0]);
                eventId = this.committed.get(uuid)[1];
                System.out.println("[EventList] uuid: " + uuid + " has already been committed as Event #" + eventId);
            }
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

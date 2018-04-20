package EventService.EventConcurrency;

import EventService.EventServiceDriver;
import EventService.MultithreadingProcess.FullBackup;
import Usage.State;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe Event class.
 */
public class Event {
    private final ReentrantReadWriteLock lock;
    private final int eventId;
    private final String eventName;
    private final int createUserId;
    private final int numtickets;
    private int avail;
    private int purchased;

    /**
     * Builder pattern to create an Event class with part of immutable data.
     */
    static class EventBuilder {
        private int eventId;
        private String eventName;
        private int createUserId;
        private int numtickets;
        private int purchased = 0;

        /**
         * EventId setter.
         *
         * @param eventId
         * @return EventBuilder
         */
        EventBuilder setEventId(int eventId) {
            this.eventId = eventId;
            return this;
        }

        /**
         * EventName setter.
         *
         * @param eventName
         * @return EventBuilder
         */
        EventBuilder setEventName(String eventName) {
            this.eventName = eventName;
            return this;
        }

        /**
         * CreateUserId setter.
         *
         * @param createUserId
         * @return EventBuilder
         */
        EventBuilder setCreateUserId(int createUserId) {
            this.createUserId = createUserId;
            return this;
        }

        /**
         * Total number of tickets setter.
         *
         * @param numtickets
         * @return EventBuilder
         */
        EventBuilder setNumtickets(int numtickets) {
            this.numtickets = numtickets;
            return this;
        }

        /**
         * Puchased number setter for backup usage.
         *
         * @param purchased
         * @return EventBuilder
         */
        EventBuilder setPurchased(int purchased) {
            this.purchased = purchased;
            return this;
        }

        /**
         * Build method.
         *
         * @return Event
         */
        Event build() {
            return new Event(this);
        }
    }

    /**
     * Constructor of Event.
     *
     * @param eb
     */
    private Event(EventBuilder eb) {
        this.lock = new ReentrantReadWriteLock();
        this.eventId = eb.eventId;
        this.eventName = eb.eventName;
        this.createUserId = eb.createUserId;
        this.numtickets = eb.numtickets;
        this.purchased = eb.purchased;
        this.avail = this.numtickets - this.purchased;
    }

    /**
     * Synchronized purchase method to check availability and purchase.
     *
     * @param tickets
     * @return boolean
     *      - true for success, false for fail
     */
    public boolean purchase(String uuid, int tickets, List<Integer> timestamp) {
        boolean result;

        this.lock.writeLock().lock();
        if (tickets > 0 && EventServiceDriver.eventList.containsLog(uuid)) {
            checkMatch(uuid, timestamp);
            result = true;
        }
        else if (this.avail - tickets >= 0 && this.purchased + tickets >= 0 &&
                (this.avail - tickets) + (this.purchased + tickets) == this.numtickets) {
            this.avail -= tickets;
            this.purchased += tickets;

            if (tickets < 0) {
                EventServiceDriver.eventList.rollbackCommit(uuid);
                EventServiceDriver.lamportTimestamps.decrementAndGetWithOutLock();
                System.out.println("[Purchase] uuid: " + uuid +
                        " with timestamp #" + timestamp.get(0) + " has been rolled back");
            }
            else {
                int newTimestamp;
                if (EventServiceDriver.state == State.PRIMARY) {
                    newTimestamp = EventServiceDriver.lamportTimestamps.incrementAndGetWithOutLock();
                }
                else {
                    newTimestamp = EventServiceDriver.lamportTimestamps.incrementAndGet();
                }

                timestamp.add(newTimestamp);
                EventServiceDriver.eventList.commit(uuid, newTimestamp, this.eventId);
                System.out.println("[Purchase] Event " + this.eventId +
                        " has been purchased and committed with timestamp #" + newTimestamp +
                        " and uuid: " + uuid);
            }

            result = true;
        }
        else {
            result = false;
        }
        this.lock.writeLock().unlock();

        return result;
    }

    /**
     * Synchronized toJsonObject method to get the detail of Event with JSON format.
     *
     * @return JsonObject
     *      - event detail
     */
    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();

        this.lock.readLock().lock();
        obj.addProperty("eventid", this.eventId);
        obj.addProperty("eventname", this.eventName);
        obj.addProperty("userid", this.createUserId);
        obj.addProperty("avail", this.avail);
        obj.addProperty("purchased", this.purchased);
        this.lock.readLock().unlock();

        return obj;
    }

    private void checkMatch(String uuid, List<Integer> timestamp) {
        int[] logDetail = EventServiceDriver.eventList.getLogDetails(uuid);

        /*
        If the request has already been committed, check if the timestamp matches.
        If not request to primary for full backup. Finally, get the timestamp and eventId by uuid.
         */
        if (EventServiceDriver.state != State.PRIMARY &&
                timestamp.get(0) != null && timestamp.get(0) != logDetail[0]) {
            System.out.println("[EventList] uuid doesn't match with timestamp, requesting for full backup...");
            FullBackup fb = new FullBackup();
            fb.requestForBackup(true);
            timestamp.add(0, EventServiceDriver.eventList.getLogDetails(uuid)[0]);
        }
        else {
            System.out.println("[EventList] uuid: " + uuid +
                    " has already been committed with timestamp #" + logDetail[0]);
            timestamp.add(logDetail[0]);
        }
    }

    void lockForBackup() {
        this.lock.readLock().lock();
    }

    void unlockFromBackup() {
        this.lock.readLock().unlock();
    }
}

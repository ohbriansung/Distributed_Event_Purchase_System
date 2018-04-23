package EventService.EventConcurrency;

import EventService.EventServiceDriver;
import EventService.MultithreadingProcess.FullBackup;
import Usage.State;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

            if (this.committed.containsKey(uuid)) {
                checkMatch(uuid, timestamp);
                timestamp.add(0, this.committed.get(uuid)[0]);
                eventId = this.committed.get(uuid)[1];
            }
            else {
                eventId = this.events.size();
                Event newEvent = new Event.EventBuilder().setEventId(eventId).setEventName(eventName)
                        .setCreateUserId(createUserId).setNumtickets(numtickets).build();
                this.events.put(eventId, newEvent);

                /*
                Add the success timestamp into the list for the usage of the caller method.
                Log the uuid, timestamp, and eventId for later usage like checking duplicate.
                 */
                timestamp.add(EventServiceDriver.lamportTimestamps.incrementAndGet());
                this.committed.put(uuid, new int[] {timestamp.get(0), eventId});

                System.out.println("[EventList] Event " + eventId +
                        " has been created and committed with timestamp #" +timestamp.get(0) +
                        " and uuid: " + uuid);
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
     * Synchronized toJsonArray method to get the list of Events from HashMap with JSON format.
     *
     * @return JsonArray
     *      - a list of Events
     */
    public JsonArray toJsonArray() {
        JsonArray array = new JsonArray();

        this.lock.readLock().lock();
        for (Event event : this.events.values()) {
            array.add(event.toJsonObject());
        }
        this.lock.readLock().unlock();

        return array;
    }

    /**
     * Return the snapshot of current committed log.
     *
     * @return JsonArray
     */
    public JsonArray getCommittedLog() {
        JsonArray array = new JsonArray();

        for (String key : this.committed.keySet()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("uuid", key);
            obj.addProperty("timestamp", this.committed.get(key)[0]);
            obj.addProperty("eventId", this.committed.get(key)[1]);
            array.add(obj);
        }

        return array;
    }

    /**
     * Restore the backup data form the primary and return the result.
     *
     * @param data
     * @param lock
     * @return boolean
     */
    public boolean restoreData(JsonObject data, boolean lock) {
        boolean result;

        try {
            if (lock) {
                this.lock.writeLock().lock();
            }

            restoreEvents(data);
            restoreLog(data);
            EventServiceDriver.lamportTimestamps.set(data.get("timestamp").getAsInt());

            result = true;
        }
        catch (Exception ignored) {
            result = false;
        }
        finally {
            if (lock) {
                this.lock.writeLock().unlock();
            }
        }

        return result;
    }

    /**
     * Overwrite the current event list data with the new one.
     *
     * @param data
     * @throws Exception
     */
    private void restoreEvents(JsonObject data) throws Exception {
        this.events.clear();
        JsonArray array = (JsonArray) data.get("eventlist");

        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = (JsonObject) array.get(i);
            int eventId = obj.get("eventid").getAsInt();
            int purchased = obj.get("purchased").getAsInt();

            Event newEvent = new Event.EventBuilder().setEventId(eventId)
                    .setEventName(obj.get("eventname").getAsString())
                    .setCreateUserId(obj.get("userid").getAsInt())
                    .setNumtickets(obj.get("avail").getAsInt() + purchased)
                    .setPurchased(purchased)
                    .build();

            this.events.put(eventId, newEvent);
        }
    }

    /**
     * Overwrite the current log data with new one.
     *
     * @param data
     * @throws Exception
     */
    private void restoreLog(JsonObject data) throws Exception {
        this.committed.clear();
        JsonArray array = (JsonArray) data.get("committedlog");

        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = (JsonObject) array.get(i);
            int[] values = new int[] {obj.get("timestamp").getAsInt(), obj.get("eventId").getAsInt()};

            this.committed.put(obj.get("uuid").getAsString(), values);
        }
    }

    /**
     * Check duplicate uuid committed log.
     *
     * @param uuid
     * @return boolean
     */
    boolean containsLog(String uuid) {
        boolean result;

        this.lock.readLock().lock();
        result = this.committed.containsKey(uuid);
        this.lock.readLock().unlock();

        return result;
    }

    /**
     * Return the timestamp and the event data.
     *
     * @param uuid
     * @return int[]
     */
    int[] getLogDetails(String uuid) {
        int[] result;

        this.lock.readLock().lock();
        result = this.committed.get(uuid);
        this.lock.readLock().unlock();

        return result;
    }

    /**
     * Rollback a commit if the request to user service fails.
     *
     * @param uuid
     */
    void rollbackCommit(String uuid) {
        this.lock.writeLock().lock();
        this.committed.remove(uuid);
        this.lock.writeLock().unlock();
    }

    /**
     * Commit the uuid, timestamp, and event data into log.
     *
     * @param uuid
     * @param timestamp
     * @param eventId
     */
    void commit(String uuid, int timestamp, int eventId) {
        int[] logDeatils = new int[] {timestamp, eventId};

        this.lock.writeLock().lock();
        this.committed.put(uuid, logDeatils);
        this.lock.writeLock().unlock();
    }

    /**
     * For secondaries to check the correctness of existing uuid and timestamp.
     * If doesn't match, send a full backup request to the primary.
     *
     * @param uuid
     * @param timestamp
     */
    private void checkMatch(String uuid, List<Integer> timestamp) {
        /*
        If the request has already been committed, check if the timestamp matches.
        If not request to primary for full backup. Finally, get the timestamp and eventId by uuid.
         */
        if (EventServiceDriver.state != State.PRIMARY &&
                timestamp.get(0) != null &&
                timestamp.get(0) != this.committed.get(uuid)[0]) {
            System.out.println("[EventList] uuid doesn't match with timestamp, requesting for full backup...");
            FullBackup fb = new FullBackup();
            fb.requestForBackup(false);
        }
        else {
            System.out.println("[EventList] uuid: " + uuid +
                    " has already been committed with timestamp #" + this.committed.get(uuid)[0]);
        }
    }

    /**
     * Lock read from outside for backup usage.
     */
    public void lockForBackup() {
        this.lock.readLock().lock();

        for (Event event : events.values()) {
            event.lockForBackup();
        }
    }

    /**
     * Unlock read from outside.
     */
    public void unlockFromBackup() {
        for (Event event : events.values()) {
            event.unlockFromBackup();
        }

        this.lock.readLock().unlock();
    }
}

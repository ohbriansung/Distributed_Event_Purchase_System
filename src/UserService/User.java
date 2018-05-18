package UserService;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe User class.
 * Each user hold a list of tickets.
 */
public class User {
    private final ReentrantReadWriteLock lock;
    private final List<Integer> tickets;
    private final int userId;
    private final String username;

    /**
     * Builder pattern to create an User class with part of immutable data.
     */
    public static class UserBuilder {
        private int userId;
        private String username;

        /**
         * Constructor of UserBuilder.
         */
        public UserBuilder() {}

        /**
         * UserId setter.
         *
         * @param userId
         * @return UserBuilder
         */
        public UserBuilder setUserId(int userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Username setter.
         *
         * @param username
         * @return UserBuilder
         */
        public UserBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        /**
         * Build method.
         *
         * @return User
         */
        public User build() {
            return new User(this);
        }
    }

    /**
     * Constructor of User.
     *
     * @param ub
     */
    private User(UserBuilder ub) {
        this.lock = new ReentrantReadWriteLock();
        this.tickets = new ArrayList<>();
        this.userId = ub.userId;
        this.username = ub.username;
    }

    /**
     * Synchronized addTicket method.
     *
     * @param number
     * @param eventId
     */
    public void addTicket(int number, int eventId) {
        this.lock.writeLock().lock();
        for (int i = 0; i < number; i++) {
            this.tickets.add(eventId);
        }
        this.lock.writeLock().unlock();
    }

    /**
     * Synchronized removeTicket method.
     * First, check if there are enough tickets to remove.
     * If checked, remove number of tickets from the list.
     *
     * @param number
     * @param eventId
     * @return boolean
     *      - success or not
     */
    public boolean removeTicket(int number, int eventId) {
        boolean result = false;
        int count = 0;

        this.lock.writeLock().lock();
        for (int i = 0; i < this.tickets.size(); i++) {
            if (this.tickets.get(i) == eventId) {
                count++;
            }
        }

        if (number <= count) {
            for (int i = 0; i < number; i++) {
                this.tickets.remove(Integer.valueOf(eventId));
            }
            result = true;
        }
        this.lock.writeLock().unlock();

        return result;
    }

    /**
     * getTickets method to support toString method
     *
     * @return JsonArray
     *      - a list of eventId
     */
    private JsonArray getTickets() {
        JsonArray array = new JsonArray();

        for (Integer eventId : this.tickets) {
            JsonObject obj = new JsonObject();
            obj.addProperty("eventid", eventId);
            array.add(obj);
        }

        return array;
    }

    /**
     * Synchronized toString method to get the detail of User with JSON format.
     *
     * @return String
     *      - user detail
     */
    public String toString() {
        this.lock.readLock().lock();
        JsonObject obj = new JsonObject();
        obj.addProperty("userid", this.userId);
        obj.addProperty("username", this.username);
        obj.add("tickets", getTickets());
        this.lock.readLock().unlock();

        return obj.toString();
    }
}

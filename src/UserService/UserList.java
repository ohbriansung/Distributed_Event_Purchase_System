package UserService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe UserList data structure.
 */
public class UserList {
    private final ReentrantReadWriteLock lock;
    private final Map<Integer, User> users;
    private final Set<String> usernames;

    /**
     * Constructor of UserList.
     */
    public UserList() {
        this.lock = new ReentrantReadWriteLock();
        this.users = new HashMap<>();
        this.usernames = new HashSet<>();
    }

    /**
     * Synchronized add method.
     * Check duplicate username in HashSet, and create an User and put it into HashMap
     *
     * @param username
     * @return int
     *      - userId, -1 for fail
     */
    public int add(String username) {
        int userId = -1;

        this.lock.writeLock().lock();
        if (!this.usernames.contains(username)) {
            try {
                userId = this.users.size();
                User newUser = new User.UserBuilder().setUserId(userId).setUsername(username).build();
                this.users.put(userId, newUser);
                this.usernames.add(username);
            }
            catch (Exception ignore) {
                userId = -1;
            }
        }
        this.lock.writeLock().unlock();

        return userId;
    }

    /**
     * Synchronized get method to get User from HashMap.
     *
     * @param userId
     * @return User
     */
    public User get(int userId) {
        this.lock.readLock().lock();
        User user = this.users.get(userId);
        this.lock.readLock().unlock();

        return user;
    }

    /**
     * Synchronized contains method.
     *
     * @param userId
     * @return boolean
     *      - contains or not
     */
    public boolean contains(int userId) {
        this.lock.readLock().lock();
        boolean result = this.users.containsKey(userId);
        this.lock.readLock().unlock();

        return result;
    }
}

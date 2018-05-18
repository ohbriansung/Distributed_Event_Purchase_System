package UserService;

/**
 * UserTicket class to manage a user's ticket.
 */
public class UserTicket {
    private final int userId;

    /**
     * Constructor of UserTicket.
     *
     * @param userId
     */
    public UserTicket(int userId) {
        this.userId = userId;
    }

    /**
     * Add method to add tickets into a user's account.
     *
     * @param eventId
     * @param tickets
     * @return boolean
     *      - success or not
     */
    public boolean add(int eventId, int tickets) {
        boolean result;

        try {
            User user = UserServiceDriver.userList.get(this.userId);
            user.addTicket(tickets, eventId);
            result = true;
        }
        catch (Exception ignored) {
            result = false;
        }

        return result;
    }

    /**
     * Transfer method to transfer tickets between users.
     *
     * @param eventId
     * @param tickets
     * @param targetUser
     * @return boolean
     *      - success or not
     */
    public boolean transfer(int eventId, int tickets, int targetUser) {
        UserList userList = UserServiceDriver.userList;
        boolean result = false;

        if (this.userId != targetUser && userList.contains(targetUser)) {
            User user = userList.get(this.userId);
            boolean removeSuccess = user.removeTicket(tickets, eventId);

            if (removeSuccess) {
                User target = userList.get(targetUser);
                target.addTicket(tickets, eventId);
                result = true;
            }
        }

        return result;
    }
}

package UserService;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * UserServlet class to handle request for user account information.
 */
public class UserServlet extends BaseServlet {

    /**
     * doGet method to response the user account information,
     * including the tickets held by the user.
     *
     * @param request
     * @param response
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("request: GET " + request.getRequestURI());

        response.setContentType(UserServiceDriver.appType);
        response.setStatus(400);

        try {
            int userId = Integer.parseInt(request.getRequestURI().replaceFirst("/", ""));
            User user = UserServiceDriver.userList.get(userId);

            if (user != null) {
                PrintWriter pw = response.getWriter();
                String responseBody = user.toString();

                response.setStatus(200);
                pw.println(responseBody);
            }
        }
        catch (Exception ignored) {}
    }

    /**
     * doPost method to manage user's tickets.
     *
     * @param request
     * @param response
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("request: POST " + request.getRequestURI());

        response.setContentType(UserServiceDriver.appType);
        response.setStatus(400);

        try {
            String[] args = request.getRequestURI().split("/");
            int userId = Integer.parseInt(args[1]);

            if (args.length == 4 && UserServiceDriver.userList.contains(userId) &&
                    args[2].equals("tickets") && methods().contains(args[3])) {
                String requestBody = parseRequest(request);
                JsonObject body = parseJSON(requestBody);
                int eventId = body.get("eventid").getAsInt();
                int tickets = body.get("tickets").getAsInt();

                boolean success = false;
                UserTicket uTicket = new UserTicket(userId);
                if (args[3].equals("add")) {
                    success = uTicket.add(eventId, tickets);
                }
                else if (args[3].equals("transfer")) {
                    int targetUser = body.get("targetuser").getAsInt();
                    success = uTicket.transfer(eventId, tickets, targetUser);
                }

                if (success) {
                    response.setStatus(200);
                }
            }
        }
        catch (Exception ignored) {}
    }

    /**
     * Return the methods user's ticket method should handle.
     *
     * @return Set
     */
    private Set<String> methods() {
        Set<String> methods = new HashSet<>();
        methods.add("add");
        methods.add("transfer");

        return methods;
    }
}

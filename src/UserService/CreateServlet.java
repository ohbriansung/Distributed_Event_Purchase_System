package UserService;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * CreateServlet class to handle request for creating user.
 */
public class CreateServlet extends BaseServlet {

    /**
     * doPost method for creating a new user and response with the user id.
     *
     * @param request
     * @param response
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("request: POST /create");

        response.setContentType(UserServiceDriver.appType);
        response.setStatus(400);

        try {
            String requestBody = parseRequest(request);
            JsonObject body = parseJSON(requestBody);

            String username = body.get("username").getAsString();
            int userId = UserServiceDriver.userList.add(username);

            if (userId > -1) {
                PrintWriter pw = response.getWriter();
                JsonObject responseBody = getJsonResponse(userId);

                response.setStatus(200);
                pw.println(responseBody.toString());
            }
        }
        catch (Exception ignored) {}
    }

    /**
     * Generate a JSON format String with parameters.
     *
     * @param userId
     * @return JsonObject
     */
    private JsonObject getJsonResponse(int userId) {
        JsonObject obj = new JsonObject();
        obj.addProperty("userid", userId);

        return obj;
    }
}

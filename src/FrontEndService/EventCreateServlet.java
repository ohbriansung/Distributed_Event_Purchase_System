package FrontEndService;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

/**
 * EventCreateServlet to handle request for creating event.
 */
public class EventCreateServlet extends BaseServlet {

    /**
     * doPost method to check the user account with User Service,
     * then send a POST request to Event Service to purchase tickets.
     *
     * @param request
     * @param response
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("[Servlet] POST request " + request.getRequestURI());

        response.setContentType(FrontEndServiceDriver.APP_TYPE);
        response.setStatus(400);

        try {
            String requestBody = parseRequest(request);
            JsonObject body = (JsonObject) parseJson(requestBody);

            if (checkUser(body)) {
                String url = FrontEndServiceDriver.primaryEventService +
                        request.getRequestURI().replaceFirst("/events", "");
                HttpURLConnection connection = doPostRequest(url, body);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    PrintWriter pw = response.getWriter();
                    JsonObject responseBody = (JsonObject) parseResponse(connection);

                    response.setStatus(200);
                    pw.println(responseBody.toString());
                }
            }
        }
        catch (Exception ignored) {}
    }

    /**
     * Send a GET request to User Service to check if a user exists.
     *
     * @param body
     * @return boolean
     *      - user exists or not
     * @throws Exception
     */
    private boolean checkUser(JsonObject body) throws Exception {
        int userId = body.get("userid").getAsInt();
        String url = FrontEndServiceDriver.primaryUserService + "/" + userId;
        HttpURLConnection connection = doGetRequest(url);

        return (connection.getResponseCode() == HttpURLConnection.HTTP_OK);
    }
}

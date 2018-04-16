package FrontEndService;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * UserServlet class to handle request for user account information and tickets transfer.
 */
public class UserServlet extends BaseServlet {

    /**
     * doGet method to send a GET request to User Service to get the user account information,
     * and to send a GET request to Event Service to get the information of events of user's tickets.
     *
     * @param request
     * @param response
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("[Servlet] GET request " + request.getRequestURI());

        response.setContentType(FrontEndServiceDriver.APP_TYPE);
        response.setStatus(400);

        try {
            String url = FrontEndServiceDriver.primaryUserService +
                    request.getRequestURI().replaceFirst("/users", "");
            HttpURLConnection connection = doGetRequest(url);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                PrintWriter pw = response.getWriter();
                JsonObject responseBody = (JsonObject) parseResponse(connection);
                addEventDetail(responseBody);

                response.setStatus(200);
                pw.println(responseBody.toString());
            }
        }
        catch (Exception ignored) {}
    }

    /**
     * doPost method to send POST request to User Service to transfer tickets between users.
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
            String url = FrontEndServiceDriver.primaryUserService +
                    request.getRequestURI().replaceFirst("/users", "");

            if (url.endsWith("/tickets/transfer")) {
                HttpURLConnection connection = doPostRequest(url, body);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    response.setStatus(200);
                }
            }
        }
        catch (Exception ignored) {}
    }

    /**
     * Add event detail into user account information.
     *
     * @param body
     * @throws Exception
     */
    void addEventDetail(JsonObject body) throws Exception {
        List<Integer> tickets = getTickets(body);
        JsonArray array = getEvents(tickets);
        body.remove("tickets");
        body.add("tickets", array);
    }

    /**
     * Return the list of user's tickets.
     *
     * @param obj
     * @return List
     * @throws NumberFormatException
     */
    private List<Integer> getTickets(JsonObject obj) throws NumberFormatException {
        List<Integer> tickets = new ArrayList<>();
        JsonArray array = (JsonArray) obj.get("tickets");
        int total = array.size();

        for (int i = 0; i < total; i++) {
            JsonObject ticket = (JsonObject) array.get(i);
            tickets.add(ticket.get("eventid").getAsInt());
        }

        return tickets;
    }

    /**
     * Send GET requests to Event Service to get the information of events,
     * and return the list with JsonArray format.
     *
     * @param tickets
     * @return JsonArray
     * @throws JsonParseException
     * @throws IOException
     */
    private JsonArray getEvents(List<Integer> tickets) throws JsonParseException, IOException {
        JsonArray array = new JsonArray();

        for (Integer eventId : tickets) {
            String url = FrontEndServiceDriver.primaryEventService + "/" + eventId;
            HttpURLConnection connection = doGetRequest(url);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JsonObject obj = (JsonObject) parseResponse(connection);
                array.add(obj);
            }
            else {
                throw new IOException();
            }
        }

        return array;
    }
}

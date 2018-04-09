package EventService.Servlet;

import EventService.Concurrency.Event;
import EventService.EventServiceDriver;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * PurchaseServlet class to handle request for purchasing tickets.
 */
public class PurchaseServlet extends BaseServlet {

    /**
     * doPost to purchase tickets.
     * After purchased, send a POST request to User Service to add tickets into a user's account.
     * If the User Service response with 400, rollback the tickets just purchased.
     *
     * @param request
     * @param response
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("request: POST " + request.getRequestURI());

        response.setContentType(EventServiceDriver.APP_TYPE);
        response.setStatus(400);

        try {
            String requestBody = parseRequest(request);
            JsonObject body = (JsonObject) parseJson(requestBody);

            int eventIdURI = Integer.parseInt(request.getRequestURI().replaceFirst("/purchase/", ""));
            int eventId = body.get("eventid").getAsInt();
            int userId = body.get("userid").getAsInt();
            int tickets = body.get("tickets").getAsInt();

            if (eventIdURI == eventId && tickets > 0) {
                Event event = EventServiceDriver.eventList.get(eventId);

                if (event != null) {
                    boolean success = event.purchase(tickets);

                    if (success) {
                        int responseCode = doPostUserTickets(userId, eventId, tickets);

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            response.setStatus(200);
                        }
                        else { // rollback
                            tickets *= -1;
                            event.purchase(tickets);
                        }
                    }
                }
            }
        }
        catch (Exception ignored) {}
    }

    /**
     * Send a POST request to User Service to add tickets
     * into user's account and get the response code.
     *
     * @param userId
     * @param eventId
     * @param tickets
     * @return int
     */
    private int doPostUserTickets(int userId, int eventId, int tickets) {
        String primaryUser = EventServiceDriver.userServiceList.getPrimary();
        String url = primaryUser + "/" + userId + "/tickets/add";
        JsonObject body = new JsonObject();
        body.addProperty("eventid", eventId);
        body.addProperty("tickets", tickets);

        int responseCode;
        try {
            HttpURLConnection connection = doPostRequest(url, body);
            responseCode = connection.getResponseCode();
        }
        catch (IOException ignored) {
            responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
        }

        return responseCode;
    }
}

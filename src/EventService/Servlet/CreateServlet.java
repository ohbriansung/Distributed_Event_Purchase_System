package EventService.Servlet;

import EventService.EventServiceDriver;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

/**
 * CreateServlet class to handle request creating event.
 */
public class CreateServlet extends BaseServlet {

    /**
     * doPost method to create event and response with event id.
     *
     * @param request
     * @param response
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("request: POST /create");

        response.setContentType(EventServiceDriver.APP_TYPE);
        response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        try {
            String requestBody = parseRequest(request);
            JsonObject body = (JsonObject) parseJson(requestBody);

            String eventName = body.get("eventname").getAsString();
            int createUserId = body.get("userid").getAsInt();
            int numtickets = body.get("numtickets").getAsInt();
            int eventId = EventServiceDriver.eventList.add(eventName, createUserId, numtickets);

            if (eventId > -1) {
                PrintWriter pw = response.getWriter();
                JsonObject responseBody = getJSONResponse(eventId);

                response.setStatus(HttpURLConnection.HTTP_OK);
                pw.println(responseBody.toString());
            }
        }
        catch (Exception ignored) {}
    }

    /**
     * Generate a JSON format response with parameter.
     *
     * @param eventId
     * @return JsonObject
     */
    private JsonObject getJSONResponse(int eventId) {
        JsonObject obj = new JsonObject();
        obj.addProperty("eventid", eventId);

        return obj;
    }
}

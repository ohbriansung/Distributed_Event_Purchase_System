package EventService.Servlet;

import EventService.EventServiceDriver;
import Usage.State;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

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
        System.out.println("[Servlet] POST request /create");

        response.setContentType(EventServiceDriver.APP_TYPE);
        response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        try {
            String requestBody = parseRequest(request);
            JsonObject body = (JsonObject) parseJson(requestBody);

            timestampBlock(body);

            String uuid = body.get("uuid").getAsString();
            String eventName = body.get("eventname").getAsString();
            int createUserId = body.get("userid").getAsInt();
            int numtickets = body.get("numtickets").getAsInt();

            List<Integer> timestamp = new ArrayList<>();
            if (EventServiceDriver.state != State.PRIMARY) {
                timestamp.add(body.get("timestamp").getAsInt());
            }

            /*
            Pass a container into add method so we can retrieve the timestamp.
            Generate a Lamport Timestamp right after creating the new event.
             */
            int eventId = EventServiceDriver.eventList.add(uuid, eventName, createUserId, numtickets, timestamp);

            if (eventId > -1) {
                primaryReplication(request.getRequestURI(), body, timestamp.get(0));

                // response after completing replication
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

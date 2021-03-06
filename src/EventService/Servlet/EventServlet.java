package EventService.Servlet;

import EventService.EventConcurrency.Event;
import EventService.EventServiceDriver;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

/**
 * EventServlet class to handle request for event information.
 */
public class EventServlet extends BaseServlet {

    /**
     * doGet method to response the event information.
     *
     * @param request
     * @param response
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("[Servlet] GET request " + request.getRequestURI());

        response.setContentType(EventServiceDriver.APP_TYPE);
        response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        try {
            int eventId = Integer.parseInt(request.getRequestURI().replaceFirst("/", ""));
            Event event = EventServiceDriver.eventList.get(eventId);

            if (event != null) {
                PrintWriter pw = response.getWriter();
                JsonObject responseBody = event.toJsonObject();

                response.setStatus(HttpURLConnection.HTTP_OK);
                pw.println(responseBody.toString());
            }
        }
        catch (Exception ignored) {}
    }
}

package EventService;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

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
        System.out.println("request: GET " + request.getRequestURI());

        response.setContentType(EventServiceDriver.appType);
        response.setStatus(400);

        try {
            int eventId = Integer.parseInt(request.getRequestURI().replaceFirst("/", ""));
            Event event = EventServiceDriver.eventList.get(eventId);

            if (event != null) {
                PrintWriter pw = response.getWriter();
                JsonObject responseBody = event.toJsonObject();

                response.setStatus(200);
                pw.println(responseBody.toString());
            }
        }
        catch (Exception ignored) {}
    }
}

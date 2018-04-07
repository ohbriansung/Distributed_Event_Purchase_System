package EventService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * ListServlet class to handle the request for the list of events.
 */
public class ListServlet extends BaseServlet {

    /**
     * doGet method to response the list of events.
     *
     * @param request
     * @param response
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("request: GET /list");

        response.setContentType(EventServiceDriver.appType);
        response.setStatus(400);

        try {
            PrintWriter pw = response.getWriter();
            String responseBody = EventServiceDriver.eventList.toString();

            response.setStatus(200);
            pw.println(responseBody);
        }
        catch (IOException ignored) {}
    }
}

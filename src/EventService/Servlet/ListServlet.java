package EventService.Servlet;

import EventService.EventServiceDriver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

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

        response.setContentType(EventServiceDriver.APP_TYPE);
        response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        try {
            PrintWriter pw = response.getWriter();
            String responseBody = EventServiceDriver.eventList.toJsonArray().toString();

            response.setStatus(HttpURLConnection.HTTP_OK);
            pw.println(responseBody);
        }
        catch (IOException ignored) {}
    }
}

package FrontEndService;

import com.google.gson.JsonArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

/**
 * EventListServlet class to handle request for getting event list.
 */
public class EventListServlet extends BaseServlet {

    /**
     * doGet method to send a GET request to Event Service to get the list of events.
     *
     * @param request
     * @param response
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("[Servlet] GET request /events");

        response.setContentType(FrontEndServiceDriver.APP_TYPE);
        response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        try {
            String url = FrontEndServiceDriver.primaryEventService + "/list";
            HttpURLConnection connection = doGetRequest(url);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                PrintWriter pw = response.getWriter();
                JsonArray responseBody = (JsonArray) parseResponse(connection);

                response.setStatus(HttpURLConnection.HTTP_OK);
                pw.println(responseBody.toString());
            }
        }
        catch (Exception ignored) {}
    }
}

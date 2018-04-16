package FrontEndService.Servlet;

import FrontEndService.FrontEndServiceDriver;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

/**
 * EventServlet to handle request for event information and purchase.
 */
public class EventServlet extends BaseServlet {

    /**
     * doGet method to send a GET request to Event Service to get the event information.
     *
     * @param request
     * @param response
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("[Servlet] GET request " + request.getRequestURI());

        response.setContentType(FrontEndServiceDriver.APP_TYPE);
        response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        try {
            String url = FrontEndServiceDriver.primaryEventService +
                    request.getRequestURI().replaceFirst("/events", "");
            HttpURLConnection connection = doGetRequest(url);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                PrintWriter pw = response.getWriter();
                JsonObject responseBody = (JsonObject) parseResponse(connection);

                response.setStatus(HttpURLConnection.HTTP_OK);
                pw.println(responseBody.toString());
            }
        }
        catch (Exception ignored) {}
    }

    /**
     * doPost method to send a POST method to Event Service to purchase tickets.
     *
     * @param request
     * @param response
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("[Servlet] POST request " + request.getRequestURI());

        response.setContentType(FrontEndServiceDriver.APP_TYPE);
        response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        try {
            String requestBody = parseRequest(request);
            JsonObject body = (JsonObject) parseJson(requestBody);
            String[] arguments = request.getRequestURI().replace("/events/", "").split("/");
            int tickets = body.get("tickets").getAsInt();

            JsonObject newRequestBody = getNewRequestBody(arguments, tickets);
            String url = FrontEndServiceDriver.primaryEventService + "/" + arguments[1] + "/" + arguments[0];
            HttpURLConnection connection = doPostRequest(url, newRequestBody);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                response.setStatus(HttpURLConnection.HTTP_OK);
            }
        }
        catch (Exception ignored) {}
    }

    /**
     * Generate a JSON format String with parameters.
     *
     * @param arguments
     * @param tickets
     * @return JsonObject
     * @throws NumberFormatException
     */
    private JsonObject getNewRequestBody(String[] arguments, int tickets) throws NumberFormatException {
        JsonObject obj = new JsonObject();
        obj.addProperty("userid", Integer.parseInt(arguments[2]));
        obj.addProperty("eventid", Integer.parseInt(arguments[0]));
        obj.addProperty("tickets", tickets);

        return obj;
    }
}

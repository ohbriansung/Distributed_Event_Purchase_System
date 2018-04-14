package EventService.Servlet;

import EventService.EventServiceDriver;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.List;

public class GreetServlet extends BaseServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("request: POST /greet");

        response.setContentType(EventServiceDriver.APP_TYPE);
        response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        try {
            PrintWriter pw = response.getWriter();
            JsonArray responseBody = getServiceList();
            addSender(request);

            response.setStatus(HttpURLConnection.HTTP_OK);
            pw.println(responseBody.toString());
        }
        catch (Exception ignored) {}
    }

    private void addSender(HttpServletRequest request) throws IOException {
        String requestBody = parseRequest(request);
        JsonObject body = (JsonObject) parseJson(requestBody);

        String host = request.getRemoteAddr();
        String port = body.get("port").getAsString();
        EventServiceDriver.eventServiceList.addService(host + ":" + port);
    }
}

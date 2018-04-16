package EventService.Servlet;

import EventService.EventServiceDriver;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;

public class BackupServlet extends BaseServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("[Servlet] POST request /backup");

        response.setContentType(EventServiceDriver.APP_TYPE);
        response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        try {
            String requestBody = parseRequest(request);
            JsonObject body = (JsonObject) parseJson(requestBody);

            EventServiceDriver.eventList.restoreData(body);
            response.setStatus(HttpURLConnection.HTTP_OK);
            System.out.println("[Backup] Data has been restored");
        }
        catch (Exception ignored) {
            System.out.println("[Backup] Failed to restore data");
        }
    }
}

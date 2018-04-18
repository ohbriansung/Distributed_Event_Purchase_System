package EventService.Servlet;

import EventService.EventServiceDriver;
import EventService.MultithreadingProcess.FullBackup;
import Usage.State;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

public class BackupServlet extends BaseServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("[Servlet] GET request /backup");

        response.setContentType(EventServiceDriver.APP_TYPE);
        response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        if (EventServiceDriver.state == State.PRIMARY) {
            try {
                PrintWriter pw = response.getWriter();
                FullBackup fb = new FullBackup();
                JsonObject responseBody = fb.getData();

                response.setStatus(HttpURLConnection.HTTP_OK);
                pw.println(responseBody);
            }
            catch (Exception ignored) {}
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("[Servlet] POST request /backup");

        response.setContentType(EventServiceDriver.APP_TYPE);
        response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        try {
            String requestBody = parseRequest(request);
            JsonObject body = (JsonObject) parseJson(requestBody);

            EventServiceDriver.eventList.restoreData(body, true);
            response.setStatus(HttpURLConnection.HTTP_OK);
            System.out.println("[Backup] Data has been restored");
        }
        catch (Exception ignored) {
            System.out.println("[Backup] Failed to restore data");
        }
    }
}

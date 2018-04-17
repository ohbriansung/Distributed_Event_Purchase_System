package EventService.Servlet;

import EventService.EventServiceDriver;
import EventService.MultithreadingProcess.FullBackup;
import Usage.State;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

public class GreetServlet extends BaseServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(EventServiceDriver.APP_TYPE);
        response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        try {
            PrintWriter pw = response.getWriter();
            JsonArray responseBody = getServiceList();
            addSender(request);

            response.setStatus(HttpURLConnection.HTTP_OK);
            pw.println(responseBody.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addSender(HttpServletRequest request) throws Exception {
        String uri = request.getRequestURI();
        String requestBody = parseRequest(request);
        JsonObject body = (JsonObject) parseJson(requestBody);
        String address = request.getRemoteAddr() + ":" + body.get("port").getAsString();

        System.out.println("[Servlet] POST request " + uri + " from " + address);

        switch (uri) {
            case "/greet/event":
                addToEventServiceList(address);
                break;
            case "/greet/frontend":
                addToFrontEndServiceList(address);
                break;
            case "/greet/loadbalancing":
                break;
            default:
                throw new Exception("[Servlet] Bad request: " + uri);
        }
    }

    private void addToEventServiceList(String address) {
        if (!EventServiceDriver.eventServiceList.contains(address)) {
            System.out.println("[Servlet] Add " + address + " into event service list");
            EventServiceDriver.eventServiceList.addService(address);

            if (EventServiceDriver.state == State.PRIMARY) {
                FullBackup fb = new FullBackup(address);
                fb.startBackup();
            }
        }
    }

    private void addToFrontEndServiceList(String address) {
        if (!EventServiceDriver.frontendServiceList.contains(address)) {
            System.out.println("[Servlet] Add " + address + " into frontend service list");
            EventServiceDriver.frontendServiceList.addService(address);
        }
    }
}

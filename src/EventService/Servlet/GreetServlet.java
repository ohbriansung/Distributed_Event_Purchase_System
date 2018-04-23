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

/**
 * GreetServlet to handle the greet request.
 */
public class GreetServlet extends BaseServlet {

    /**
     * Add the sender to the service list.
     * Reply with the snapshot of current service list to maintain the membership.
     *
     * @param request
     * @param response
     */
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

    /**
     * Parse the request body and add the address of sender into the list based on uri methods.
     *
     * @param request
     * @throws Exception
     */
    private void addSender(HttpServletRequest request) throws Exception {
        String uri = request.getRequestURI();
        String requestBody = parseRequest(request);
        JsonObject body = (JsonObject) parseJson(requestBody);
        String address = request.getRemoteAddr() + ":" + body.get("port").getAsString();

        switch (uri) {
            case "/greet/event":
                addToEventServiceList(address);
                break;
            case "/greet/frontend":
                addToFrontEndServiceList(address);
                break;
            default:
                throw new Exception("[Servlet] Bad request: " + uri);
        }
    }

    /**
     * Add address into event service list.
     *
     * @param address
     */
    private void addToEventServiceList(String address) {
        if (!EventServiceDriver.eventServiceList.contains(address)) {
            System.out.println("[Servlet] Added " + address + " into event service list");
            EventServiceDriver.eventServiceList.addService(address);

            if (EventServiceDriver.state == State.PRIMARY) {
                FullBackup fb = new FullBackup();
                fb.startBackup(address);
            }
        }
    }

    /**
     * Add address into frontend service list.
     *
     * @param address
     */
    private void addToFrontEndServiceList(String address) {
        if (!EventServiceDriver.frontendServiceList.contains(address)) {
            System.out.println("[Servlet] Added " + address + " into frontend service list");
            EventServiceDriver.frontendServiceList.addService(address);
        }
    }
}

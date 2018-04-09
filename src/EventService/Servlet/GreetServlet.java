package EventService.Servlet;

import EventService.EventServiceDriver;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

public class GreetServlet extends BaseServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("request: GET /greet");

        response.setContentType(EventServiceDriver.APP_TYPE);
        response.setStatus(400);

        try {
            PrintWriter pw = response.getWriter();
            JsonArray responseBody = getServiceList();
            addSender(request);

            response.setStatus(200);
            pw.println(responseBody.toString());
        }
        catch (Exception ignored) {}
    }

    private JsonArray getServiceList() {
        JsonArray array = new JsonArray();
        List<String> list = EventServiceDriver.eventServiceList.getList();

        for (String service : list) {
            JsonObject obj = new JsonObject();
            obj.addProperty("service", service);
            array.add(obj);
        }

        return array;
    }

    private void addSender(HttpServletRequest request) {
        String host = request.getRemoteAddr();
        String port = request.getParameter("fromport");
        System.out.println("adding: " + host + ":" + port);
        EventServiceDriver.eventServiceList.addService(host + ":" + port);
    }
}

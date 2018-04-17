package FrontEndService;

import com.google.gson.JsonObject;

import java.net.HttpURLConnection;

public class Gossip extends BaseServlet implements Runnable {

    @Override
    public void run() {
        while (FrontEndServiceDriver.alive) {
            String address = FrontEndServiceDriver.primaryEventService;
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("port", FrontEndServiceDriver.properties.get("port"));

            try {
                HttpURLConnection connection = doPostRequest(address + "/greet/frontend", requestBody);
                connection.getResponseCode();
                Thread.sleep(1000);
            }
            catch (Exception ignored) {}
        }
    }
}

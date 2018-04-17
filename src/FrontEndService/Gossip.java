package FrontEndService;

import com.google.gson.JsonObject;

import java.net.HttpURLConnection;

public class Gossip extends BaseServlet implements Runnable {

    @Override
    public void run() {
        while (FrontEndServiceDriver.alive) {
            String address = FrontEndServiceDriver.primaryEventService;

            System.out.println("[Gossip] Start gossip with " + address);
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("port", FrontEndServiceDriver.properties.get("port"));

            try {
                HttpURLConnection connection = doPostRequest(address + "/greet/frontend", requestBody);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    System.out.println("[Gossip] " + address + " is there");
                }
                else {
                    System.out.println("[Gossip] " + address + " is currently unreachable");
                }

                Thread.sleep(10000);
            }
            catch (Exception ignored) {}
        }
    }
}

package EventService.Usage;

import EventService.Servlet.BaseServlet;
import EventService.EventServiceDriver;
import Usage.ServiceName;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Gossip extends BaseServlet implements Runnable {
    private ExecutorService executor;

    public Gossip() {
        this.executor = Executors.newFixedThreadPool(4);
    }

    @Override
    public void run() {
        while (EventServiceDriver.alive) {
            List<String> services = EventServiceDriver.eventServiceList.getList();
            String uri = "/greet";

            for (String url : services) {
                if (isNotMe(url)) {
                    this.executor.submit(new GreetAndUpdate(url, uri));
                }
            }

            try {
                Thread.sleep(10000);
            }
            catch (InterruptedException ie) {
                System.err.println(ie);
            }
        }
    }

    private boolean isNotMe(String url) {
        String myUrl = EventServiceDriver.properties.get("host") +
                ":" + EventServiceDriver.properties.get("port");

        return !myUrl.equals(url);
    }

    private class GreetAndUpdate implements Runnable {
        private String url;
        private String uri;

        private GreetAndUpdate(String url, String uri) {
            this.url = url;
            this.uri = uri;
        }

        @Override
        public void run() {
            try {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("port", EventServiceDriver.properties.get("port"));
                HttpURLConnection connection = doPostRequest(this.url + this.uri, requestBody);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    JsonArray responseBody = (JsonArray) parseResponse(connection);
                    updateServiceList(responseBody);
                }
                else {
                    wait(500); // wait for other threads to finish
                    EventServiceDriver.eventServiceList.removeService(this.url);
                }
            }
            catch (Exception ignored) {}
        }

        private void updateServiceList(JsonArray newList) {
            for (int i = 0; i < newList.size(); i++) {
                try {
                    JsonObject obj = (JsonObject) newList.get(i);
                    String service = obj.get("service").getAsString();

                    if (service.equals(ServiceName.FRONT_END.toString())) {
                        EventServiceDriver.frontendServiceList.addService(obj.get("address").getAsString());
                    }
                    else if (service.equals(ServiceName.EVENT.toString())) {
                        EventServiceDriver.eventServiceList.addService(obj.get("address").getAsString());
                    }
                    else if (service.equals(ServiceName.USER.toString())) {
                        EventServiceDriver.userServiceList.addService(obj.get("address").getAsString());
                    }
                }
                catch (Exception ignored) {}
            }
        }
    }
}

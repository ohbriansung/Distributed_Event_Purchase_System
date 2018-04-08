package EventService;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Gossip extends BaseServlet implements Runnable {
    private ExecutorService executor;

    Gossip() {
        this.executor = Executors.newFixedThreadPool(4);
    }

    @Override
    public void run() {
        while (EventServiceDriver.alive) {
            List<String> services = EventServiceDriver.eventServiceList.getList();

            for (String url : services) {
                if (isNotMe(url)) {
                    this.executor.submit(new GreetAndUpdate(url));
                }
            }

            try {
                Thread.sleep(5000);
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

        private GreetAndUpdate(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                HttpURLConnection connection = doGetRequest(this.url + "/greet");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    JsonArray responseBody = (JsonArray) parseResponse(connection);
                    updateEventServiceList(responseBody);
                }
                else {
                    wait(500); // wait for other threads to finish
                    EventServiceDriver.eventServiceList.removeService(this.url);
                }
            }
            catch (Exception ignored) {}
        }

        private void updateEventServiceList(JsonArray newList) {
            for (int i = 0; i < newList.size(); i++) {
                try {
                    JsonObject obj = (JsonObject) newList.get(i);
                    String service = obj.get("service").getAsString();
                    EventServiceDriver.eventServiceList.addService(service);
                }
                catch (Exception ignored) {}
            }
        }
    }
}

package EventService.MultithreadingProcess;

import EventService.EventServiceDriver;
import EventService.Servlet.BaseServlet;
import com.google.gson.JsonObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class Replication extends BaseServlet {
    private final String uri;
    private final JsonObject requestBody;
    private final int timestamp;

    public Replication(String uri, JsonObject requestBody, int timestamp) {
        this.uri = uri;
        this.requestBody = requestBody;
        this.timestamp = timestamp;
        this.requestBody.addProperty("timestamp", timestamp);
    }

    public void startReplicate() {
        List<String> services = EventServiceDriver.eventServiceList.getList();
        List<Thread> currentTasks = new ArrayList<>();

        for (String url : services) {
            if (!getCurrentAddress().equals(url)) {
                Thread newTask = new Thread(new SendReplicate(url));
                currentTasks.add(newTask);
                newTask.start();

                if (this.requestBody.get("demo") != null) {
                    // simulate election after different version, and resending request from frontend to new primary
                    try {
                        newTask.join();
                        System.out.println("[Demo] One replicate has been sent, shutting down...");
                    }
                    catch (InterruptedException ignored) {}
                    System.exit(-1);
                }
            }
        }

        try {
            for (Thread task : currentTasks) {
                task.join();
            }
        }
        catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    private class SendReplicate implements Runnable {
        private final String url;

        private SendReplicate(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                HttpURLConnection connection = doPostRequest(
                        this.url + Replication.this.uri, Replication.this.requestBody);

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new Exception();
                }

                System.out.println("[Replication] Sent replicate #" +
                        Replication.this.requestBody.get("timestamp").getAsInt() + " to " + this.url);
            }
            catch (Exception ignored) {
                ignored.printStackTrace();
                System.out.println("[Replication] Remove " + this.url + " from the list");
                EventServiceDriver.eventServiceList.removeService(this.url);
            }
        }
    }
}

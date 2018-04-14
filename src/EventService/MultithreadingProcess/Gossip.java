package EventService.MultithreadingProcess;

import EventService.Servlet.BaseServlet;
import EventService.EventServiceDriver;
import Usage.ServiceName;
import Usage.State;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.HttpURLConnection;
import java.util.*;

public class Gossip extends BaseServlet implements Runnable {

    @Override
    public void run() {
        while (EventServiceDriver.alive) {
            try {
                if (EventServiceDriver.state != State.CANDIDATE) {
                    List<Thread> currentTasks = new ArrayList<>();
                    Vector<String> toRemove = new Vector<>();
                    List<String> services = EventServiceDriver.eventServiceList.getList();

                    for (String url : services) {
                        if (!url.equals(getCurrentAddress())) {
                            System.out.println("[Gossip] Start gossip with " + url);
                            Thread newTask = new Thread(new GreetAndUpdate(url, toRemove));
                            currentTasks.add(newTask);
                            newTask.start();
                        }
                    }

                    for (Thread task : currentTasks) {
                        task.join();
                    }

                    // remove after finishing all tasks
                    for (String service : toRemove) {
                        System.out.println("[Gossip] Remove " + service + " from the list");
                        EventServiceDriver.eventServiceList.removeService(service);

                        // start an election if removing primary
                        if (service.equals(EventServiceDriver.eventServiceList.getPrimary())) {
                            Thread election = new Thread(new BullyElection());
                            election.start();
                            election.join();
                        }
                    }
                }

                Thread.sleep(10000);
            }
            catch (InterruptedException ie) {
                System.err.println(ie);
            }
        }
    }

    private class GreetAndUpdate implements Runnable {
        private String url;
        private Vector<String> toRemove;

        private GreetAndUpdate(String url, Vector<String> toRemove) {
            this.url = url;
            this.toRemove = toRemove;
        }

        @Override
        public void run() {
            try {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("port", EventServiceDriver.properties.get("port"));
                HttpURLConnection connection = doPostRequest(this.url + "/greet", requestBody);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    JsonArray responseBody = (JsonArray) parseResponse(connection);
                    updateServiceList(responseBody);
                    System.out.println("[Gossip] List updated: " + responseBody.toString());
                }
                else {
                    this.toRemove.add(this.url);
                }
            }
            catch (Exception ignored) {
                if (!this.toRemove.contains(this.url)) {
                    this.toRemove.add(this.url);
                }
            }
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
                }
                catch (Exception ignored) {}
            }
        }
    }
}

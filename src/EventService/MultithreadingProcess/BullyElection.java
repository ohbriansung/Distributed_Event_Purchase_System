package EventService.MultithreadingProcess;

import EventService.EventServiceDriver;
import EventService.Servlet.BaseServlet;
import Usage.State;
import com.google.gson.JsonObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class BullyElection extends BaseServlet implements Runnable {
    private volatile boolean beenReplied;

    public BullyElection() {
        this.beenReplied = false;
    }

    @Override
    public void run() {
        List<String> services = EventServiceDriver.eventServiceList.getList();
        int currentRank = services.indexOf(getCurrentAddress());

        // no other service rank higher, so current service get to be the new primary
        if (currentRank + 1 == services.size()) {
            announceNewPrimary();
        }
        else {
            do {
                List<Thread> currentTasts = new ArrayList<>();

                for (int i = currentRank + 1; i < services.size(); i++) {
                    System.out.println("[Election] Sending election request to " + services.get(i));
                    Thread newTask = new Thread(new Election(services.get(i)));
                    currentTasts.add(newTask);
                    newTask.start();
                }

                /*
                After all finishing tasks, check if there is any reply.
                If there is no reply, announce that a new primary has been elected.
                If there are replies, wait for announcement from service with higher rank,
                resend the election request when timeout and no announcement.
                 */
                try {
                    for (Thread task : currentTasts) {
                        task.join();
                    }

                    if (!this.beenReplied) {
                        announceNewPrimary();
                    }
                    else {
                        Thread.sleep(3000);
                    }
                }
                catch (InterruptedException ignored) {}

                // if received reply and still in candidate state after timeout, retry the election
            } while (EventServiceDriver.state == State.CANDIDATE);
        }
    }

    private void announceNewPrimary() {
        // change state
        System.out.println("[State] Change into primary state");
        EventServiceDriver.state = State.PRIMARY;
        EventServiceDriver.eventServiceList.setPrimary(getCurrentAddress());

        // start announcing to all services that "I am the new primary!"
        startAnnouncing("Event");
        startAnnouncing("FrontEnd");
    }

    private void startAnnouncing(String type) {
        List<String> services = (type.equals("Event")) ? EventServiceDriver.eventServiceList.getList() :
                EventServiceDriver.frontendServiceList.getList();

        for (String url : services) {
            if (!getCurrentAddress().equals(url)) {
                System.out.println("[Election] Sending announcement to " + type + " service on " + url);
                Thread newTask = new Thread(new Announce(url, type));
                newTask.start();
            }
        }
    }

    private class Election implements Runnable {
        private final String url;

        private Election(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                HttpURLConnection connection = doGetRequest(this.url + "/election");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    System.out.println("[Election] " + this.url + " has replied: there is a service with higher rank");
                    BullyElection.this.beenReplied = true;
                }
                else {
                    throw new Exception();
                }
            }
            catch (Exception ignored) {
                printRemove(this.url);
                EventServiceDriver.eventServiceList.removeService(this.url);
            }
        }
    }

    private class Announce implements Runnable {
        private final String url;
        private final String type;

        private Announce(String url, String type) {
            this.url = url;
            this.type = type;
        }

        @Override
        public void run() {
            try {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("port", EventServiceDriver.properties.get("port"));
                HttpURLConnection connection = doPostRequest(this.url + "/election", requestBody);

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new Exception();
                }
            }
            catch (Exception ignored) {
                printRemove(this.url);

                if (this.type.equals("Event")) {
                    EventServiceDriver.eventServiceList.removeService(this.url);
                }
                else {
                    EventServiceDriver.frontendServiceList.removeService(this.url);
                }
            }
        }
    }

    private void printRemove(String url) {
        System.out.println("[Election] Remove " + url + " from the list");
    }
}

package EventService.Usage;

import EventService.Concurrency.Event;
import EventService.EventServiceDriver;
import EventService.Servlet.BaseServlet;
import Usage.State;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.HttpURLConnection;
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
                for (int i = currentRank + 1; i < services.size(); i++) {
                    System.out.println("[Election] Sending election request to " + services.get(i));
                    Thread newTask = new Thread(new Election(services.get(i)));
                    newTask.start();
                }

                try {
                    Thread.sleep(3000);
                }
                catch (InterruptedException ignored) {}

                if (!this.beenReplied) {
                    announceNewPrimary();
                }

                // if received reply and still in candidate state after timeout, retry the election
            } while (EventServiceDriver.state == State.CANDIDATE);
        }
    }

    private void announceNewPrimary() {
        String currentAddress = getCurrentAddress();

        // change state
        EventServiceDriver.state = State.PRIMARY;
        EventServiceDriver.eventServiceList.setPrimary(currentAddress);

        // start announcing "I am new primary!"
        List<String> services = EventServiceDriver.eventServiceList.getList();

        for (String url : services) {
            if (!url.equals(currentAddress)) {
                System.out.println("[Election] Sending announce to " + url);
                Thread newTask = new Thread(new Announce(url));
                newTask.start();
            }
        }
    }

    private class Election implements Runnable {
        private String url;

        private Election(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                HttpURLConnection connection = doGetRequest(this.url + "/election");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    beenReplied = true;
                }
                else {
                    EventServiceDriver.eventServiceList.removeService(this.url);
                }
            }
            catch (IOException ignored) {
                EventServiceDriver.eventServiceList.removeService(this.url);
            }
        }
    }

    private class Announce implements Runnable {
        private String url;

        private Announce(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("port", EventServiceDriver.properties.get("port"));
                HttpURLConnection connection = doPostRequest(this.url + "/election", requestBody);

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    EventServiceDriver.eventServiceList.removeService(this.url);
                }
            }
            catch (IOException ignored) {
                EventServiceDriver.eventServiceList.removeService(this.url);
            }
        }
    }
}

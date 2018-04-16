package EventService.MultithreadingProcess;

import EventService.EventServiceDriver;
import EventService.Servlet.BaseServlet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.HttpURLConnection;

public class FullBackup extends BaseServlet {
    private final String address;

    public FullBackup(String address) {
        this.address = address;
    }

    public void startBackup() {
        System.out.println("[Backup] Starting backup to " + this.address);
        EventServiceDriver.eventList.lockForBackup();

        try {
            JsonObject requestBody = getData();
            HttpURLConnection connection = doPostRequest(this.address + "/backup", requestBody);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                System.out.println("[Backup] Finished backup to " + this.address);
            }
            else {
                throw new Exception();
            }
        }
        catch (Exception ignored) {
            ignored.printStackTrace();
            System.out.println("[Backup] Failed to backup to " + this.address + ", remove from the list");
            EventServiceDriver.eventServiceList.removeService(this.address);
        }
        finally {
            EventServiceDriver.eventList.unlockFromBackup();
        }
    }

    private JsonObject getData() {
        JsonObject data = new JsonObject();

        int timestamp = EventServiceDriver.lamportTimestamps.get();
        data.addProperty("timestamp", timestamp);

        JsonArray eventList = EventServiceDriver.eventList.toJsonArray();
        data.add("eventlist", eventList);

        JsonArray committedLog = EventServiceDriver.eventList.getCommittedLog();
        data.add("committedlog", committedLog);

        return data;
    }
}

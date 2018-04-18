package EventService.MultithreadingProcess;

import EventService.EventServiceDriver;
import EventService.Servlet.BaseServlet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.HttpURLConnection;
import java.net.URL;

public class FullBackup extends BaseServlet {

    public void startBackup(String address) {
        System.out.println("[Backup] Starting backup to " + address);
        EventServiceDriver.eventList.lockForBackup();

        try {
            JsonObject requestBody = getData();
            HttpURLConnection connection = doPostRequest(address + "/backup", requestBody);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                System.out.println("[Backup] Finished backup to " + address);
            }
            else {
                throw new Exception();
            }
        }
        catch (Exception ignored) {
            ignored.printStackTrace();
            System.out.println("[Backup] Failed to backup to " + address + ", remove from the list");
            EventServiceDriver.eventServiceList.removeService(address);
        }
        finally {
            EventServiceDriver.eventList.unlockFromBackup();
        }
    }

    public JsonObject getData() {
        JsonObject data = new JsonObject();

        int timestamp = EventServiceDriver.lamportTimestamps.get();
        data.addProperty("timestamp", timestamp);

        JsonArray eventList = EventServiceDriver.eventList.toJsonArray();
        data.add("eventlist", eventList);

        JsonArray committedLog = EventServiceDriver.eventList.getCommittedLog();
        data.add("committedlog", committedLog);

        return data;
    }

    public void requestForBackup(boolean lock) {
        try {
            String url = EventServiceDriver.eventServiceList.getPrimary() + "/backup";
            HttpURLConnection connection = doGetRequest(url);
            JsonObject responseBody = (JsonObject) parseResponse(connection);
            EventServiceDriver.eventList.restoreData(responseBody, lock);
            System.out.println("[Backup] Data has been restored");
        }
        catch (Exception ignored) {
            System.out.println("[Backup] Failed to restore data");
        }
    }
}

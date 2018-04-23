package EventService.MultithreadingProcess;

import EventService.EventServiceDriver;
import EventService.Servlet.BaseServlet;

import java.net.HttpURLConnection;
import java.util.List;

/**
 * GreetWithFrontEnd class to keep greeting with frontend services.
 */
public class GreetWithFrontEnd extends BaseServlet implements Runnable {

    /**
     * run method to start the operation.
     */
    @Override
    public void run() {
        while (EventServiceDriver.alive) {
            try {
                List<String> services = EventServiceDriver.frontendServiceList.getList();

                for (String url : services) {
                    greetAndUpdate(url);
                }

                Thread.sleep(500);
            }
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    /**
     * Send the greet request. If the service is no longer there, remove it from the list.
     *
     * @param url
     */
    private void greetAndUpdate(String url) {
        try {
            HttpURLConnection connection = doGetRequest(url + "/greet");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception();
            }
        }
        catch (Exception ignored) {
            System.out.println("[Greet] Remove frontend " + url + " from the list");
            EventServiceDriver.frontendServiceList.removeService(url);
        }
    }
}

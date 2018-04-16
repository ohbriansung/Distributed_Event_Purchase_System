package EventService.MultithreadingProcess;

import EventService.EventServiceDriver;
import EventService.Servlet.BaseServlet;

import java.net.HttpURLConnection;
import java.util.List;

public class GreetWithFrontEnd extends BaseServlet implements Runnable {

    @Override
    public void run() {
        while (EventServiceDriver.alive) {
            try {
                List<String> services = EventServiceDriver.frontendServiceList.getList();

                for (String url : services) {
                    System.out.println("[Greet] Greet with frontend on " + url);
                    greetAndUpdate(url);
                }

                Thread.sleep(10000);
            }
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    private void greetAndUpdate(String url) {
        try {
            HttpURLConnection connection = doGetRequest(url + "/greet");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception();
            }
        }
        catch (Exception ignored) {
            System.out.println("[Greet] Remove frontend on " + url);
            EventServiceDriver.frontendServiceList.removeService(url);
        }
    }
}

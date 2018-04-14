package EventService.Usage;

import EventService.EventServiceDriver;
import EventService.Servlet.BaseServlet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

public class GreetWithFrontEnd extends BaseServlet implements Runnable {

    public GreetWithFrontEnd() {}

    @Override
    public void run() {
        while (EventServiceDriver.alive) {
            try {
                List<String> services = EventServiceDriver.frontendServiceList.getList();

                for (String url : services) {
                    greetAndUpdate(url);
                }

                Thread.sleep(10000);
            }
            catch (InterruptedException ie) {
                System.err.println(ie);
            }
        }
    }

    private void greetAndUpdate(String url) {
        try {
            HttpURLConnection connection = doGetRequest(url + "/greet");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                EventServiceDriver.frontendServiceList.removeService(url);
            }
        }
        catch (IOException ignored) {
            EventServiceDriver.frontendServiceList.removeService(url);
        }
    }
}

package Usage;

import com.google.gson.JsonObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ConcurrentTest {

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.println("Usage: java -cp project4.jar Usage.ConcurrentTest <address> <times_of_test>");
            return;
        }

        String address = args[0];
        int times = Integer.parseInt(args[1]);
        List<Thread> list = new ArrayList<>();

        for (int i = 0; i < times; i++) {
            list.add(new Thread(new ConcurrentTest.Request(address)));
        }

        for (Thread thread : list) {
            thread.start();
        }

        for (Thread thread : list) {
            thread.join();
        }

        System.out.println("[Test] Concurrency test finished");
    }

    private static class Request implements Runnable {
        private JsonObject body;
        private HttpURLConnection connection;

        Request(String address) throws Exception {
            this.body = new JsonObject();
            this.body.addProperty("userid", 2294);
            this.body.addProperty("eventname", "Concurrency test");
            this.body.addProperty("numtickets", 10);

            URL url = new URL("http://" + address + "/events/create");
            this.connection = (HttpURLConnection) url.openConnection();
            this.connection.setRequestMethod("POST");
            this.connection.setRequestProperty("Content-Type", "application/json");
            this.connection.setDoOutput(true);
        }

        @Override
        public void run() {
            try {
                OutputStream os = this.connection.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();
                os.close();
                this.connection.getResponseCode();
            }
            catch (Exception ignored) {}
        }
    }
}

package Usage;

import com.google.gson.JsonObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ConcurrentTest {

    public static void main(String[] args) throws Exception {
        List<Thread> list = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            list.add(new Thread(new ConcurrentTest.Request()));
        }

        for (Thread thread : list) {
            thread.start();
        }

        for (Thread thread : list) {
            thread.join();
        }
    }

    private static class Request implements Runnable {
        private JsonObject body;
        private HttpURLConnection connection;

        public Request() throws Exception {
            this.body = new JsonObject();
            this.body.addProperty("userid", 2294);
            this.body.addProperty("eventname", "Distributed Software Development");
            this.body.addProperty("numtickets", 20);

            URL url = new URL("http://localhost:4560/events/create");
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

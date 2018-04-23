package Usage;

import com.google.gson.JsonObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ConcurrentTest class for concurrent writes.
 */
public class ConcurrentTest {

    /**
     * main method to start the test.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            System.out.println("Usage: java -cp project4.jar Usage.ConcurrentTest <test_number 0: create, 1:purchase> <address> <times_of_test>");
            return;
        }

        int testNo = Integer.parseInt(args[0]);
        String address = args[1];
        int times = Integer.parseInt(args[2]);
        List<Thread> list = new ArrayList<>();

        for (int i = 0; i < times; i++) {
            if (testNo == 0) {
                list.add(new Thread(new ConcurrentTest.Request(address)));
            }
            else if (testNo == 1) {
                list.add(new Thread(new ConcurrentTest.Request(address, testNo)));
            }
        }

        for (Thread thread : list) {
            thread.start();
        }

        for (Thread thread : list) {
            thread.join();
        }

        System.out.println("[Test] Concurrency test finished");
    }

    /**
     * Request class implement Runnable for supporting currency.
     */
    private static class Request implements Runnable {
        private JsonObject body;
        private HttpURLConnection connection;

        /**
         * Overlapped constructor of Request for create.
         *
         * @param address
         * @throws Exception
         */
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

        /**
         * Overlapped constructor of Request for purchase.
         *
         * @param address
         * @param i
         * @throws Exception
         */
        Request(String address, int i) throws Exception {
            this.body = new JsonObject();
            this.body.addProperty("tickets", 2);

            URL url = new URL("http://" + address + "/events/" + i + "/purchase/2294");
            this.connection = (HttpURLConnection) url.openConnection();
            this.connection.setRequestMethod("POST");
            this.connection.setRequestProperty("Content-Type", "application/json");
            this.connection.setDoOutput(true);
        }

        /**
         * run method to start the operation.
         */
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

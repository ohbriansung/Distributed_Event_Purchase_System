package FrontEndService;

import Concurrency.BlockingThreads;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import java.net.InetAddress;
import java.util.*;

/**
 * FrontEndServiceDriver class for starting the Front End Service.
 */
public class FrontEndServiceDriver {
    public static final String APP_TYPE = "application/json";
    static volatile boolean alive = true;
    static Map<String, String> properties;

    static BlockingThreads blockingThreads;
    static volatile String primaryEventService;
    static volatile String primaryUserService;

    /**
     * main method to start the server.
     *
     * @param args
     *      - current port and address of primaries
     */
    public static void main(String[] args) {
        FrontEndServiceDriver.properties = new HashMap<>();
        FrontEndServiceDriver.blockingThreads = new BlockingThreads();

        try {
            FrontEndServiceDriver.initProperties(args);
            FrontEndServiceDriver.startServer();
        }
        catch (Exception ex) {
            FrontEndServiceDriver.alive = false;
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Parse arguments and store the data.
     *
     * @param args
     * @throws Exception
     */
    private static void initProperties(String[] args) throws Exception {
        boolean port = false;
        boolean primaryEvent = false;
        boolean primaryUser = false;

        String currentHost =  InetAddress.getLocalHost().getHostAddress();
        FrontEndServiceDriver.properties.put("host", currentHost);

        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "-port":
                    FrontEndServiceDriver.properties.put("port", args[i + 1]);
                    port = true;
                    break;
                case "-primaryEvent":
                    FrontEndServiceDriver.primaryEventService = args[i + 1];
                    primaryEvent = true;
                    break;
                case "-primaryUser":
                    FrontEndServiceDriver.primaryUserService = args[i + 1];
                    primaryUser = true;
                    break;
            }
        }

        if (!port || !primaryEvent || !primaryUser) {
            throw new Exception("Lack of parameter: port, primaryEvent, or primaryUser");
        }
    }

    /**
     * Start the server to listen and greet with primary event.
     *
     * @throws Exception
     */
    private static void startServer() throws Exception {
        int port = Integer.parseInt(FrontEndServiceDriver.properties.get("port"));
        Server server = new Server(port);
        ServletHandler servHandler = new ServletHandler();

        servHandler.addServletWithMapping(EventListServlet.class, "/events");
        servHandler.addServletWithMapping(EventServlet.class, "/events/*");
        servHandler.addServletWithMapping(EventCreateServlet.class, "/events/create");
        servHandler.addServletWithMapping(UserServlet.class, "/users/*");
        servHandler.addServletWithMapping(UserCreateServlet.class, "/users/create");
        servHandler.addServletWithMapping(GreetServlet.class, "/greet");
        servHandler.addServletWithMapping(ElectionServlet.class, "/election");
        server.setHandler(servHandler);

        Thread gossipThread = new Thread(new Gossip());

        System.out.println("[System] Starting frontend service on " + FrontEndServiceDriver.properties.get("host") +
                ":" + FrontEndServiceDriver.properties.get("port"));

        server.start();
        gossipThread.start();
        server.join();
    }
}

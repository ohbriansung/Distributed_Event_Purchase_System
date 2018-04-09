package EventService;

import Concurrency.ServiceList;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import java.net.InetAddress;
import java.util.*;

/**
 * EventServiceDriver class for starting the Event Service.
 */
public class EventServiceDriver {
    static final String APP_TYPE = "application/json";
    static boolean alive = true;
    static EventList eventList;
    static Map<String, String> properties;
    static ServiceList<String> eventServiceList;
    static ServiceList<String> userServiceList;

    /**
     * main method to start the server.
     *
     * @param args
     */
    public static void main(String[] args) {
        EventServiceDriver.eventList = new EventList();
        EventServiceDriver.properties = new HashMap<>();
        EventServiceDriver.eventServiceList = new ServiceList<>();
        EventServiceDriver.userServiceList = new ServiceList<>();

        try {
            EventServiceDriver.initProperties(args);
            EventServiceDriver.startServer();
        }
        catch (Exception ex) {
            EventServiceDriver.alive = false;
            System.err.println(ex);
            System.exit(-1);
        }
    }

    private static void initProperties(String[] args) throws Exception {
        boolean port = false;
        boolean primaryE = false;
        boolean primaryU = false;

        String currentHost =  InetAddress.getLocalHost().getHostAddress();
        EventServiceDriver.properties.put("host", currentHost);

        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("-port")) {
                EventServiceDriver.properties.put("port", args[i + 1]);
                EventServiceDriver.eventServiceList.addService(
                        currentHost + ":" + EventServiceDriver.properties.get("port"));
                port = true;
            }
            else if (args[i].equals("-primaryEvent")) {
                if (args[i + 1].equals("this")) {
                    EventServiceDriver.eventServiceList.setPrimary(
                            currentHost + ":" + EventServiceDriver.properties.get("port"));
                }
                else {
                    EventServiceDriver.eventServiceList.setPrimary(args[i + 1]);
                    EventServiceDriver.eventServiceList.addService(args[i + 1]);
                }
                primaryE = true;
            }
            else if (args[i].equals("-primaryUser")) {
                EventServiceDriver.userServiceList.setPrimary(args[i + 1]);
                primaryU = true;
            }
        }

        EventServiceDriver.properties.put("port", "4599");
        EventServiceDriver.eventServiceList.addService(currentHost + ":4599");
        port = true;
        EventServiceDriver.eventServiceList.setPrimary(
                currentHost + ":" + EventServiceDriver.properties.get("port"));
        primaryE = true;
        primaryU = true;

        if (!port || !primaryE || !primaryU) {
            throw new Exception("Lack of parameter: port, primaryEvent, or primaryUser.");
        }
    }

    private static void startServer() throws Exception {
        int port = Integer.parseInt(EventServiceDriver.properties.get("port"));
        Server server = new Server(port);
        ServletHandler servHandler = new ServletHandler();

        servHandler.addServletWithMapping(CreateServlet.class, "/create");
        servHandler.addServletWithMapping(ListServlet.class, "/list");
        servHandler.addServletWithMapping(EventServlet.class, "/*");
        servHandler.addServletWithMapping(PurchaseServlet.class, "/purchase/*");
        servHandler.addServletWithMapping(GreetServlet.class, "/greet");
        server.setHandler(servHandler);

        Thread gossipThread = new Thread(new Gossip());

        server.start();
        gossipThread.start();
        server.join();
    }
}

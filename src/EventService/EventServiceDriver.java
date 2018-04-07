package EventService;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

/**
 * EventServiceDriver class for starting the Event Service.
 */
public class EventServiceDriver {
    static final String appType = "application/json";
    static EventList eventList;
    static Map<String, String> properties;
    static List<String> eventServiceList;
    static String primaryEvent;
    static List<String> userServiceList;
    static String primaryUser;

    /**
     * main method to start the server.
     *
     * @param args
     */
    public static void main(String[] args) {
        EventServiceDriver.eventList = new EventList();
        EventServiceDriver.properties = new HashMap<>();
        EventServiceDriver.eventServiceList = new ArrayList<>();
        EventServiceDriver.userServiceList = new ArrayList<>();

        try {
            EventServiceDriver.initProperties(args);

            int port = Integer.parseInt(EventServiceDriver.properties.get("port"));
            Server server = new Server(port);
            ServletHandler servHandler = new ServletHandler();

            servHandler.addServletWithMapping(CreateServlet.class, "/create");
            servHandler.addServletWithMapping(ListServlet.class, "/list");
            servHandler.addServletWithMapping(EventServlet.class, "/*");
            servHandler.addServletWithMapping(PurchaseServlet.class, "/purchase/*");
            server.setHandler(servHandler);

            server.start();
            server.join();
        }
        catch (Exception ex) {
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
                port = true;
            }
            else if (args[i].equals("-primaryEvent")) {
                if (args[i + 1].equals("this")) {
                    EventServiceDriver.primaryEvent = currentHost + ":" + EventServiceDriver.properties.get("port");
                }
                else {
                    EventServiceDriver.primaryEvent = args[i + 1];
                }
                primaryE = true;
            }
            else if (args[i].equals("-primaryUser")) {
                EventServiceDriver.primaryUser = args[i + 1];
                primaryU = true;
            }
        }

        if (!port || !primaryE || !primaryU) {
            throw new Exception("Lack of parameter: port, primaryEvent, or primaryUser.");
        }
    }
}

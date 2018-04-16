package EventService;

import Concurrency.ConcurrentInteger;
import Concurrency.ServiceList;
import EventService.MultithreadingProcess.GreetWithFrontEnd;
import Usage.ServiceName;
import EventService.EventConcurrency.EventList;
import EventService.Servlet.*;
import EventService.MultithreadingProcess.Gossip;
import Usage.State;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import java.net.InetAddress;
import java.util.*;

/**
 * EventServiceDriver class for starting the Event Service.
 */
public class EventServiceDriver {
    public static final String APP_TYPE = "application/json";
    public static volatile boolean alive = true;
    public static EventList eventList;
    public static Map<String, String> properties;

    public static ServiceList<String> frontendServiceList;
    public static ServiceList<String> eventServiceList;
    public static volatile String primaryUserService;
    public static volatile State state;
    public static ConcurrentInteger lamportTimestamps;

    /**
     * main method to start the server.
     *
     * @param args
     *      - address of primaries
     */
    public static void main(String[] args) {
        initDataStructures();

        try {
            EventServiceDriver.initProperties(args);
            EventServiceDriver.startServer();
        }
        catch (Exception ex) {
            EventServiceDriver.alive = false;
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    private static void initDataStructures() {
        EventServiceDriver.eventList = new EventList();
        EventServiceDriver.properties = new HashMap<>();
        EventServiceDriver.frontendServiceList = new ServiceList<>(ServiceName.FRONT_END.toString());
        EventServiceDriver.eventServiceList = new ServiceList<>(ServiceName.EVENT.toString());
        EventServiceDriver.lamportTimestamps = new ConcurrentInteger();
    }

    private static void initProperties(String[] args) throws Exception {
        boolean port = false;
        boolean primaryEvent = false;
        boolean primaryUser = false;

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
                    EventServiceDriver.state = State.PRIMARY;
                }
                else {
                    EventServiceDriver.eventServiceList.setPrimary(args[i + 1]);
                    EventServiceDriver.eventServiceList.addService(args[i + 1]);
                    EventServiceDriver.state = State.SECONDARY;
                }
                primaryEvent = true;
            }
            else if (args[i].equals("-primaryUser")) {
                EventServiceDriver.primaryUserService = args[i + 1];
                primaryUser = true;
            }
        }

        // TODO: delete before deploy
        EventServiceDriver.properties.put("port", "4599");
        EventServiceDriver.eventServiceList.addService(currentHost + ":4599");
        EventServiceDriver.eventServiceList.addService(currentHost + ":4599");
        port = true;
        EventServiceDriver.eventServiceList.setPrimary(currentHost + ":4599");
        EventServiceDriver.state = State.PRIMARY;
        //EventServiceDriver.state = State.SECONDARY;
        primaryEvent = true;
        EventServiceDriver.primaryUserService = "localhost:4552";
        primaryUser = true;
        // TODO: delete before deploy

        if (!port || !primaryEvent || !primaryUser) {
            throw new Exception("Lack of parameter: port, primaryEvent, or primaryUser");
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
        servHandler.addServletWithMapping(GreetServlet.class, "/greet/*");
        servHandler.addServletWithMapping(ElectionServlet.class, "/election");
        servHandler.addServletWithMapping(BackupServlet.class, "/backup");
        server.setHandler(servHandler);

        Thread gossipThread = new Thread(new Gossip());
        Thread greetFrontEnd = new Thread(new GreetWithFrontEnd());

        System.out.println("[System] Starting event service on " + EventServiceDriver.properties.get("host") +
                ":" + EventServiceDriver.properties.get("port"));

        server.start();
        gossipThread.start();
        greetFrontEnd.start();
        server.join();
    }
}

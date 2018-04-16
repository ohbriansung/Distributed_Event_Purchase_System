package FrontEndService;

import FrontEndService.MultithreadingProcess.Gossip;
import FrontEndService.Servlet.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import java.net.InetAddress;
import java.util.*;

/**
 * FrontEndServiceDriver class for starting the Front End Service.
 */
public class FrontEndServiceDriver {
    public static final String APP_TYPE = "application/json";
    public static volatile boolean alive = true;
    public static Map<String, String> properties;

    public static volatile String primaryEventService;
    public static volatile String primaryUserService;

    /**
     * main method to start the server.
     *
     * @param args
     *      - current port and address of primaries
     */
    public static void main(String[] args) {
        FrontEndServiceDriver.properties = new HashMap<>();

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
                    FrontEndServiceDriver.primaryUserService = args[i + 1];
                    primaryEvent = true;
                    break;
                case "-primaryUser":
                    FrontEndServiceDriver.primaryUserService = args[i + 1];
                    primaryUser = true;
                    break;
            }
        }

        // TODO: delete before deploy
        FrontEndServiceDriver.properties.put("port", "4599");
        port = true;
        FrontEndServiceDriver.primaryUserService = "localhost:4599";
        primaryEvent = true;
        FrontEndServiceDriver.primaryUserService = "localhost:4552";
        primaryUser = true;
        // TODO: delete before deploy

        if (!port || !primaryEvent || !primaryUser) {
            throw new Exception("Lack of parameter: port, primaryEvent, or primaryUser");
        }
    }

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
        server.setHandler(servHandler);

        Thread gossipThread = new Thread(new Gossip());

        System.out.println("[System] Starting frontend service on " + FrontEndServiceDriver.properties.get("host") +
                ":" + FrontEndServiceDriver.properties.get("port"));

        server.start();
        gossipThread.start();
        server.join();
    }
}

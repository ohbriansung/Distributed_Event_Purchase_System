package UserService;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * UserServiceDriver class for starting the User Service.
 */
public class UserServiceDriver {
    static final String appType = "application/json";
    static UserList userList;
    static Map<String, String> hosts;

    /**
     * main method to start the server.
     *
     * @param args
     */
    public static void main(String[] args) {
        UserServiceDriver.userList = new UserList();
        UserServiceDriver.hosts = new HashMap<>();
        UserServiceDriver.loadConfig();

        int port = Integer.parseInt(UserServiceDriver.hosts.get("myUserServicePort"));
        Server server = new Server(port);
        ServletHandler servHandler = new ServletHandler();

        servHandler.addServletWithMapping(new ServletHolder(new CreateServlet()), "/create");
        servHandler.addServletWithMapping(new ServletHolder(new UserServlet()), "/*");

        try {
            server.setHandler(servHandler);
            server.start();
            server.join();
        }
        catch (Exception ex) {
            System.err.println(ex);
            System.exit(-1);
        }
    }

    /**
     * Load host and port information from config file.
     */
    private static void loadConfig() {
        String fileName = "services.properties";

        Set<String> required = new HashSet<>();
        required.add("myUserServiceHost");
        required.add("myUserServicePort");

        Properties config = new Properties();
        try {
            config.load(new FileInputStream(fileName));
        }
        catch (IOException ignored) {
            System.err.println("Cannot find/load " + fileName + " file.");
            System.exit(-1);
        }

        if (!config.keySet().containsAll(required)) {
            System.err.println("Must provide the following in properties file: " + required);
            System.exit(-1);
        }

        for (String property : required) {
            UserServiceDriver.hosts.put(property, config.getProperty(property));
        }
    }
}

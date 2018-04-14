package EventService.Servlet;

import EventService.EventServiceDriver;
import com.google.gson.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Abstract BaseServlet class to extends HttpServlet
 * and to contain commonly used methods.
 */
public abstract class BaseServlet extends HttpServlet {

    /**
     * Initialize a new HttpURLConnection for particular service.
     *
     * @param urlSring
     * @return HttpURLConnection
     * @throws IOException
     */
    private HttpURLConnection initConnection(String urlSring) throws IOException {
        URL url = new URL("http://" + urlSring);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);

        return connection;
    }

    /**
     * Parse the body of POST request.
     *
     * @param request
     * @return String
     * @throws IOException
     */
    public String parseRequest(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String str;
        while ((str = reader.readLine()) != null) {
            sb.append(str).append(System.lineSeparator());
        }

        return sb.substring(sb.indexOf("{"));
    }

    /**
     * Convert string into JsonElement.
     *
     * @param body
     * @return JsonElement
     * @throws JsonParseException
     */
    public JsonElement parseJson(String body) throws JsonParseException {
        JsonParser parser = new JsonParser();
        return parser.parse(body);
    }

    /**
     * Send a GET request using HttpURLConnection.
     *
     * @param url
     * @return HttpURLConnection
     * @throws IOException
     */
    public HttpURLConnection doGetRequest(String url) throws IOException {
        HttpURLConnection connection = initConnection(url);
        connection.setRequestMethod("GET");

        return connection;
    }

    /**
     * Send a POST request using HttpURLConnection.
     *
     * @param url
     * @param body
     * @return HttpURLConnection
     * @throws IOException
     */
    public HttpURLConnection doPostRequest(String url, JsonObject body) throws IOException {
        HttpURLConnection connection = initConnection(url);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", EventServiceDriver.APP_TYPE);
        connection.setDoOutput(true);

        OutputStream os = connection.getOutputStream();
        os.write(body.toString().getBytes());
        os.flush();
        os.close();

        return connection;
    }

    /**
     * Parse the response of HttpURLConnection into JSON format.
     *
     * @param connection
     * @return JsonElement
     * @throws JsonParseException
     * @throws IOException
     */
    public JsonElement parseResponse(HttpURLConnection connection) throws JsonParseException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();

        String str;
        while ((str = in.readLine()) != null) {
            sb.append(str);
        }
        in.close();

        return parseJson(sb.toString());
    }

    public String getCurrentAddress() {
        return EventServiceDriver.properties.get("host") +
                ":" + EventServiceDriver.properties.get("port");
    }

    public JsonArray getServiceList() {
        JsonArray array = EventServiceDriver.frontendServiceList.getData();
        array.addAll(EventServiceDriver.eventServiceList.getData());

        return array;
    }
}

package EventService;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;

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
     * @param serviceType
     * @param uri
     * @return HttpURLConnection
     * @throws IOException
     */
    private HttpURLConnection initConnection(String serviceType, String uri) throws IOException {
        URL url = new URL("http://" + EventServiceDriver.primaryEvent + uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        return connection;
    }

    /**
     * Parse the body of POST request.
     *
     * @param request
     * @return String
     * @throws IOException
     */
    String parseRequest(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String str;
        while ((str = reader.readLine()) != null) {
            sb.append(str).append(System.lineSeparator());
        }

        return sb.substring(sb.indexOf("{"));
    }

    /**
     * Convert string into JsonObject.
     *
     * @param body
     * @return JsonObject
     * @throws JsonParseException
     */
    JsonObject parseJson(String body) throws JsonParseException {
        JsonParser parser = new JsonParser();
        return (JsonObject) parser.parse(body);
    }

    /**
     * Send a POST request using HttpURLConnection.
     *
     * @param service
     * @param uri
     * @param body
     * @return HttpURLConnection
     * @throws IOException
     */
    HttpURLConnection doPostRequest(String service, String uri, JsonObject body) throws IOException {
        HttpURLConnection connection = initConnection(service, uri);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", EventServiceDriver.appType);
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
     * @return JsonObject
     * @throws JsonParseException
     * @throws IOException
     */
    JsonObject parseResponse(HttpURLConnection connection) throws JsonParseException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();

        String str;
        while ((str = in.readLine()) != null) {
            sb.append(str);
        }
        in.close();

        return parseJson(sb.toString());
    }
}

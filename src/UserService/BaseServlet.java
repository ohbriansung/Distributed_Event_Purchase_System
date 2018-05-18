package UserService;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Abstract BaseServlet class to extends HttpServlet
 * and to contain commonly used methods.
 */
public abstract class BaseServlet extends HttpServlet {

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
     * Convert string into JSONObject.
     *
     * @param body
     * @return JsonObject
     * @throws JsonParseException
     */
    JsonObject parseJSON(String body) throws JsonParseException {
        JsonParser parser = new JsonParser();
        return parser.parse(body).getAsJsonObject();
    }
}

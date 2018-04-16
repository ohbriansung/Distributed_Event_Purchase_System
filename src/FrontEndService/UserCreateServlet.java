package FrontEndService;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

/**
 * UserCreateServlet class to handle request for creating user.
 */
public class UserCreateServlet extends BaseServlet {

    /**
     * doPost method to send a POST request to User Service to create a user.
     *
     * @param request
     * @param response
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("request: POST " + request.getRequestURI());

        response.setContentType(FrontEndServiceDriver.APP_TYPE);
        response.setStatus(400);

        try {
            String url = FrontEndServiceDriver.primaryUserService +
                    request.getRequestURI().replaceFirst("/users", "");
            String requestBody = parseRequest(request);
            JsonObject body = (JsonObject) parseJson(requestBody);
            HttpURLConnection connection = doPostRequest(url, body);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                PrintWriter pw = response.getWriter();
                JsonObject responseBody = (JsonObject) parseResponse(connection);

                response.setStatus(200);
                pw.println(responseBody.toString());
            }
        }
        catch (Exception ignored) {}
    }
}

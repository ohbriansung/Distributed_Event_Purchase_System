package FrontEndService.Servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;

public class GreetServlet extends BaseServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("[Servlet] GET request /greet");
        response.setStatus(HttpURLConnection.HTTP_OK);
    }
}

package FrontEndService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;

/**
 * GreetServlet to handle the greet request.
 */
public class GreetServlet extends BaseServlet {

    /**
     * Reply with HTTP 200 to maintain the membership.
     *
     * @param request
     * @param response
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpURLConnection.HTTP_OK);
    }
}

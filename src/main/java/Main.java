import org.eclipse.jetty.http.HttpStatus;
import spark.Spark;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class Main {
    public static void main(String[] args) {

        // Configure Spark to listen on a specific port
        Spark.port(3000); // Replace with your desired port

        // Define a route to handle incoming webhook events
        Spark.post("/", (request, response) -> {
            HttpServletRequest httpServletRequest = new HttpServletRequestWrapper(request.raw());
            HttpServletResponse httpServletResponse = new HttpServletResponseWrapper(response.raw());

            //Setting the default response
            httpServletResponse.setHeader("Custom-Message", "Webhook received successfully");
            httpServletResponse.setStatus(HttpStatus.OK_200);

            String payload = request.body();
            GitHubWebhookHandler webhookHandler = new GitHubWebhookHandler();

            webhookHandler.handleWebhookIssueEvent(httpServletRequest,httpServletResponse, payload);

            //Taking response and custom message from HttpResponse  and return it to user
            response.status(httpServletResponse.getStatus());
            String customMessage = httpServletResponse.getHeader("Custom-Message");
            return customMessage;
        });
    }

}

import spark.Request;
import spark.Spark;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class Main {
    public static void main(String[] args) {
        // Configure Spark to listen on a specific port
        Spark.port(3000); // Replace with your desired port

        // Define a route to handle incoming webhook events
        Spark.post("/webhook-endpoint", (req, res) -> {
            Request request = (Request) req;
            HttpServletRequest httpServletRequest = new HttpServletRequestWrapper(request.raw());

            String payload = req.body();
            GitHubWebhookHandler webhookHandler = new GitHubWebhookHandler( System.getenv("GITHUB_TOKEN"));
            webhookHandler.handleWebhookEvent(httpServletRequest,payload);

            // Respond with a 200 OK status to acknowledge receipt
            res.status(200);
            return "Webhook received successfully";
        });
    }

}

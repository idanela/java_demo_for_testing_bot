import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GitHub;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class GitHubWebhookHandler {

    private final GitHub github;

    public GitHubWebhookHandler(String accessToken) throws IOException {
        github = GitHub.connectUsingOAuth(accessToken);
    }

    public  void handleWebhookEvent(HttpServletRequest request, String payloadJson) {
        try {
            // Check the X-GitHub-Event header to determine the event type
            String eventType = request.getHeader("X-GitHub-Event");
            if (eventType == null || !verifySignature(payloadJson, eventType)) {
                // The signature doesn't match, so reject the request
                System.out.println("Webhook verification failed.");
                return;
            }

            if ("issues".equals(eventType)) {
                // Parse the webhook payload into a GHEventPayload
                StringReader reader = new StringReader(payloadJson);
                GHEventPayload payload = github.parseEventPayload(reader, GHEventPayload.class);

                // Extract information from the payload
                String issueTitle = ((GHEventPayload.Issue)payload).getIssue().getTitle();
                String issueBody = ((GHEventPayload.Issue)payload).getIssue().getBody();
                ((GHEventPayload.Issue)payload).getIssue().comment("hi" + payload.getSender());

                System.out.println( payload.getRepository().getName());
                // Use the GitHub API for Java to create an issue or perform actions
                //github.getRepository(payload.getRepository().getName()).createIssue(issueTitle).body(issueBody).create();

                // Respond to the webhook event
                // You can send an HTTP response back to GitHub to acknowledge the event.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        public boolean verifySignature(String payload, String signature) {
            try {
                String algorithm = "HmacSHA1";
                Mac mac = Mac.getInstance(algorithm);
                SecretKeySpec secretKeySpec = new SecretKeySpec(System.getenv("WEBHOOK_SECRET").getBytes(), algorithm);
                mac.init(secretKeySpec);

                byte[] rawHmac = mac.doFinal(payload.getBytes());

                StringBuilder hexString = new StringBuilder();
                for (byte b : rawHmac) {
                    hexString.append(String.format("%02x", b));
                }

                return hexString.toString().equals(signature);
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                e.printStackTrace();
                return false;
            }
        }
}


import io.github.cdimascio.dotenv.Dotenv;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GitHub;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;


public class GitHubWebhookHandler {

    private final GitHub github;
    private final Dotenv dotenv;

    private static final String GITHUB_EVENT = "X-GitHub-Event";
    private static final String REQUEST_SIGNATURE = "x-hub-signature-256";
    private static final String GITHUB_ACCESS_TOKEN = "GITHUB_TOKEN";
    private static final String WEBHOOK_SECRET = "WEBHOOK_SECRET";


    public GitHubWebhookHandler() throws IOException {
        dotenv = Dotenv.load();
        github = GitHub.connectUsingOAuth(dotenv.get(GITHUB_ACCESS_TOKEN));
    }

    public void handleWebhookEvent(HttpServletRequest request, HttpServletResponse response, String payloadJson) {
        try {
            String eventType = request.getHeader(GITHUB_EVENT);
            String signature = request.getHeader(REQUEST_SIGNATURE);
            if (eventType == null || !GithubUtils.verifySignature(payloadJson, signature, dotenv.get(WEBHOOK_SECRET))) {
                response.setHeader("Custom-Message", "Webhook verification failed.");
                response.setStatus(401);
                return;
            }

            if (GithubUtils.isGithubIssue(eventType)) {
                // Parse the webhook payload into an Issue object
                StringReader reader = new StringReader(payloadJson);
                GHEventPayload.Issue payload = github.parseEventPayload(reader, GHEventPayload.Issue.class);
                if (GithubUtils.isIssueOpened(payload.getAction()))
                    GithubUtils.postCommentOnIssue(payload.getIssue(), payload.getSender().getLogin());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}


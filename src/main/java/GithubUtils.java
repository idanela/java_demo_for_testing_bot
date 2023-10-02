import org.apache.commons.codec.binary.Hex;
import org.kohsuke.github.GHIssue;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class GithubUtils {
    private static final String THANKS_FOR_ISSUE_MESSAGE_TEMPLATE = "Thanks for the issue report @%s! We will look into it as soon as possible.";


    public static boolean isGithubIssue(String eventType) {
        return "issues".equals(eventType);
    }

    public static boolean isIssueOpened(String action) {
        return "opened".equals(action);
    }

    public static void postCommentOnIssue(GHIssue issue, String userName) {
        try {
            issue.comment(String.format(THANKS_FOR_ISSUE_MESSAGE_TEMPLATE, userName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean verifySignature(String payload, String signature, String webhookSecret) {
        try {
            signature = signature.replace("sha256=", "");
            String algorithm = "HmacSHA256";
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), algorithm);
            mac.init(secretKeySpec);

            byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = Hex.encodeHexString(rawHmac);


            return calculatedSignature.equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
    }

}

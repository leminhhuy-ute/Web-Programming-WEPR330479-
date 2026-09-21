package topicmanagement.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

@Component
public class LegacyPasswordVerifier {
    public boolean verifyLegacyHash(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) return false;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equalsIgnoreCase(storedHash);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }
}

package topicmanagement.security;

import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;

/**
 * Transitional verification for existing PBKDF2 seed rows; successful logins are upgraded to
 * BCrypt.
 */
public final class LegacyPasswordVerifier {
  private LegacyPasswordVerifier() {}

  public static boolean matches(String password, String hash) {
    try {
      String[] p = hash.split("\\$");
      if (p.length != 4 || !p[0].equals("pbkdf2_sha256")) return false;
      int n = Integer.parseInt(p[1]);
      if (n < 100000 || n > 2000000) return false;
      byte[] salt = Base64.getDecoder().decode(p[2]);
      PBEKeySpec s = new PBEKeySpec(password.toCharArray(), salt, n, 256);
      byte[] actual =
          SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(s).getEncoded();
      s.clearPassword();
      return MessageDigest.isEqual(actual, Base64.getDecoder().decode(p[3]));
    } catch (Exception e) {
      return false;
    }
  }
}

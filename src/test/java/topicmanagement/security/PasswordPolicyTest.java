package topicmanagement.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordPolicyTest {
  @Test
  void acceptsStrongPasswordAndRejectsEveryWeakVariant() {
    assertTrue(PasswordPolicy.isStrong("Strong@123"));
    assertFalse(PasswordPolicy.isStrong("short1!A".substring(0, 7)));
    assertFalse(PasswordPolicy.isStrong("lowercase@123"));
    assertFalse(PasswordPolicy.isStrong("UPPERCASE@123"));
    assertFalse(PasswordPolicy.isStrong("NoDigits@Here"));
    assertFalse(PasswordPolicy.isStrong("NoSpecial123"));
    assertFalse(PasswordPolicy.isStrong("Has Space@123"));
    assertThrows(IllegalArgumentException.class, () -> PasswordPolicy.requireStrong("weak"));
  }
}

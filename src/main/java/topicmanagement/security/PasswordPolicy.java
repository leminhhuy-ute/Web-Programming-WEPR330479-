package topicmanagement.security;

/** One password policy shared by account creation, admin reset and self-service change. */
public final class PasswordPolicy {
  public static final String MESSAGE =
      "Mật khẩu mới cần 8–72 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt; không chứa khoảng trắng.";

  private PasswordPolicy() {}

  public static boolean isStrong(String value) {
    return value != null && value.length() >= 8 && value.length() <= 72
        && value.codePoints().anyMatch(Character::isUpperCase)
        && value.codePoints().anyMatch(Character::isLowerCase)
        && value.codePoints().anyMatch(Character::isDigit)
        && value.codePoints().anyMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))
        && value.codePoints().noneMatch(Character::isWhitespace);
  }

  public static void requireStrong(String value) {
    if (!isStrong(value)) throw new IllegalArgumentException(MESSAGE);
  }
}

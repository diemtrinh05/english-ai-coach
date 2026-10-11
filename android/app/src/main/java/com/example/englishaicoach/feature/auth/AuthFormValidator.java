package com.example.englishaicoach.feature.auth;

public final class AuthFormValidator {
    public enum Error { REQUIRED, EMAIL, LENGTH, MISMATCH }
    public static final class Errors {
        public final Error name, email, password, confirmation;
        Errors(Error name, Error email, Error password, Error confirmation) {
            this.name = name; this.email = email; this.password = password; this.confirmation = confirmation;
        }
        public boolean valid() { return name == null && email == null && password == null && confirmation == null; }
    }
    private AuthFormValidator() { }
    public static Errors validate(boolean register, String name, String email, String password, String confirmation) {
        Error nameError = !register ? null : blank(name) ? Error.REQUIRED : name.length() > 100 ? Error.LENGTH : null;
        Error emailError = blank(email) ? Error.REQUIRED : (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || register && email.length() > 255) ? Error.EMAIL : null;
        Error passwordError = blank(password) ? Error.REQUIRED : password.length() > 100
                || register && password.length() < 8 ? Error.LENGTH : null;
        Error confirmationError = !register ? null : blank(confirmation) ? Error.REQUIRED
                : !password.equals(confirmation) ? Error.MISMATCH : null;
        return new Errors(nameError, emailError, passwordError, confirmationError);
    }
    private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}

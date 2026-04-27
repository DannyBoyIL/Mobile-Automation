package com.mobileAutomation.credentials;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;

public final class CredentialsStore {

    /**
     * Loaded once at class init. {@code ignoreIfMissing} so CI runs (env-var-only)
     * don't fail when no {@code .env} file is present on the runner.
     */
    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    /**
     * Aliases spoken in {@code .feature} files. Listed here so the error message
     * for an unknown alias can name every valid one.
     */
    private static final List<String> ALIASES = List.of(
            "the valid user",
            "the unknown user",
            "the wrong-password user",
            "the empty-username user"
    );

    private CredentialsStore() {
    }

    /**
     * Returns the credentials for {@code alias}.
     *
     * @throws IllegalArgumentException if the alias is not recognized
     * @throws IllegalStateException    if a required env var is unset both in the process environment and in {@code .env}
     */
    public static Credentials get(String alias) {
        return switch (alias) {
            case "the valid user" -> new Credentials(load("LOGIN_VALID_USER"), load("LOGIN_VALID_PASSWORD"));
            case "the unknown user" -> new Credentials("foo", "bar");
            case "the wrong-password user" -> new Credentials(load("LOGIN_VALID_USER"), "wrong");
            case "the empty-username user" -> new Credentials("", load("LOGIN_VALID_PASSWORD"));
            default -> throw new IllegalArgumentException(String.format(
                    "Unknown credentials alias: '%s'. Known aliases: %s",
                    alias,
                    ALIASES
            ));
        };
    }

    /**
     * env first, .env fallback. Missing key → IllegalStateException; empty value → "".
     */
    private static String load(String key) {
        String envVal = System.getenv(key);
        if (envVal != null) return envVal;
        String fileVal = DOTENV.get(key);
        if (fileVal == null) {
            throw new IllegalStateException(String.format(
                    "Missing credentials env var: %s Set it in the process environment or in a project-root .env file. See .env.example for the full key list.",
                    key));
        }
        return fileVal;
    }
}

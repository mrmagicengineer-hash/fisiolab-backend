package com.app.fisiolab_system.security;

import java.util.regex.Pattern;

/**
 * Defense-in-depth input sanitizer.
 *
 * JPA repositories already use parameterized queries, so SQL injection is
 * prevented at the persistence layer. This utility adds an additional
 * boundary validation step: it rejects inputs containing suspicious
 * patterns (SQL meta-characters, comment sequences, common SQL keywords)
 * before they reach services or repositories. This protects native queries,
 * log sinks, and any future code path that might concatenate the value.
 */
public final class InputSanitizer {

    private static final Pattern SQL_META_CHARS =
            Pattern.compile(".*([;'\"`]|--|/\\*|\\*/|\\bxp_|\\bexec\\b).*",
                            Pattern.CASE_INSENSITIVE);

    private static final Pattern SQL_KEYWORDS =
            Pattern.compile("\\b(union|select|insert|update|delete|drop|alter|truncate|grant|revoke|create|rename)\\b",
                            Pattern.CASE_INSENSITIVE);

    private static final int MAX_SEARCH_LENGTH = 100;

    private InputSanitizer() {
    }

    /**
     * Validates a free-text search query. Throws IllegalArgumentException
     * if the input exceeds the allowed length or contains patterns that
     * look like SQL injection attempts.
     */
    public static String sanitizeSearchQuery(String query) {
        if (query == null) {
            throw new IllegalArgumentException("La busqueda no puede estar vacia.");
        }
        String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("La busqueda no puede estar vacia.");
        }
        if (trimmed.length() > MAX_SEARCH_LENGTH) {
            throw new IllegalArgumentException(
                    "La busqueda no puede exceder " + MAX_SEARCH_LENGTH + " caracteres.");
        }
        if (SQL_META_CHARS.matcher(trimmed).matches()
                || SQL_KEYWORDS.matcher(trimmed).find()) {
            throw new IllegalArgumentException(
                    "La busqueda contiene caracteres no permitidos.");
        }
        return trimmed;
    }
}

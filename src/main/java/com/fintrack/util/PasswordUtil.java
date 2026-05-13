package com.fintrack.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing utility using BCrypt.
 */
public final class PasswordUtil {

    private static final int BCRYPT_ROUNDS = 12;

    private PasswordUtil() {}

    /**
     * Hashes a plain-text password using BCrypt.
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verifies a plain-text password against a BCrypt hash.
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}

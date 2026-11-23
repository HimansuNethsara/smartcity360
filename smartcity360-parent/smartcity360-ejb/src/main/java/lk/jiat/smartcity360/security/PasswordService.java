package lk.jiat.smartcity360.security;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Singleton
@Lock(LockType.READ)
public class PasswordService {

    public String hash(String plain) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(plain.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean matches(String plain, String storedHex) {
        if (plain == null || storedHex == null) {
            return false;
        }
        return MessageDigest.isEqual(hash(plain).getBytes(StandardCharsets.US_ASCII),
                storedHex.toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }

    public void requireStrong(String plain) {
        if (plain == null || plain.length() < 8) {
            throw new BusinessRuleException("Password must be at least 8 characters.");
        }
        if (plain.length() > 72) {
            throw new BusinessRuleException("Password must be at most 72 characters.");
        }
        boolean letter = plain.chars().anyMatch(Character::isLetter);
        boolean digit = plain.chars().anyMatch(Character::isDigit);
        if (!letter || !digit) {
            throw new BusinessRuleException("Password must contain at least one letter and one digit.");
        }
    }
}

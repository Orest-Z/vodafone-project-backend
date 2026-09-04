package al.vodafone.vodafone_project_backend.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * Generates fake, demo-only eSIM activation data. Nothing here talks to a
 * real SM-DP+/telecom backend — the LPA activation code and install link are
 * well-formed enough that iOS recognizes and attempts to process them (Apple
 * validates the network side, which is real), but the fake SM-DP+ address
 * has no server behind it, so a real device will error out once it tries to
 * actually reach it.
 */
@Service
public class EsimProvisioningService {

    // example.com is IANA-reserved for documentation/testing (RFC 2606): it
    // actually resolves and answers on 443, so Apple's esimsetup.apple.com
    // hand-off succeeds and opens the native "Cellular Plan Detected" screen
    // (matching QR-scan behavior) instead of failing early in Safari with a
    // DNS/connection error. It doesn't speak the GSMA RSP protocol, so the
    // real failure still happens later, once Settings tries to fetch the
    // profile — same point where a real QR scan would fail too.
    private static final String FAKE_SMDP_ADDRESS = "example.com";
    private static final String MATCHING_ID_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public record FakeEsimProfile(String activationCode, String installLink, String phoneNumber) {}

    public FakeEsimProfile generate() {
        String activationCode = "LPA:1$" + FAKE_SMDP_ADDRESS + "$" + generateMatchingId();
        String installLink = "https://esimsetup.apple.com/qrcode?carddata=" + activationCode;
        return new FakeEsimProfile(activationCode, installLink, generatePhoneNumber());
    }

    private String generateMatchingId() {
        StringBuilder sb = new StringBuilder();
        for (int group = 0; group < 4; group++) {
            if (group > 0) sb.append('-');
            for (int i = 0; i < 5; i++) {
                sb.append(MATCHING_ID_CHARS.charAt(RANDOM.nextInt(MATCHING_ID_CHARS.length())));
            }
        }
        return sb.toString();
    }

    private String generatePhoneNumber() {
        int subscriberNumber = RANDOM.nextInt(10_000_000);
        return String.format("+355 69 %03d %04d", subscriberNumber / 10000, subscriberNumber % 10000);
    }
}

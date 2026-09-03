package al.vodafone.vodafone_project_backend.service;

import al.vodafone.vodafone_project_backend.model.Pack;
import al.vodafone.vodafone_project_backend.model.Tourist;
import al.vodafone.vodafone_project_backend.model.UserSubscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Talks to PassKit (app.passkit.com) to issue and update Apple/Google Wallet
 * passes. PassKit holds its own Apple Pass Type ID and Google Wallet issuer
 * account, which is the whole point of using them — we never touch Apple
 * Developer or Google Play Console credentials ourselves.
 *
 * Auth: a Long-Lived API Token, generated from your PassKit program under
 * Settings -> Pass APIs. Keep it out of application.properties/version
 * control the same way you should for the Gmail app password already in
 * there — pass it in as an environment variable in real deployments.
 */
@Service
@Slf4j
public class PassKitService {

    private final RestClient restClient;
    private final String programId;
    private final String tierId;
    private final String passUrlHost;

    public PassKitService(
            @Value("${passkit.api.base-url}") String apiBaseUrl,
            @Value("${passkit.api.token}") String apiToken,
            @Value("${passkit.program-id}") String programId,
            @Value("${passkit.tier-id}") String tierId,
            @Value("${passkit.pass-url-host}") String passUrlHost
    ) {
        this.programId = programId;
        this.tierId = tierId;
        this.passUrlHost = passUrlHost;
        this.restClient = RestClient.builder()
                .baseUrl(apiBaseUrl)
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Enrolls (or updates, since PassKit upserts by externalId) a tourist as
     * a member so their pack shows up as a Wallet pass. Best-effort: if
     * PassKit is unreachable or misconfigured this returns empty rather than
     * throwing, so a PassKit outage never blocks pack activation — same
     * philosophy as email sending elsewhere in this service layer.
     */
    public Optional<PassKitEnrollment> enrollTourist(Tourist tourist, UserSubscription subscription, Pack pack) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("programId", programId);
            body.put("tierId", tierId);
            body.put("externalId", subscription.getId().toString());
            body.put("person", Map.of(
                    "displayName", tourist.getFirstName() + " " + tourist.getLastName(),
                    "forename", tourist.getFirstName(),
                    "surname", tourist.getLastName(),
                    "emailAddress", tourist.getEmail()
            ));

            // TODO — confirm the exact metadata key against your pass design
            // and https://docs.passkit.io/protocols/member/#operation/Members_enrolMember
            // before relying on this. Your program's custom fields (DATA,
            // MINUTES, VALID) and the barcode override for the partner
            // discount QR are set through *some* structured key here — I'm
            // not certain enough of the literal field name from docs alone
            // to ship it silently. Test one call in Postman
            // (https://www.postman.com/passkitinc/passkit-v4-sdk/overview/)
            // against your program, see what key the designer actually
            // reads from, then fill this in — it's one map, not a redesign.
            //
            // body.put("metaData", Map.of(
            //         "data", pack.getDataAllowance(),
            //         "minutes", String.valueOf(pack.getMinutesAllowance()),
            //         "validDays", String.valueOf(pack.getDurationDays())
            // ));
            // body.put("passOverrides", Map.of(
            //         "barcode", Map.of("message", subscription.getOrderRef())
            // ));

            Map<?, ?> response = restClient.put()
                    .uri("/members/member")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            Object memberId = response != null ? response.get("id") : null;
            if (memberId == null) {
                log.warn("PassKit enrollment for subscription {} returned no member id — check response shape.",
                        subscription.getId());
                return Optional.empty();
            }

            String passUrl = passUrlHost + "/" + memberId;
            return Optional.of(new PassKitEnrollment(memberId.toString(), passUrl));

        } catch (Exception e) {
            log.error("PassKit enrollment failed for subscription {}: {}", subscription.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    public record PassKitEnrollment(String memberId, String passUrl) {
        public String applePassUrl() {
            return passUrl + ".pkpass";
        }

        public String googlePassUrl() {
            return passUrl + ".gpay";
        }
    }
}
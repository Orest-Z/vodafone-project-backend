package al.vodafone.vodafone_project_backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory, per-client-IP request throttling for /api/v1/**. There's a
 * single backend instance and no shared cache (no Redis) in this stack, so
 * an in-process bucket per IP is the right-sized fix — if this ever runs as
 * more than one instance, these buckets would need to move to a shared
 * store (e.g. Redis) since each instance would otherwise count separately.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private enum Rule {
        // Payment-adjacent — legitimate traffic here is inherently rare per
        // visitor, so this stays tight.
        ACTIVATION(5, Duration.ofMinutes(1)),
        // Hit automatically on every checkout page load/order attempt, and
        // is otherwise an email-enumeration/spam vector.
        DISCOUNT_LOOKUP(30, Duration.ofMinutes(1)),
        // Game-hub writes — double-spend is already blocked by DB
        // constraints, this is just noise/abuse control.
        GAME_ACTION(20, Duration.ofMinutes(1)),
        // Stateless, no-DB-write pricing preview hit on every slider drag
        // (~1 call per ~300ms while dragging). Bounded well above DEFAULT
        // since legitimate interactive use across all 4 controls in one
        // session can plausibly exceed DEFAULT's 120/min, but this is cheap
        // in-memory arithmetic with no persistence, so a generous per-IP
        // ceiling is still safe.
        CUSTOM_PLAN_QUOTE(300, Duration.ofMinutes(1)),
        // A real write (creates a hidden Pack + 4 PackFeature rows) but,
        // unlike ACTIVATION, happens with no payment gate before it - so it
        // needs its own tier rather than reusing ACTIVATION's 5/min (a user
        // might legitimately re-click "Build & Continue" a couple of times
        // while tweaking sliders) or DEFAULT's 120/min (too loose for a
        // write with no payment proof - would let a scripted client spam
        // Pack rows).
        CUSTOM_PLAN_BUILD(10, Duration.ofMinutes(1)),
        // Baseline ceiling for everything else under /api/v1/** (pack
        // listing, sponsor offers, game-hub reads, subscription status).
        DEFAULT(120, Duration.ofMinutes(1));

        final int capacity;
        final Duration period;

        Rule(int capacity, Duration period) {
            this.capacity = capacity;
            this.period = period;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Preflight requests carry no credentials/payload worth limiting,
        // and throttling them would just break CORS for real clients.
        if (!path.startsWith("/api/v1/") || "OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        Rule rule = ruleFor(path, method);
        String key = rule.name() + ":" + clientIp(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket(rule));

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please slow down and try again shortly.\"}");
        }
    }

    private Rule ruleFor(String path, String method) {
        if ("/api/v1/activations".equals(path) && "POST".equalsIgnoreCase(method)) {
            return Rule.ACTIVATION;
        }
        if ("/api/v1/tourists/discount-by-email".equals(path)) {
            return Rule.DISCOUNT_LOOKUP;
        }
        if ("POST".equalsIgnoreCase(method)
                && (path.equals("/api/v1/drop/play")
                    || path.equals("/api/v1/drop/redrop/play")
                    || path.equals("/api/v1/game-hub/claim-daily-credit"))) {
            return Rule.GAME_ACTION;
        }
        if ("POST".equalsIgnoreCase(method) && "/api/v1/packs/custom/quote".equals(path)) {
            return Rule.CUSTOM_PLAN_QUOTE;
        }
        if ("POST".equalsIgnoreCase(method) && "/api/v1/packs/custom/build".equals(path)) {
            return Rule.CUSTOM_PLAN_BUILD;
        }
        return Rule.DEFAULT;
    }

    private Bucket newBucket(Rule rule) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(rule.capacity)
                .refillGreedy(rule.capacity, rule.period)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

# Vodafone Tourist Pack — Backend

Spring Boot 4.1 / Java 21 REST API behind the [Tourist Pack frontend](../tourists-pack) — pack
catalog and custom-plan pricing, PayPal-verified activation, eSIM provisioning (demo), Apple/Google
Wallet passes via PassKit, and the Daily Drop game hub.

## Stack

- Spring Boot 4.1, Java 21
- Spring Data JPA + PostgreSQL (hosted on Supabase)
- Spring Validation, Spring Mail (Gmail SMTP)
- Lombok
- `com.google.zxing` — real eSIM activation QR codes rendered inline in the confirmation email
- `bucket4j` — in-memory per-IP rate limiting
- Hibernate `ddl-auto=update` — schema changes apply automatically on startup; there is no
  Flyway/Liquibase migration history, so a new nullable column added to an entity shows up in the
  DB the next time the app starts, but **existing rows are never backfilled** (see Gotchas below).

## Structure

```
controller/     REST layer — thin, delegates to services
service/        Business logic (ActivationService, GameService, EmailService, PassKitService,
                CustomPlanPricingService, QrCodeService, EsimProvisioningService, ...)
repository/     Spring Data JPA interfaces
model/          JPA entities = database tables
dto/            Record types for request/response bodies
config/         CorsConfig, RateLimitFilter
```

## Running locally

```
./mvnw spring-boot:run          # http://localhost:8080
./mvnw test                     # full test suite
./mvnw test -Dtest=ClassName    # a single test class
```

Configuration is read from environment variables (see `application.properties`), sourced from a
`.env` file at the repo root via `spring.config.import=optional:file:.env[.properties]` — not
committed, ask for a copy or provide your own credentials for each service below.

| Variable | Used for |
|---|---|
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Supabase PostgreSQL connection |
| `APP_CORS_ALLOWED_ORIGIN` | Comma-separated list of allowed frontend origins |
| `APP_GAME_HUB_TIMEZONE` | Timezone the Daily Drop's "once per day" claim resets in (`Europe/Tirane`) |
| `APP_FRONTEND_BASE_URL` | Used to build links back to the frontend (wallet fallback, game hub, email) |
| `PASSKIT_API_BASE_URL` / `PASSKIT_API_TOKEN` / `PASSKIT_PROGRAM_ID` / `PASSKIT_TIER_ID` / `PASSKIT_PASS_URL_HOST` | Apple/Google Wallet pass issuance via [PassKit](https://passkit.com) |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | Gmail SMTP for the welcome/confirmation email |

### Testing from a phone on the same Wi-Fi

`APP_CORS_ALLOWED_ORIGIN` needs your dev machine's current LAN IP included (comma-separated
alongside `localhost:3000`) or the frontend's requests get CORS-blocked.

## API overview

| Endpoint | Purpose |
|---|---|
| `GET /api/v1/packs`, `GET /api/v1/packs/{id}` | Fixed pack catalog (active packs only for the list; a hidden custom-built pack is still fetchable by id) |
| `GET /api/v1/packs/sponsors` | Partner offers shown on the landing page |
| `POST /api/v1/packs/custom/quote` | Stateless live price for a data/minutes/duration combo |
| `POST /api/v1/packs/custom/build` | Persists that combo as a real (hidden) `Pack` row and returns it — reuses the entire existing checkout pipeline with zero other changes |
| `POST /api/v1/activations` | The paid-activation endpoint — requires PayPal order/capture proof, upserts the Tourist, creates the subscription, enrolls in PassKit, sends the welcome email |
| `GET /api/v1/tourists/{id}/subscription` | Everything `/my-pack` needs: pack details, key dates, order info, game credits, wallet links |
| `GET /api/v1/tourists/discount-by-email` | Looks up an unredeemed game-hub discount for the PayPal `create-order` route to apply |
| Game hub endpoints | Daily credit claim (idempotent per tourist/day), scratch-card play, prize catalog |

## Key architecture notes

**Payment trust boundary.** `POST /activations` is called by the frontend's own Next.js server
(after a real PayPal capture succeeds), never directly by the browser — the backend records the
PayPal order/capture id as proof of payment and guards against duplicates via
`PaymentTransactionRepository.findByPaypalOrderId()`. It does not itself re-verify the charge
amount against PayPal; that trust is established upstream, in the frontend's `create-order` route,
which always re-derives the price server-side from this API rather than trusting a client-supplied
number.

**Custom plan pricing is deterministic, not AI-generated.** `CustomPlanPricingService` is a pure
function of the request (base fee + per-GB/unlimited-flat + per-100-minutes + per-day, rounded to
a clean multiple) — reproducible, auditable, and cheap enough to call on every slider drag. The
constants in `CustomPlanPricingRates` are calibrated against the real fixed-pack prices but are
placeholders pending real business sign-off.

**PassKit enrollment.** `PassKitService` creates a member via `POST /members/member` (not `PUT` —
`PUT` only updates an *existing* member and 404s otherwise, which was a real bug that silently
broke every enrollment before it was found and fixed). The pass's QR code is generated
automatically by PassKit from the member id; the app never sets or overrides it. Dynamic pass
fields (data/minutes/validity/expiry, plus back-of-card links to the game hub and a data-usage
page) are sent as member `metaData` — binding a template field to one of those keys is a one-time
step in PassKit's own visual Pass Designer, not something this API can do.

## Gotchas

- **`ddl-auto=update` never backfills existing rows.** A new nullable column on a table that
  already has data comes back `NULL` for every existing row. A primitive Java field (`boolean`,
  `int`) can't hold that and Hibernate throws on the very next `SELECT` — this has bitten this repo
  more than once. Use wrapper types (`Boolean`, `Integer`) for anything added to an existing table
  unless you're also doing a one-time backfill.
- **PassKit's free/draft tier** caps total passes issued and expires each one after 48 hours —
  fine for a demo, not for anything beyond it. Add a payment method/Apple certificate in the
  PassKit dashboard to lift this.
- No Flyway/Liquibase — there is no schema migration history, only whatever Hibernate infers from
  the current entity definitions at startup.

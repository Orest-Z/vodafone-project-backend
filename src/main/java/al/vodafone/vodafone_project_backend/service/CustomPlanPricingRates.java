package al.vodafone.vodafone_project_backend.service;

import java.math.BigDecimal;

/**
 * Tunable pricing constants for custom "Build Your Own Plan" packs, all in
 * ALL (Lek). Calibrated against the real, live Vodafone Albania 2026
 * Tourist Pack prices (see tourists-pack/features/activation/lib/currency.ts
 * and the packs table): 2,700/2,900/3,300 ALL for 15/21/30 days, each with
 * ~1TB-1.2TB data (effectively unlimited for a tourist) and 1000 national
 * minutes.
 *
 * A custom spec matching a real pack's numbers (unlimitedData=true,
 * minutesAllowance=1000, durationDays=15 or 30) reproduces that pack's
 * exact price (2,700 / 3,300 ALL) with these constants; the 21-day point is
 * ~1.4% off (2,940 vs 2,900) since real pricing isn't perfectly linear in
 * duration - close enough to read as "the same menu," not an exact formula
 * fit to 3 points. See CustomPlanPricingServiceTest for the worked
 * examples. Changing any value here changes future quotes/builds only;
 * already-built Pack rows keep their persisted priceAll.
 */
final class CustomPlanPricingRates {
    private CustomPlanPricingRates() {}

    /** Flat fee always applied (covers SIM/activation overhead). */
    static final BigDecimal BASE_FEE_ALL = new BigDecimal("300");

    /** Per whole GB of metered data - a genuinely cheaper option than
     *  Unlimited for a light-data tourist. */
    static final BigDecimal PER_GB_RATE_ALL = new BigDecimal("20");

    /** Flat charge replacing the per-GB term entirely when unlimitedData=true.
     *  This is the tier real packs effectively sit in (~1TB+). */
    static final BigDecimal UNLIMITED_DATA_FLAT_ALL = new BigDecimal("1200");

    /** Per started block of 100 minutes (ceiling - e.g. 150 min = 2 blocks).
     *  1000 minutes (10 blocks) matches every real pack's flat allowance. */
    static final BigDecimal PER_100_MINUTES_RATE_ALL = new BigDecimal("60");

    /** Per validity day. */
    static final BigDecimal PER_DAY_RATE_ALL = new BigDecimal("40");

    /** Final price is rounded to the nearest multiple of this - "clean"
     *  marketing price points instead of e.g. 2,983 ALL. */
    static final BigDecimal ROUNDING_STEP_ALL = new BigDecimal("10");
}

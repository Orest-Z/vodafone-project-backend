package al.vodafone.vodafone_project_backend.service;

import al.vodafone.vodafone_project_backend.dto.CustomPlanRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Deterministic, server-side pricing for custom plans. Pure and stateless -
 * no DB access, no external calls - so it's safe and cheap to call on every
 * slider drag, and the same inputs always yield the same price.
 */
@Service
public class CustomPlanPricingService {

    public BigDecimal price(CustomPlanRequest req) {
        BigDecimal total = CustomPlanPricingRates.BASE_FEE_ALL
                .add(dataComponent(req))
                .add(blockComponent(req.minutesAllowance(), 100, CustomPlanPricingRates.PER_100_MINUTES_RATE_ALL))
                .add(CustomPlanPricingRates.PER_DAY_RATE_ALL.multiply(BigDecimal.valueOf(req.durationDays())));

        return roundToStep(total, CustomPlanPricingRates.ROUNDING_STEP_ALL).setScale(2);
    }

    public String compactDataAllowanceLabel(CustomPlanRequest req) {
        return req.unlimitedData() ? "Unlimited" : req.dataAllowanceGb() + "GB";
    }

    public String verboseDataAllowanceLabel(CustomPlanRequest req) {
        return req.unlimitedData() ? "Unlimited Data" : req.dataAllowanceGb() + " GB Data";
    }

    public String minutesLabel(int minutes) {
        return minutes + " Minutes";
    }

    public String durationLabel(int days) {
        return days + " Days Validity";
    }

    private BigDecimal dataComponent(CustomPlanRequest req) {
        return req.unlimitedData()
                ? CustomPlanPricingRates.UNLIMITED_DATA_FLAT_ALL
                : CustomPlanPricingRates.PER_GB_RATE_ALL.multiply(BigDecimal.valueOf(req.dataAllowanceGb()));
    }

    private BigDecimal blockComponent(int units, int blockSize, BigDecimal rate) {
        int blocks = (units + blockSize - 1) / blockSize;
        return rate.multiply(BigDecimal.valueOf(blocks));
    }

    private BigDecimal roundToStep(BigDecimal value, BigDecimal step) {
        return value.divide(step, 0, RoundingMode.HALF_UP).multiply(step);
    }
}

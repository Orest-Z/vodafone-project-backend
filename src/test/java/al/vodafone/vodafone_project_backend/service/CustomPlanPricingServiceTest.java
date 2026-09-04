package al.vodafone.vodafone_project_backend.service;

import al.vodafone.vodafone_project_backend.dto.CustomPlanRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CustomPlanPricingServiceTest {

    private final CustomPlanPricingService pricing = new CustomPlanPricingService();

    @Test
    void pricesMeteredDataDeterministically() {
        var req = new CustomPlanRequest(15, false, 300, 15);
        // base 300 + 15*20=300 + ceil(300/100)=3*60=180 + 15*40=600 = 1380
        assertThat(pricing.price(req)).isEqualByComparingTo("1380.00");
    }

    @Test
    void unlimitedDataUsesFlatRateIgnoringDataAllowanceGb() {
        var req = new CustomPlanRequest(null, true, 500, 30);
        // base 300 + 1200 + ceil(500/100)=5*60=300 + 30*40=1200 = 3000
        assertThat(pricing.price(req)).isEqualByComparingTo("3000.00");
    }

    @Test
    void matchesRealPack1PriceForEquivalentSpec() {
        // Pack 1: 15 days, ~1TB (unlimited-equivalent), 1000 national minutes -> 2,700 ALL
        var req = new CustomPlanRequest(null, true, 1000, 15);
        assertThat(pricing.price(req)).isEqualByComparingTo("2700.00");
    }

    @Test
    void matchesRealPack3PriceForEquivalentSpec() {
        // Pack 3: 30 days, ~1.2TB (unlimited-equivalent), 1000 national minutes -> 3,300 ALL
        var req = new CustomPlanRequest(null, true, 1000, 30);
        assertThat(pricing.price(req)).isEqualByComparingTo("3300.00");
    }

    @Test
    void minuteBlocksRoundUpAtBoundaries() {
        var exactBlock = new CustomPlanRequest(1, false, 100, 7);
        var oneOverBlock = new CustomPlanRequest(1, false, 101, 7);
        assertThat(pricing.price(oneOverBlock)).isGreaterThan(pricing.price(exactBlock));
    }

    @Test
    void finalPriceIsAlwaysRoundedToNearestTen() {
        var req = new CustomPlanRequest(7, false, 50, 9);
        BigDecimal price = pricing.price(req);
        assertThat(price.remainder(BigDecimal.TEN)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void isDeterministicForSameInput() {
        var req = new CustomPlanRequest(22, false, 300, 15);
        assertThat(pricing.price(req)).isEqualByComparingTo(pricing.price(req));
    }

    @Test
    void dataAllowanceLabelsMatchDisplayConventions() {
        assertThat(pricing.compactDataAllowanceLabel(new CustomPlanRequest(22, false, 0, 7))).isEqualTo("22GB");
        assertThat(pricing.compactDataAllowanceLabel(new CustomPlanRequest(null, true, 0, 7))).isEqualTo("Unlimited");
    }
}

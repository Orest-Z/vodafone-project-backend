package al.vodafone.vodafone_project_backend.service;

import al.vodafone.vodafone_project_backend.dto.CustomPlanQuoteResponse;
import al.vodafone.vodafone_project_backend.dto.CustomPlanRequest;
import al.vodafone.vodafone_project_backend.dto.PackDto;
import al.vodafone.vodafone_project_backend.dto.PackFeatureDto;
import al.vodafone.vodafone_project_backend.model.Pack;
import al.vodafone.vodafone_project_backend.model.PackFeature;
import al.vodafone.vodafone_project_backend.repository.PackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CustomPlanService {

    private final PackRepository packRepository;
    private final CustomPlanPricingService pricingService;

    public CustomPlanQuoteResponse quote(CustomPlanRequest req) {
        return new CustomPlanQuoteResponse(
                pricingService.price(req),
                pricingService.compactDataAllowanceLabel(req),
                pricingService.minutesLabel(req.minutesAllowance()),
                pricingService.durationLabel(req.durationDays()),
                req.durationDays()
        );
    }

    @Transactional
    public PackDto build(CustomPlanRequest req) {
        BigDecimal price = pricingService.price(req);

        Pack pack = new Pack();
        pack.setTitle("Custom Plan");
        pack.setSubtitle("Built by you");
        pack.setPriceAll(price);
        pack.setDurationDays(req.durationDays());
        pack.setDataAllowance(pricingService.compactDataAllowanceLabel(req));
        pack.setMinutesAllowance(req.minutesAllowance());
        pack.setActive(false);
        pack.setIsCustom(true);
        pack.setSortOrder(0);

        pack.getFeatures().add(feature(pack, pricingService.verboseDataAllowanceLabel(req), "data", 0));
        pack.getFeatures().add(feature(pack, pricingService.minutesLabel(req.minutesAllowance()), "minutes", 1));
        pack.getFeatures().add(feature(pack, pricingService.durationLabel(req.durationDays()), "duration", 2));

        Pack saved = packRepository.save(pack);

        return toDto(saved);
    }

    private PackFeature feature(Pack pack, String label, String iconKey, int sortOrder) {
        PackFeature f = new PackFeature();
        f.setPack(pack);
        f.setLabel(label);
        f.setIconKey(iconKey);
        f.setSortOrder(sortOrder);
        return f;
    }

    private PackDto toDto(Pack pack) {
        return new PackDto(
                pack.getId(),
                pack.getTitle(),
                pack.getSubtitle(),
                pack.getPriceAll(),
                pack.getDurationDays(),
                pack.getDataAllowance(),
                pack.getMinutesAllowance(),
                pack.getRoamingDetails(),
                pack.getImageUrl(),
                pack.getFeatures().stream()
                        .map(f -> new PackFeatureDto(f.getLabel(), f.getIconKey()))
                        .toList()
        );
    }
}

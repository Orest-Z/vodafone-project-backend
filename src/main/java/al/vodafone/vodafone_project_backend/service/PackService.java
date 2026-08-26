package al.vodafone.vodafone_project_backend.service;

import al.vodafone.vodafone_project_backend.dto.PackDto;
import al.vodafone.vodafone_project_backend.dto.PackFeatureDto;
import al.vodafone.vodafone_project_backend.dto.SponsorOfferResponse;
import al.vodafone.vodafone_project_backend.repository.PackRepository;
import al.vodafone.vodafone_project_backend.repository.PartnerOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PackService {

    private final PackRepository packRepository;

  

    @Transactional(readOnly = true)
    public List<PackDto> getAllActivePacks() {
        return packRepository.findAllByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(pack -> new PackDto(
                        pack.getId(),
                        pack.getTitle(),
                        pack.getSubtitle(),
                        pack.getPriceAll(),
                        pack.getDurationDays(),
                        pack.getDataAllowance(),
                        pack.getMinutesAllowance(),
                        pack.getImageUrl(),
                        pack.getFeatures().stream()
                                .map(f -> new PackFeatureDto(f.getLabel(), f.getIconKey()))
                                .toList()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public PackDto getPackById(UUID packId) {
        var pack = packRepository.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Pack not found: " + packId));

        return new PackDto(
                pack.getId(),
                pack.getTitle(),
                pack.getSubtitle(),
                pack.getPriceAll(),
                pack.getDurationDays(),
                pack.getDataAllowance(),
                pack.getMinutesAllowance(),
                pack.getImageUrl(),
                pack.getFeatures().stream()
                        .map(f -> new PackFeatureDto(f.getLabel(), f.getIconKey()))
                        .toList()
        );
    }
      private final PartnerOfferRepository partnerOfferRepository; // add this field, constructor injected via @RequiredArgsConstructor
    public List<SponsorOfferResponse> getActiveSponsorOffers() {
    return partnerOfferRepository.findAllActiveWithPartner().stream()
        .map(po -> new SponsorOfferResponse(
            po.getPartner().getName(),
            po.getDiscountLabel(),
            po.getPartner().getLogoUrl()))
        .toList();
}
}
package al.vodafone.vodafone_project_backend.service;

import al.vodafone.vodafone_project_backend.dto.ActivationRequest;
import al.vodafone.vodafone_project_backend.dto.ActivationResponse;
import al.vodafone.vodafone_project_backend.model.*;
import al.vodafone.vodafone_project_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivationService {

    private final TouristRepository touristRepository;
    private final PackRepository packRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    @Transactional
    public ActivationResponse activate(ActivationRequest req) {
        Pack pack = packRepository.findById(req.packId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid pack ID"));

        // Upsert Tourist by email
        Tourist tourist = touristRepository.findByEmail(req.email())
                .orElseGet(Tourist::new);
        
        tourist.setFirstName(req.firstName());
        tourist.setLastName(req.lastName());
        tourist.setEmail(req.email());
        tourist.setPassportNumber(req.passportNumber());
        tourist.setTermsAccepted(req.termsAccepted());
        tourist = touristRepository.save(tourist);

        // Generate Order Ref (e.g. VF-A8F2K9)
        String orderRef = "VF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Save Subscription
        UserSubscription subscription = new UserSubscription();
        subscription.setTourist(tourist);
        subscription.setPack(pack);
        subscription.setOrderRef(orderRef);
        subscription.setDeliveryMethod(req.deliveryMethod());
        subscription.setStatus(ActivationStatus.ACTIVE);
        subscription = userSubscriptionRepository.save(subscription);

        // Grant +1 bonus credit for Game Hub
        CreditTransaction credit = new CreditTransaction();
        credit.setTourist(tourist);
        credit.setSubscription(subscription);
        credit.setDelta(1);
        credit.setReason(CreditReason.PACK_ACTIVATION_BONUS);
        creditTransactionRepository.save(credit);

        return new ActivationResponse(
                subscription.getId(),
                tourist.getId(),
                subscription.getOrderRef(),
                subscription.getStatus(),
                subscription.getEsimQrUrl()
        );
    }
}
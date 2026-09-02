package al.vodafone.vodafone_project_backend.service;

import al.vodafone.vodafone_project_backend.dto.SubscriptionStatusResponse;
import al.vodafone.vodafone_project_backend.model.PaymentTransaction;
import al.vodafone.vodafone_project_backend.model.Tourist;
import al.vodafone.vodafone_project_backend.model.UserSubscription;
import al.vodafone.vodafone_project_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TouristService {

    private final TouristRepository touristRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    @Transactional(readOnly = true)
    public SubscriptionStatusResponse getSubscriptionStatus(UUID touristId) {
        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(NoSuchElementException::new);

        UserSubscription sub = userSubscriptionRepository
                .findFirstByTouristIdOrderByCreatedAtDesc(touristId)
                .orElseThrow(NoSuchElementException::new);

        var pack = sub.getPack();

        PaymentTransaction payment = paymentTransactionRepository
                .findBySubscriptionId(sub.getId())
                .orElse(null);

        int gameCredits = creditTransactionRepository.getBalanceByTouristId(touristId);

        var expiresAt = sub.getActivatedAt() != null
                ? sub.getActivatedAt().plus(pack.getDurationDays(), ChronoUnit.DAYS)
                : null;

        return new SubscriptionStatusResponse(
                sub.getId(),
                tourist.getFirstName(),
                tourist.getLastName(),
                sub.getOrderRef(),
                pack.getTitle(),
                pack.getSubtitle(),
                pack.getDataAllowance(),
                pack.getMinutesAllowance(),
                pack.getDurationDays(),
                sub.getDeliveryMethod().name(),
                sub.getStatus().name(),
                sub.getActivatedAt(),
                expiresAt,
                payment != null ? payment.getAmountPaid() : null,
                payment != null ? payment.getCurrency() : null,
                gameCredits
        );
    }
}

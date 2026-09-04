package al.vodafone.vodafone_project_backend.service;

import al.vodafone.vodafone_project_backend.dto.ActivationRequest;
import al.vodafone.vodafone_project_backend.dto.ActivationResponse;
import al.vodafone.vodafone_project_backend.model.*;
import al.vodafone.vodafone_project_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import al.vodafone.vodafone_project_backend.dto.DiscountLookupResponse;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ActivationService {

    private final TouristRepository touristRepository;
    private final PackRepository packRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final GamePlayRepository gamePlayRepository;
    private final EmailService emailService;
    private final PassKitService passKitService;
    private final EsimProvisioningService esimProvisioningService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Transactional
    public ActivationResponse activate(ActivationRequest req) {
        Pack pack = packRepository.findById(req.packId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid pack ID"));
                
        // Guard against duplicate submissions (e.g. a retried/double-clicked
        // capture on the frontend) re-inserting a second paid activation for
        // the same PayPal order.
        if (paymentTransactionRepository.findByPaypalOrderId(req.paypalOrderId()).isPresent()) {
            throw new IllegalStateException("This PayPal order has already been recorded");
        }

        // Upsert Tourist by email
        Tourist tourist = touristRepository.findByEmail(req.email())
                .orElseGet(Tourist::new);

        tourist.setFirstName(req.firstName());
        tourist.setLastName(req.lastName());
        tourist.setEmail(req.email());
        tourist.setPassportNumber(req.passportNumber());
        tourist.setTermsAccepted(req.termsAccepted());
        tourist = touristRepository.save(tourist);

        // Consume any unredeemed game-hub discount now that a purchase has
        // actually gone through. create-order (frontend) always applies a
        // tourist's unredeemed PACK_DISCOUNT to the price it quotes PayPal
        // whenever one exists, so an existing unredeemed discount at this
        // point necessarily means it was already baked into amountPaid —
        // without this, the same discount could be reused indefinitely.
        gamePlayRepository.findFirstUnredeemedPackDiscount(tourist.getId())
                .ifPresent(discountPlay -> {
                    discountPlay.setRedeemedAt(java.time.Instant.now());
                    gamePlayRepository.save(discountPlay);
                });

        // Generate Order Ref (e.g. VF-A8F2K9)
        String orderRef = "VF-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Save Subscription
        UserSubscription subscription = new UserSubscription();
        subscription.setTourist(tourist);
        subscription.setPack(pack);
        subscription.setOrderRef(orderRef);
        subscription.setDeliveryMethod(req.deliveryMethod());
        subscription.setStatus(ActivationStatus.ACTIVE);
        // Never set anywhere before — every subscription's activatedAt (and
        // therefore /my-pack's derived expiresAt) was silently null, which
        // is why "Activated"/"Expires" only ever showed dashes.
        subscription.setActivatedAt(java.time.Instant.now());

        if (subscription.getDeliveryMethod() == DeliveryMethod.ESIM) {
            EsimProvisioningService.FakeEsimProfile esim = esimProvisioningService.generate();
            subscription.setEsimActivationCode(esim.activationCode());
            subscription.setEsimQrUrl(esim.installLink());
            subscription.setEsimManualCode(esim.activationCode());
            subscription.setEsimPhoneNumber(esim.phoneNumber());
        }

        subscription = userSubscriptionRepository.save(subscription);

        // Record the payment itself — this is the row that was missing
        // entirely before: without it there was no durable proof in our own
        // database that money had actually changed hands for this order.
        PaymentTransaction payment = new PaymentTransaction();
        payment.setSubscription(subscription);
        payment.setTourist(tourist);
        payment.setPaypalOrderId(req.paypalOrderId());
        payment.setPaypalCaptureId(req.paypalCaptureId());
        payment.setAmountPaid(req.amountPaid());
        payment.setCurrency(req.currency());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment = paymentTransactionRepository.save(payment);

        // Grant +1 bonus credit for Game Hub
        CreditTransaction credit = new CreditTransaction();
        credit.setTourist(tourist);
        credit.setSubscription(subscription);
        credit.setDelta(1);
        credit.setReason(CreditReason.PACK_ACTIVATION_BONUS);
        creditTransactionRepository.save(credit);

        Optional<PassKitService.PassKitEnrollment> passkitEnrollment =
                passKitService.enrollTourist(tourist, subscription, pack);

        String appleWalletUrl = frontendBaseUrl + "/wallet/apple/" + subscription.getId();
        String googleWalletUrl = frontendBaseUrl + "/wallet/google/" + subscription.getId();

        if (passkitEnrollment.isPresent()) {
            PassKitService.PassKitEnrollment enrollment = passkitEnrollment.get();
            subscription.setPasskitMemberId(enrollment.memberId());
            subscription.setPasskitPassUrl(enrollment.passUrl());
            subscription = userSubscriptionRepository.save(subscription);

            appleWalletUrl = enrollment.applePassUrl();
            googleWalletUrl = enrollment.googlePassUrl();
        }

        TouristWelcomeEmailContext emailContext = new TouristWelcomeEmailContext(
                tourist.getFirstName(),
                subscription.getOrderRef(),
                pack.getTitle(),
                pack.getDataAllowance(),
                pack.getMinutesAllowance(),
                pack.getDurationDays(),
                subscription.getDeliveryMethod(),
                subscription.getEsimQrUrl(),
                subscription.getEsimManualCode(),
                subscription.getEsimActivationCode(),
                subscription.getEsimPhoneNumber(),
                frontendBaseUrl + "/game-hub?touristId=" + tourist.getId(),
                appleWalletUrl,
                googleWalletUrl,
                frontendBaseUrl + "/my-pack?touristId=" + tourist.getId()
        );
        emailService.sendTouristWelcomeEmail(req.email(), emailContext);

        return new ActivationResponse(
                subscription.getId(),
                tourist.getId(),
                payment.getId(),
                subscription.getOrderRef(),
                subscription.getStatus(),
                subscription.getEsimQrUrl()
        );
        
    }

            @Transactional(readOnly = true)
        public DiscountLookupResponse getAvailableDiscountByEmail(String email) {
            return touristRepository.findByEmail(email)
                    .flatMap(t -> gamePlayRepository.findFirstUnredeemedPackDiscount(t.getId()))
                    .map(gp -> new DiscountLookupResponse(true, gp.getPrize().getDiscountPercent()))
                    .orElse(new DiscountLookupResponse(false, null));
        }
}
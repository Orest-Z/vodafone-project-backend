package al.vodafone.vodafone_project_backend.repository;

import al.vodafone.vodafone_project_backend.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    Optional<PaymentTransaction> findByPaypalOrderId(String paypalOrderId);
    Optional<PaymentTransaction> findByPaypalCaptureId(String paypalCaptureId);
}
package za.ac.cput.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.enums.PaymentStatus;
import za.ac.cput.repository.PaymentRepository;
import za.ac.cput.repository.PromotionRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Re-derives every promotion's usageCount from the payments that actually used it.
 *
 * usageCount is the atomic guard that stops a code being redeemed past its limit, so it has to
 * be stored. But older code paths could make it drift: a non-atomic read-then-save that let a
 * retry count twice, and deleting a paid payment without giving its use back. Running this at
 * startup puts the stored number back in line with the PAID payment rows (the source of truth)
 * and logs every correction, so nothing changes silently.
 */
@Component
public class PromotionUsageReconciler implements ApplicationRunner, Ordered {

    private static final Logger log = LoggerFactory.getLogger(PromotionUsageReconciler.class);

    private final PromotionRepository promotions;
    private final PaymentRepository payments;

    public PromotionUsageReconciler(PromotionRepository promotions, PaymentRepository payments) {
        this.promotions = promotions;
        this.payments = payments;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, Long> paid = new HashMap<>();
        for (Object[] row : payments.countByPromotionAndStatus(PaymentStatus.PAID))
            paid.put((String) row[0], ((Number) row[1]).longValue());
        for (Promotion p : promotions.findAll()) {
            int actual = paid.getOrDefault(p.getPromotionId(), 0L).intValue();
            if (p.getUsageCount() != actual) {
                promotions.setUsageCount(p.getPromotionId(), actual);
                log.warn("Promotion {}: stored usage count was {}, paid payments say {}. Corrected.",
                        p.getCode(), p.getUsageCount(), actual);
            }
        }
    }
}

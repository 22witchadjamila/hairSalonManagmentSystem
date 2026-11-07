package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.ac.cput.domain.Promotion;

import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, String> {
    Optional<Promotion> findByCode(String code);

    /** Customers type codes in any case; "summer10" must find SUMMER10. */
    Optional<Promotion> findByCodeIgnoreCase(String code);

    /**
     * Counts one use - but only if a use is still available - in a single UPDATE. Returns 0 when
     * the limit is already reached. Reading the count and saving count+1 in Java instead would let
     * two simultaneous payments both take the last use.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Promotion p set p.usageCount = p.usageCount + 1 "
            + "where p.promotionId = :id and p.usageCount < p.usageLimit")
    int incrementUsageIfAvailable(@Param("id") String promotionId);

    /** Gives a use back when a payment that used the code is refunded. Never goes below zero. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Promotion p set p.usageCount = p.usageCount - 1 "
            + "where p.promotionId = :id and p.usageCount > 0")
    int decrementUsage(@Param("id") String promotionId);

    /**
     * Saves an admin's edit WITHOUT touching usageCount. Saving a whole entity would write back
     * the count as it was when the edit form loaded, silently undoing any payment recorded in
     * between. The usageCount guard also makes it impossible to set a limit below the uses
     * already counted: the row simply isn't updated and 0 is returned.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Promotion p set p.code = :code, p.description = :description, "
            + "p.discountType = :discountType, p.discountValue = :discountValue, "
            + "p.dateRange.startDate = :startDate, p.dateRange.endDate = :endDate, "
            + "p.usageLimit = :usageLimit "
            + "where p.promotionId = :id and p.usageCount <= :usageLimit")
    int updateDetails(@Param("id") String id, @Param("code") String code,
                      @Param("description") String description,
                      @Param("discountType") za.ac.cput.domain.enums.DiscountType discountType,
                      @Param("discountValue") java.math.BigDecimal discountValue,
                      @Param("startDate") java.time.LocalDate startDate,
                      @Param("endDate") java.time.LocalDate endDate,
                      @Param("usageLimit") int usageLimit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Promotion p set p.ended = :ended where p.promotionId = :id")
    int setEnded(@Param("id") String id, @Param("ended") boolean ended);

    /** Startup reconciliation only - see PromotionUsageReconciler. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Promotion p set p.usageCount = :count where p.promotionId = :id")
    int setUsageCount(@Param("id") String id, @Param("count") int count);
}

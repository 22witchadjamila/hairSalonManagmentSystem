package za.ac.cput.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.enums.DiscountType;
import za.ac.cput.domain.enums.PaymentStatus;
import za.ac.cput.domain.valueobject.DateRange;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.repository.AppointmentRepository;
import za.ac.cput.repository.PaymentRepository;
import za.ac.cput.repository.PromotionRepository;
import za.ac.cput.repository.SalonServiceRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Promotion usage - the 'times used' policy")
class PromotionUsageTest {

    @Mock PromotionRepository promotions;
    @Mock SalonServiceRepository services;
    @Mock AppointmentRepository appointments;
    @Mock PaymentRepository payments;
    @InjectMocks PromotionOfferService offers;

    private static final LocalDate TODAY = LocalDate.now();

    private static Promotion promo(int used, int limit, boolean ended) {
        return new Promotion.Builder().setPromotionId("p1").setCode("SUMMER10").setDiscountType(DiscountType.PERCENTAGE)
                .setDiscountValue(BigDecimal.TEN).setDateRange(DateRange.of(TODAY.minusDays(1), TODAY.plusDays(10)))
                .setUsageLimit(limit).setUsageCount(used).setEnded(ended).build();
    }

    private static List<Object[]> rows(Object[]... rows) {
        return List.of(rows);
    }

    @Test
    @DisplayName("stats come from the rows: paid = used, unpaid bookings = held, refunds shown apart")
    void statsFromRows() {
        when(promotions.findAll()).thenReturn(List.of(promo(2, 10, false)));
        when(payments.countByPromotionAndStatus(PaymentStatus.PAID)).thenReturn(rows(new Object[]{"p1", 2L}));
        when(payments.countByPromotionAndStatus(PaymentStatus.REFUNDED)).thenReturn(rows(new Object[]{"p1", 1L}));
        when(appointments.countUnpaidBookingsByPromoCode(anyCollection(), anyCollection()))
                .thenReturn(rows(new Object[]{"SUMMER10", 3L}));

        PromotionOfferService.UsageStats s = offers.usageStats().get(0);

        assertEquals(2, s.redeemed());
        assertEquals(3, s.heldByBookings());
        assertEquals(1, s.refunded());
        assertEquals(5, s.remaining());   // 10 - 2 used - 3 held
        assertEquals("ACTIVE", s.status());
    }

    @Test
    @DisplayName("booking is refused once paid uses + unpaid holds reach the limit")
    void bookingRespectsHolds() {
        when(promotions.findByCodeIgnoreCase("summer10")).thenReturn(Optional.of(promo(1, 3, false)));
        when(appointments.countUnpaidBookingsByPromoCode(anyCollection(), anyCollection()))
                .thenReturn(rows(new Object[]{"SUMMER10", 2L}));
        assertThrows(InvalidOperationException.class, () -> offers.requireBookable("summer10"));
    }

    @Test
    @DisplayName("booking is allowed while there is room")
    void bookingAllowedWithRoom() {
        when(promotions.findByCodeIgnoreCase("SUMMER10")).thenReturn(Optional.of(promo(1, 5, false)));
        when(appointments.countUnpaidBookingsByPromoCode(anyCollection(), anyCollection()))
                .thenReturn(rows(new Object[]{"SUMMER10", 2L}));
        assertEquals("SUMMER10", offers.requireBookable("SUMMER10").getCode());
    }

    @Test
    @DisplayName("an ended promotion can't be booked, paid with, or offered")
    void endedPromotionIsUnusable() {
        Promotion ended = promo(0, 10, true);
        when(promotions.findByCodeIgnoreCase("SUMMER10")).thenReturn(Optional.of(ended));
        assertThrows(InvalidOperationException.class, () -> offers.requireUsable("SUMMER10"));
        assertFalse(PromotionOfferService.isActive(ended, TODAY));
        assertEquals("ENDED", PromotionOfferService.statusOf(ended, TODAY));
    }

    @Test
    @DisplayName("redeem fails cleanly when another payment took the last use")
    void redeemIsAtomic() {
        when(promotions.incrementUsageIfAvailable("p1")).thenReturn(0);
        assertThrows(InvalidOperationException.class, () -> offers.redeem(promo(5, 5, false)));
    }

    @Test
    @DisplayName("statusOf covers every state")
    void statuses() {
        assertEquals("FULLY_USED", PromotionOfferService.statusOf(promo(5, 5, false), TODAY));
        Promotion future = new Promotion.Builder().setPromotionId("p2").setCode("SOON").setDateRange(
                DateRange.of(TODAY.plusDays(2), TODAY.plusDays(4))).setUsageLimit(1).build();
        assertEquals("SCHEDULED", PromotionOfferService.statusOf(future, TODAY));
        assertEquals("EXPIRED", PromotionOfferService.statusOf(future, TODAY.plusDays(5)));
    }
}

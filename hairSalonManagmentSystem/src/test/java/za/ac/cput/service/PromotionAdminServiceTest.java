package za.ac.cput.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.enums.DiscountType;
import za.ac.cput.domain.valueobject.DateRange;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.repository.PromotionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromotionAdminService - create, edit, end early")
class PromotionAdminServiceTest {

    @Mock PromotionRepository promotions;
    @Mock PromotionOfferService offers;
    @InjectMocks PromotionAdminService service;

    private static final LocalDate TODAY = LocalDate.now();

    private static PromotionAdminService.Details details(String code, DiscountType type, String value,
                                                         LocalDate start, LocalDate end, Integer limit) {
        return new PromotionAdminService.Details(code, "desc", type, value == null ? null : new BigDecimal(value), start, end, limit);
    }

    private static PromotionAdminService.Details valid() {
        return details("SUMMER10", DiscountType.PERCENTAGE, "10", TODAY, TODAY.plusDays(30), 50);
    }

    private static Promotion existing(int used) {
        return new Promotion.Builder().setPromotionId("p1").setCode("SUMMER10").setDiscountType(DiscountType.PERCENTAGE)
                .setDiscountValue(BigDecimal.TEN).setDateRange(DateRange.of(TODAY, TODAY.plusDays(30)))
                .setUsageLimit(50).setUsageCount(used).build();
    }

    @Test
    @DisplayName("create: normalises the code and starts the count at zero")
    void createsValidPromotion() {
        when(promotions.findByCodeIgnoreCase("SUMMER10")).thenReturn(Optional.empty());
        when(promotions.save(any(Promotion.class))).thenAnswer(inv -> inv.getArgument(0));
        Promotion p = service.create(details("  summer10 ", DiscountType.PERCENTAGE, "10", TODAY, TODAY.plusDays(5), 5));
        assertEquals("SUMMER10", p.getCode());
        assertEquals(0, p.getUsageCount());
        assertFalse(p.isEnded());
    }

    @Test
    @DisplayName("rejects bad input with a readable message")
    void rejectsInvalidInput() {
        lenient().when(promotions.findByCodeIgnoreCase(anyString())).thenReturn(Optional.empty());
        List<PromotionAdminService.Details> bad = List.of(
                details("", DiscountType.PERCENTAGE, "10", TODAY, TODAY, 5),                     // no code
                details("HAS SPACE", DiscountType.PERCENTAGE, "10", TODAY, TODAY, 5),            // bad code
                details("OK10", null, "10", TODAY, TODAY, 5),                                    // no type
                details("OK10", DiscountType.PERCENTAGE, "0", TODAY, TODAY, 5),                  // zero
                details("OK10", DiscountType.PERCENTAGE, "-5", TODAY, TODAY, 5),                 // negative
                details("OK10", DiscountType.PERCENTAGE, "101", TODAY, TODAY, 5),                // > 100%
                details("OK10", DiscountType.FIXED_AMOUNT, "10.005", TODAY, TODAY, 5),           // 3 decimals
                details("OK10", DiscountType.PERCENTAGE, "10", TODAY.plusDays(5), TODAY, 5),     // end < start
                details("OK10", DiscountType.PERCENTAGE, "10", null, TODAY, 5),                  // no start
                details("OK10", DiscountType.PERCENTAGE, "10", TODAY, TODAY, 0),                 // limit 0
                details("OK10", DiscountType.PERCENTAGE, "10", TODAY.minusDays(9), TODAY.minusDays(1), 5)); // already over
        for (PromotionAdminService.Details d : bad)
            assertThrows(InvalidOperationException.class, () -> service.create(d), d.toString());
        verify(promotions, never()).save(any());
    }

    @Test
    @DisplayName("rejects a code another promotion already uses")
    void rejectsDuplicateCode() {
        Promotion other = existing(0);
        when(promotions.findByCodeIgnoreCase("SUMMER10")).thenReturn(Optional.of(other));
        assertThrows(InvalidOperationException.class, () -> service.create(valid()));
    }

    @Test
    @DisplayName("edit keeps its own code, and never writes the usage count")
    void editDoesNotTouchUsage() {
        Promotion p = existing(7);
        when(promotions.findById("p1")).thenReturn(Optional.of(p));
        when(promotions.findByCodeIgnoreCase("SUMMER10")).thenReturn(Optional.of(p));
        when(promotions.updateDetails(eq("p1"), eq("SUMMER10"), any(), any(), any(), any(), any(), eq(60))).thenReturn(1);

        service.update("p1", details("SUMMER10", DiscountType.PERCENTAGE, "15", TODAY, TODAY.plusDays(40), 60));

        verify(promotions).updateDetails(eq("p1"), eq("SUMMER10"), eq("desc"), eq(DiscountType.PERCENTAGE),
                eq(new BigDecimal("15")), eq(TODAY), eq(TODAY.plusDays(40)), eq(60));
        verify(promotions, never()).save(any());
        verify(promotions, never()).setUsageCount(anyString(), anyInt());
    }

    @Test
    @DisplayName("edit can't lower the limit below uses already counted")
    void limitNotBelowUsage() {
        Promotion p = existing(7);
        when(promotions.findById("p1")).thenReturn(Optional.of(p));
        when(promotions.findByCodeIgnoreCase("SUMMER10")).thenReturn(Optional.of(p));
        assertThrows(InvalidOperationException.class,
                () -> service.update("p1", details("SUMMER10", DiscountType.PERCENTAGE, "10", TODAY, TODAY.plusDays(1), 6)));
        verify(promotions, never()).updateDetails(any(), any(), any(), any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("end and reopen flip only the ended flag")
    void endAndReopen() {
        when(promotions.findById("p1")).thenReturn(Optional.of(existing(3)));
        service.end("p1");
        verify(promotions).setEnded("p1", true);
        service.reopen("p1");
        verify(promotions).setEnded("p1", false);
    }

    @Test
    @DisplayName("a promotion with history can't be deleted - end it instead")
    void deleteRefusedWithHistory() {
        when(promotions.findById("p1")).thenReturn(Optional.of(existing(0)));
        when(offers.usageStats()).thenReturn(List.of(
                new PromotionOfferService.UsageStats("p1", "SUMMER10", 0, 2, 0, 50, 48, "ACTIVE")));
        assertThrows(InvalidOperationException.class, () -> service.delete("p1"));
        verify(promotions, never()).deleteById(any());
    }
}

package za.ac.cput.domain.valueobject;

import jakarta.persistence.Embeddable;

import java.time.LocalDate;

/** A start/end date pair. Cannot exist with endDate before startDate*/
@Embeddable
public class DateRange {

    private LocalDate startDate;
    private LocalDate endDate;

    protected DateRange() {}

    private DateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static DateRange of(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Invalid date range: endDate cannot be before startDate ("+ startDate +"-"+
                    endDate +")");
        }
        return new DateRange(startDate, endDate);
    }

}

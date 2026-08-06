package za.ac.cput.domain.valueobject;

import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.util.Objects;

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

    /** True if the given date falls within this range, inclusive of both ends. */
    public boolean contains(LocalDate date){
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof DateRange dateRange)) return false;
        return Objects.equals(startDate, dateRange.startDate) && Objects.equals(endDate, dateRange.endDate);
    }

    @Override
    public int hashCode(){
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString(){
        return startDate + " to " + endDate;
    }
}

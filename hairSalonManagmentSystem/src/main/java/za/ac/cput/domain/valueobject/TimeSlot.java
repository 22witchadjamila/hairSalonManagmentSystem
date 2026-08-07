package za.ac.cput.domain.valueobject;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeSlot {

    private LocalTime startTime;
    private LocalTime endTime;

    protected TimeSlot(){}

    private TimeSlot(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static TimeSlot of(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "Invalid time slot: endTime must be after startTime (" + startTime + " - "+
                            endTime + ")");
        }
        return new TimeSlot(startTime, endTime);
    }

    /** True if this slot and other slot*/
}

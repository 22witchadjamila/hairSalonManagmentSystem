package za.ac.cput.domain.valueobject;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

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

    /** True if this slot and other slot share any time in common*/
    public boolean overlaps(TimeSlot other){
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime(){
        return endTime;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof TimeSlot timeSlot)) return false;
        return Objects.equals(startTime, timeSlot.startTime) && Objects.equals(endTime, timeSlot.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }

    @Override
    public String toString() {
        return startTime + " - " + endTime;
    }
}

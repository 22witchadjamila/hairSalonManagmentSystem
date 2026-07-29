package za.ac.cput.factory;

import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.Stylist;
import za.ac.cput.domain.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class AppointmentFactory {

    public static Appointment buildAppointment(Customer customer, Stylist stylist,
                                               LocalDate date, LocalTime startTime,
                                               int durationMinutes, String notes) {
        if(customer == null || stylist == null) return null;
        if(date == null || startTime == null) return null;
        if(date.isBefore(LocalDate.now())) return null;
        if(durationMinutes <= 0) return null;

        return new Appointment.Builder()
                .setAppointmentId(UUID.randomUUID().toString())
                .setCustomer(customer)
                .setStylist(stylist)
                .setAppointmentDate(date)
                .setStartTime(startTime)
                .setEndTime(startTime.plusMinutes(durationMinutes))
                .setStatus(AppointmentStatus.PENDING)
                .setNotes(notes)
                .setCreatedAt(LocalDateTime.now())
                .build();
    }
}


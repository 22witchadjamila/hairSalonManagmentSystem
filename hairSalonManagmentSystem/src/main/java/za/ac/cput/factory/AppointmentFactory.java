package za.ac.cput.factory;

import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.Stylist;
import za.ac.cput.domain.enums.AppointmentStatus;
import za.ac.cput.domain.valueobject.TimeSlot;
import za.ac.cput.util.Helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppointmentFactory {

    /** Duration is derived from the chosen SalonService, so an appointment's length can never drift from the service it's booking. */
    public static Appointment buildAppointment(Customer customer, Stylist stylist, SalonService salonService,
                                               LocalDate date, LocalTime startTime, String notes) {
        if(customer == null || stylist == null || salonService == null) return null;
        if(date == null || startTime == null) return null;
        if(date.isBefore(LocalDate.now())) return null;

        TimeSlot timeSlot = TimeSlot.of(startTime, startTime.plusMinutes(salonService.getDurationMinutes()));

        return new Appointment.Builder()
                .setAppointmentId(Helper.generateId())
                .setCustomer(customer)
                .setStylist(stylist)
                .setSalonService(salonService)
                .setAppointmentDate(date)
                .setTimeSlot(timeSlot)
                .setStatus(AppointmentStatus.PENDING)
                .setNotes(notes)
                .setCreatedAt(LocalDateTime.now())
                .build();
    }
}

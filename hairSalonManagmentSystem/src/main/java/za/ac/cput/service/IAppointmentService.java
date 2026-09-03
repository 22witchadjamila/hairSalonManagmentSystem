package za.ac.cput.service;

import za.ac.cput.domain.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IAppointmentService extends IService<Appointment, String> {
    Appointment bookAppointment(String customerId, String stylistId, String salonServiceId,
                                 LocalDate date, LocalTime startTime, String notes);
    Appointment confirmAppointment(String appointmentId);
    Appointment cancelAppointment(String appointmentId);
    Appointment completeAppointment(String appointmentId);
    List<Appointment> findByCustomer(String customerId);
    List<Appointment> findByStylist(String stylistId);
}

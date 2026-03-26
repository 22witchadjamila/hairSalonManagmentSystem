package za.ac.cput.repository.appointment.impl;

import za.ac.cput.domain.Appointment;
import java.util.Set;
//Name: Dayyaan Francis//
//Student number: 222277343//
//Class: AppointmentRepository Interface//

public interface IAppointmentRepository {

    Appointment create(Appointment appointment);

    Appointment read(String id);

    Appointment update(Appointment appointment);

    boolean delete(String id);

    Set<Appointment> getAll();
}

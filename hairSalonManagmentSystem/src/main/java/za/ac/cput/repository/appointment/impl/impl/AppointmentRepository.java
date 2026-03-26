package za.ac.cput.repository.appointment.impl.impl;

import za.ac.cput.domain.Appointment;
import za.ac.cput.repository.appointment.impl.IAppointmentRepository;

import java.util.HashSet;
import java.util.Set;
//Name: Dayyaan Francis//
//Student number: 222277343//
//Class: Appointment Repository//

public class AppointmentRepository implements IAppointmentRepository {

    private static AppointmentRepository repository = null;
    private Set<Appointment> appointmentDB = null;

    private AppointmentRepository() {
        appointmentDB = new HashSet<>();
    }

    public static AppointmentRepository getRepository() {
        if (repository == null)
            repository = new AppointmentRepository();
        return repository;
    }

    @Override
    public Appointment create(Appointment appointment) {
        appointmentDB.add(appointment);
        return appointment;
    }

    @Override
    public Appointment read(String id) {
        return appointmentDB.stream()
                .filter(a -> a.getAppointmentId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Appointment update(Appointment appointment) {
        Appointment oldAppointment = read(appointment.getAppointmentId());
        if (oldAppointment != null) {
            appointmentDB.remove(oldAppointment);
            appointmentDB.add(appointment);
            return appointment;
        }
        return null;
    }

    @Override
    public boolean delete(String id) {
        Appointment appointmentToDelete = read(id);
        if (appointmentToDelete != null) {
            appointmentDB.remove(appointmentToDelete);
            return true;
        }
        return false;
    }

    @Override
    public Set<Appointment> getAll() {
        return appointmentDB;
    }
}
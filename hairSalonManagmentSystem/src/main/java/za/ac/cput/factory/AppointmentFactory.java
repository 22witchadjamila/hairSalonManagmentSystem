package za.ac.cput.factory;

import za.ac.cput.domain.Appointment;
import java.util.UUID;

public class AppointmentFactory {

    public static Appointment createAppointment(String date, String time,
                                                String clientId,
                                                String employeeId,
                                                String serviceId) {

        String id = UUID.randomUUID().toString();

        return new Appointment.Builder()
                .setAppointmentId(id)
                .setDate(date)
                .setTime(time)
                .setClientId(clientId)
                .setEmployeeId(employeeId)
                .setServiceId(serviceId)
                .build();
    }
}


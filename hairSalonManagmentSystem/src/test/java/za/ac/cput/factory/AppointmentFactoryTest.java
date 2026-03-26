package za.ac.cput.factory;

import za.ac.cput.domain.Appointment;

public class AppointmentFactoryTest {
//Name: Dayyaan Francis//
    //Student number: 222277343//
    //Class: AppointmentFactoryTest//
    public static void main(String[] args) {

        Appointment appointment = AppointmentFactory.createAppointment(
                "2026-05-01",
                "10:00",
                "C001",
                "E001",
                "S001"
        );

        if (appointment != null) {
            System.out.println("Appointment created successfully:");
            System.out.println(appointment);
        } else {
            System.out.println("Appointment creation failed.");
        }
    }
}


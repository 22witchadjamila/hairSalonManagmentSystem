package za.ac.cput.repository.appointment.impl;

import za.ac.cput.repository.appointment.impl.impl.AppointmentRepository;

public class AppointmentRepositoryTest {
    //Name: Dayyaan Francis//
    //Student number: 222277343//
    //Class: AppointmentRepositoryTest//

    public static void main(String[] args) {

        // Just test if repository instance can be obtained
        AppointmentRepository repository = AppointmentRepository.getRepository();

        if (repository != null) {
            System.out.println("Repository instance obtained ✅");
        } else {
            System.out.println("Failed to get repository ❌");
        }

        // Just test if getAll() works without needing appointments
        System.out.println("All appointments: " + repository.getAll());
    }
}
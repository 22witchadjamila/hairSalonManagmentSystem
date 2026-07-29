/* StylistFactory.java
- Created Stylist objects with basic validation.
Author: Will Bryan Koeries
Student Number: 240160711
Date: 20 March 2026
*/


package za.ac.cput.factory;

import za.ac.cput.domain.Stylist;

import java.util.UUID;


public class StylistFactory {

    public static Stylist buildStylist(String firstName, String lastName,
                                       String email, String phoneNumber,
                                       String speciality) {
        if (firstName == null || firstName.isBlank()) return null;
        if (lastName == null || lastName.isBlank()) return null;
        if (email == null || !email.contains("@")) return null;
        if (phoneNumber == null || phoneNumber.isBlank()) return null;

        return new Stylist.Builder()
                .setStylistId(UUID.randomUUID().toString())
                .setFirstName(firstName.trim())
                .setLastName(lastName.trim())
                .setEmail(email.trim().toLowerCase())
                .setPhoneNumber(phoneNumber.trim())
                .setSpeciality(speciality.trim())
                .setActive(true)
                .build();
    }

    public static Stylist buildStylistWithBio(String firstName, String lastName,
                                              String email, String phoneNumber,
                                              String speciality, String bio) {
        if (firstName == null || firstName.isBlank()) return null;
        if (lastName == null || lastName.isBlank()) return null;
        if (email == null || !email.contains("@")) return null;

        return new Stylist.Builder()
                .setStylistId(UUID.randomUUID().toString())
                .setFirstName(firstName.trim())
                .setLastName(lastName.trim())
                .setEmail(email.trim().toLowerCase())
                .setPhoneNumber(phoneNumber)
                .setSpeciality(speciality)
//                .setBio(bio)
                .setActive(true)
                .build();
    }
}
/* StylistFactory.java
- Created Stylist objects with basic validation.
Author: Will Bryan Koeries
Student Number: 240160711
Date: 20 March 2026
*/


package za.ac.cput.factory;

import za.ac.cput.domain.Stylist;
import za.ac.cput.domain.valueobject.Email;
import za.ac.cput.domain.valueobject.PhoneNumber;
import za.ac.cput.util.Helper;



public class StylistFactory {

    public static Stylist buildStylist(String firstName, String lastName,
                                       String email, String phoneNumber,
                                       String speciality) {
        if (Helper.isNullOrEmpty(firstName)) return null;
        if (Helper.isNullOrEmpty(lastName)) return null;
        if (!Helper.isValidEmail(email)) return null;
        if (Helper.isNullOrEmpty(phoneNumber)) return null;

        return new Stylist.Builder()
                .setStylistId(Helper.generateId())
                .setFirstName(firstName.trim())
                .setLastName(lastName.trim())
                .setEmail(Email.of(email))
                .setPhoneNumber(PhoneNumber.of(phoneNumber))
                .setSpeciality(Helper.isNullOrEmpty(speciality) ? null : speciality.trim())
                .setActive(true)
                .build();
    }

}
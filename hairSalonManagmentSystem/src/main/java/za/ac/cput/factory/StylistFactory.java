package za.ac.cput.factory;

import za.ac.cput.domain.Stylist;

public class StylistFactory {

    public static Stylist createStylist(int stylistId, String name, String speciality, int experienceYears) {

        if (stylistId <= 0 || name == null || name.isEmpty()) {
            return null;
        }

        return new Stylist.Builder()
                .setStylistId(stylistId)
                .setName(name)
                .setSpeciality(speciality)
                .setExperienceYears(experienceYears)
                .build();
    }
}
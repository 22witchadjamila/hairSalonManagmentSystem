/* Stylist.java
- Domain model class representing a Stylist entity with attributes and Builder pattern.
Author: Will Bryan Koeries
Student Number: 240160711
Date: 20 March 2026
*/

package za.ac.cput.domain;

public class Stylist {

    private int stylistId;
    private String name;
    private String speciality;
    private int experienceYears;

    private Stylist(Builder builder) {
        this.stylistId = builder.stylistId;
        this.name = builder.name;
        this.speciality = builder.speciality;
        this.experienceYears = builder.experienceYears;
    }

    public int getStylistId() {
        return stylistId;
    }

    public String getName() {
        return name;
    }

    public String getSpeciality() {
        return speciality;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public static class Builder {
        private int stylistId;
        private String name;
        private String speciality;
        private int experienceYears;

        public Builder setStylistId(int stylistId) {
            this.stylistId = stylistId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSpeciality(String speciality) {
            this.speciality = speciality;
            return this;
        }

        public Builder setExperienceYears(int experienceYears) {
            this.experienceYears = experienceYears;
            return this;
        }

        public Stylist build() {
            return new Stylist(this);
        }
    }
}
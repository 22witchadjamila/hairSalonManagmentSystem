/* StylistFactoryTest.java
- Tests creation of Stylist objects using StylistFactory.
Author: Will Bryan Koeries
Student Number: 240160711
Date: 20 March 2026
*/


package za.ac.cput.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Stylist;

import static org.junit.jupiter.api.Assertions.*;

class StylistFactoryTest {

    @Test
    void createStylist_valid() {
        Stylist stylist = StylistFactory.createStylist(1, "Jane Doe", "Hair Specialist", 5);

        assertNotNull(stylist);
        assertEquals(1, stylist.getStylistId());
        assertEquals("Jane Doe", stylist.getName());
        assertEquals("Hair Specialist", stylist.getSpeciality());
        assertEquals(5, stylist.getExperienceYears());
    }

    @Test
    void createStylist_invalidId() {
        Stylist stylist = StylistFactory.createStylist(0, "Jane Doe", "Hair Specialist", 5);

        assertNull(stylist);
    }

    @Test
    void createStylist_invalidName() {
        Stylist stylist = StylistFactory.createStylist(1, "", "Hair Specialist", 5);

        assertNull(stylist);
    }

    @Test
    void createStylist_nullName() {
        Stylist stylist = StylistFactory.createStylist(1, null, "Hair Specialist", 5);

        assertNull(stylist);
    }
}
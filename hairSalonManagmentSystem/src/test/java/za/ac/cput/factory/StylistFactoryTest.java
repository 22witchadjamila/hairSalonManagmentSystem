/* StylistFactoryTest.java
- Tests creation of Stylist objects using StylistFactory.
Author: Will Bryan Koeries
Student Number: 240160711
Date: 20 March 2026
*/


package za.ac.cput.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Stylist;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StylistFactory Tests")
class StylistFactoryTest {

    @Test
    @DisplayName("Should create a valid active stylist")
    void shouldCreateValidStylist() {
        Stylist stylist = StylistFactory.buildStylist(
                "Lebo", "Mokoena", "lebo@salon.com", "0711234567", "Colouring");
        assertNotNull(stylist);
        assertEquals("Lebo", stylist.getFirstName());
        assertEquals("Mokoena", stylist.getLastName());
        assertEquals("lebo@salon.com", stylist.getEmail());
        assertEquals("Colouring", stylist.getSpeciality());
        assertTrue(stylist.isActive());
        assertNotNull(stylist.getStylistId());
    }

//    @Test
//    @DisplayName("Should create stylist with bio")
//    void shouldCreateStylistWithBio() {
//        Stylist stylist = StylistFactory.buildStylistWithBio(
//                "Lebo", "Mokoena", "lebo@salon.com", "071",
//                "Colouring", "5 years experience in balayage");
//        assertNotNull(stylist);
//        assertEquals("5 years experience in balayage" );
//    }

    @Test
    @DisplayName("Should return null when first name is null")
    void shouldReturnNullWhenFirstNameIsNull() {
        Stylist stylist = StylistFactory.buildStylist(
                "", "Mokoena", "lebo@salon.com", "071", "Cuts");
        assertNull(stylist);
    }

    @Test
    @DisplayName("Should return null when email is invalid")
    void shouldReturnNullWhenEmailIsInvalid() {
        Stylist stylist = StylistFactory.buildStylist(
                "Lebo", "Mokoena", "notanemail", "071", "Cuts");
        assertNull(stylist);
    }

    @Test
    @DisplayName("Should return null when last name is blank")
    void shouldReturnNullWhenLastNameIsBlank() {
        Stylist stylist = StylistFactory.buildStylist(
                "Lebo", "   ", "lebo@salon.com", "071", "Cuts");
        assertNull(stylist);
    }

    @Test
    @DisplayName("Should generate unique IDs for each stylist")
    void shouldGenerateUniqueIds() {
        Stylist s1 = StylistFactory.buildStylist("A", "B", "a@b.com", "001", "Cuts");
        Stylist s2 = StylistFactory.buildStylist("C", "D", "c@d.com", "002", "Colour");
        assertNotNull(s1);
        assertNotNull(s2);
        assertNotEquals(s1.getStylistId(), s2.getStylistId());
    }
}
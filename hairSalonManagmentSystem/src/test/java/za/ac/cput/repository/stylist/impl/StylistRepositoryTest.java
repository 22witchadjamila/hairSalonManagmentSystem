/* StylistRepositoryTest.java
- Tests CRUD operations for StylistRepository.
Author: Will Bryan Koeries
Student Number: 240160711
Date: 26 March 2026
*/


package za.ac.cput.repository.stylist.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Stylist;
import za.ac.cput.factory.StylistFactory;

import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class StylistRepositoryTest {

    private static StylistRepository repository;
    private static Stylist stylist;

    @BeforeAll
    static void setup() {
        repository = StylistRepository.getRepository();
        stylist = StylistFactory.createStylist(1, "Jane Doe", "Hair Specialist", 5);
    }

    @Test
    void create() {
        Stylist created = repository.create(stylist);
        assertNotNull(created);
        assertEquals(stylist.getStylistId(), created.getStylistId());
    }

    @Test
    void read() {
        Stylist read = repository.read(stylist.getStylistId());
        assertNotNull(read);
        assertEquals(stylist.getStylistId(), read.getStylistId());
    }

    @Test
    void update() {
        Stylist updated = new Stylist.Builder()
                .setStylistId(stylist.getStylistId())
                .setName("Updated Name")
                .setSpeciality("Color Specialist")
                .setExperienceYears(10)
                .build();

        Stylist result = repository.update(updated);
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void delete() {
        boolean success = repository.delete(stylist.getStylistId());
        assertTrue(success);
    }

    @Test
    void getAll() {
        repository.create(stylist);
        Set<Stylist> all = repository.getAll();
        assertFalse(all.isEmpty());
    }
}
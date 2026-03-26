/* StylistRepository.java
- Handles storing and managing Stylist objects.
Author: Will Bryan Koeries
Student Number: 240160711
Date: 26 March 2026
*/

package za.ac.cput.repository.stylist.impl;

import za.ac.cput.domain.Stylist;
import za.ac.cput.repository.stylist.IStylistRepository;

import java.util.HashSet;
import java.util.Set;

public class StylistRepository implements IStylistRepository {

    private static StylistRepository repository = null;
    private final Set<Stylist> stylistDB;

    private StylistRepository() {
        stylistDB = new HashSet<>();
    }

    public static StylistRepository getRepository() {
        if (repository == null) {
            repository = new StylistRepository();
        }
        return repository;
    }

    @Override
    public Stylist create(Stylist stylist) {
        stylistDB.add(stylist);
        return stylist;
    }

    @Override
    public Stylist read(Integer id) {
        return stylistDB.stream()
                .filter(s -> s.getStylistId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Stylist update(Stylist stylist) {
        Stylist old = read(stylist.getStylistId());
        if (old != null) {
            stylistDB.remove(old);
            stylistDB.add(stylist);
            return stylist;
        }
        return null;
    }

    @Override
    public boolean delete(Integer id) {
        Stylist stylist = read(id);
        if (stylist != null) {
            stylistDB.remove(stylist);
            return true;
        }
        return false;
    }

    @Override
    public Set<Stylist> getAll() {
        return stylistDB;
    }
}
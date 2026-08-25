package za.ac.cput.service;

import za.ac.cput.domain.Stylist;

import java.util.List;

public interface IStylistService extends IService<Stylist, String> {
    Stylist register(String firstName, String lastName, String email, String phoneNumber, String speciality);
    List<Stylist> findActiveStylists();
}

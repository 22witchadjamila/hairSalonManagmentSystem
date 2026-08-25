package za.ac.cput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.Stylist;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.StylistFactory;
import za.ac.cput.repository.StylistRepository;
import za.ac.cput.service.IStylistService;

import java.util.List;

@Service
public class StylistServiceImpl implements IStylistService {

    private final StylistRepository repository;

    public StylistServiceImpl(StylistRepository repository) {
        this.repository = repository;
    }

    @Override
    public Stylist create(Stylist stylist) {
        return repository.save(stylist);
    }

    @Override
    public Stylist read(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Stylist", id));
    }

    @Override
    public Stylist update(Stylist stylist) {
        read(stylist.getStylistId());
        return repository.save(stylist);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<Stylist> getAll() {
        return repository.findAll();
    }

    @Override
    public Stylist register(String firstName, String lastName, String email, String phoneNumber, String speciality) {
        Stylist stylist = StylistFactory.buildStylist(firstName, lastName, email, phoneNumber, speciality);
        if (stylist == null) {
            throw new InvalidOperationException("Invalid stylist details provided.");
        }
        return create(stylist);
    }

    @Override
    public List<Stylist> findActiveStylists() {
        return repository.findByIsActiveTrue();
    }
}

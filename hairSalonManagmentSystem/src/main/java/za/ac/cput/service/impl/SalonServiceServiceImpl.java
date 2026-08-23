package za.ac.cput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.enums.ServiceCategory;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.SalonServiceFactory;
import za.ac.cput.repository.SalonServiceRepository;
import za.ac.cput.service.ISalonServiceService;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SalonServiceServiceImpl implements ISalonServiceService {

    private final SalonServiceRepository repository;

    public SalonServiceServiceImpl(SalonServiceRepository repository) {
        this.repository = repository;
    }

    @Override
    public SalonService create(SalonService salonService) {
        return repository.save(salonService);
    }

    @Override
    public SalonService read(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("SalonService", id));
    }

    @Override
    public SalonService update(SalonService salonService) {
        read(salonService.getServiceId());
        return repository.save(salonService);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<SalonService> getAll() {
        return repository.findAll();
    }

    @Override
    public SalonService register(String name, String description, int durationMinutes,
                                 BigDecimal price, ServiceCategory category) {
        SalonService salonService = SalonServiceFactory.buildService(
                name, description, durationMinutes, price, category);
        if (salonService == null) {
            throw new InvalidOperationException("Invalid service details provided.");
        }
        return create(salonService);
    }

    @Override
    public List<SalonService> findByCategory(ServiceCategory category) {
        return repository.findByCategory(category);
    }

    @Override
    public List<SalonService> findActiveServices() {
        return repository.findByIsActiveTrue();
    }

}

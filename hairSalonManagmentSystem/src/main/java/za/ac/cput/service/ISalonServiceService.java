package za.ac.cput.service;

import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.enums.ServiceCategory;

import java.math.BigDecimal;
import java.util.List;

public interface ISalonServiceService extends IService<SalonService,String> {
    SalonService register(String name, String description, int durationMinutes,
                          BigDecimal price, ServiceCategory category);

    List<SalonService> findByCategory(ServiceCategory category);
    List<SalonService> findActiveServices();
}

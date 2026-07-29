package za.ac.cput.factory;

import za.ac.cput.domain.Service;
/*ServiceFactory.java
 Service Factory  class
 Author: Witcha Francisco (222894822)
 Date: 26/03/2026
*/

import za.ac.cput.domain.Service;
import za.ac.cput.domain.enums.ServiceCategory;

import java.math.BigDecimal;
import java.util.UUID;

public class ServiceFactory {

    public static Service buildService(String name, String description,
                                       int durationMinutes, BigDecimal price,
                                       ServiceCategory category) {
        if (name == null || name.isBlank()) return null;
        if (durationMinutes <= 0) return null;
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) return null;
        if (category == null) return null;

        return new Service.Builder()
                .setServiceId(UUID.randomUUID().toString())
                .setName(name.trim())
                .setDescription(description)
                .setDurationMinutes(durationMinutes)
                .setPrice(price)
                .setCategory(category)
                .setActive(true)
                .build();
    }
}


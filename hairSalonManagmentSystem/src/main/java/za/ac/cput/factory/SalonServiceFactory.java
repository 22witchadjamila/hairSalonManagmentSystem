package za.ac.cput.factory;

/*SalonServiceFactory.java
 SalonService Factory  class
 Author: Witcha Francisco (222894822)
 Date: 26/03/2026
*/

import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.enums.ServiceCategory;
import za.ac.cput.domain.valueobject.Money;
import za.ac.cput.util.Helper;

import java.math.BigDecimal;

public class SalonServiceFactory {

    public static SalonService buildService(String name, String description,
                                       int durationMinutes, BigDecimal price,
                                       ServiceCategory category) {
        if (Helper.isNullOrEmpty(name)) return null;
        if (!Helper.isPositiveInteger(durationMinutes)) return null;
        if (!Helper.isValidAmount(price)) return null;
        if (category == null) return null;

        return new SalonService.Builder()
                .setServiceId(Helper.generateId())
                .setName(name.trim())
                .setDescription(description)
                .setDurationMinutes(durationMinutes)
                .setPrice(Money.of(price))
                .setCategory(category)
                .setActive(true)
                .build();
    }
}

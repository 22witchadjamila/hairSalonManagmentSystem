package za.ac.cput.domain;
/*Service.java
 Service  class
 Author: Witcha Francisco (222894822)
 Date: 23/03/2026
*/
import jakarta.persistence.*;
import za.ac.cput.domain.enums.ServiceCategory;
import za.ac.cput.domain.valueobject.Money;

import java.math.BigDecimal;

@Entity
@Table(name = "services")
public class SalonService {

    @Id
    private String serviceId;
    private String name;
    private String description;
    private int durationMinutes;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "price"))
    private Money price;

    @Enumerated(EnumType.STRING)
    private ServiceCategory category;

    private boolean isActive;

    protected SalonService() {}

    public SalonService(Builder builder) {
        this.serviceId       = builder.serviceId;
        this.name            = builder.name;
        this.description     = builder.description;
        this.durationMinutes = builder.durationMinutes;
        this.price          = builder.price;
        this.category        = builder.category;
        this.isActive        = builder.isActive;
    }

    public String getServiceId()         { return serviceId; }
    public String getName()              { return name; }
    public String getDescription()       { return description; }
    public int getDurationMinutes()      { return durationMinutes; }
    public Money getPrice()              {return price;}
    public ServiceCategory getCategory() { return category; }
    public boolean isActive()            { return isActive; }

    public static class Builder {
        private String serviceId;
        private String name;
        private String description;
        private int durationMinutes;
        private Money price;
        private ServiceCategory category;
        private boolean isActive;

        public Builder setServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }
        public Builder setDurationMinutes(int durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public Builder setPrice(Money price) {
            this.price = price;
            return this;
        }

        public Builder setCategory(ServiceCategory category) {
            this.category = category;
            return this;
        }
        public Builder setActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        public SalonService build() {
            return new SalonService(this);
        }
    }
}

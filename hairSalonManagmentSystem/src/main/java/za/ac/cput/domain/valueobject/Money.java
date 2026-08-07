package za.ac.cput.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

/** A non-negative monetary amount. Cannot exist in an invalid (null/negative) state. */
@Embeddable
public class Money {

    private BigDecimal value;

    protected Money() {}

    private Money(BigDecimal value) {
        this.value = value;
    }

    public static Money of(BigDecimal raw) {
        if (raw == null || raw.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money amount must be non-negative: "+ raw);
        }
        return new Money(raw);
    }

    @JsonValue
    public BigDecimal getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof Money money)) return false;
        return value != null && money.value != null && value.compareTo(money.value) == 0;
    }

    @Override
    public int hashCode() {
        return value == null ? 0 : value.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

package za.ac.cput.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Embeddable;
import za.ac.cput.util.Helper;

import java.util.Objects;

/** A phone number. Cannot exist in an invalid (blank) state. */
@Embeddable
public class PhoneNumber {

    private String value;

    protected PhoneNumber() {}

    private PhoneNumber(String value) {
        this.value = value;
    }

    public static PhoneNumber of(String raw) {
        if (Helper.isNullOrEmpty(raw)) {
            throw new IllegalArgumentException("Phone number cannot be blank.");
        }
        return new PhoneNumber(raw.trim());
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

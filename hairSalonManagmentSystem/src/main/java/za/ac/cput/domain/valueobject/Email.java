package za.ac.cput.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonValue;
import za.ac.cput.util.Helper;

import java.util.Objects;

public class Email {

    private String value;

    protected Email(){}

    private Email(String value){
        this.value = value;
    }

    public static Email of(String raw){
        if (!Helper.isValidEmail(raw)){
            throw new IllegalArgumentException("Invalid email address: "+ raw);
        }
        return new Email(raw.trim().toLowerCase());
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof Email email)) return false;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode(){
        return Objects.hash(value);
    }

    @Override
    public String toString(){
        return value;
    }
}

package za.ac.cput.domain;

public class Customer {
    private String customerId;
    private String name;
    private int phoneNumber;
    private String email;

    private Customer(){}

    private Customer(Builder builder){
        this.customerId = builder.customerId;
        this.name = builder.name;
        this.phoneNumber = builder.phoneNumber;
        this.email = builder.email;
    }
    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId='" + customerId + '\'' +
                ", name='" + name + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", email='" + email + '\'' +
                '}';
    }

    public static class Builder{
        private String customerId;
        private String name;
        private int phoneNumber;
        private String email;

        public Builder setCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setPhoneNumber(int phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Customer build(){
            return new Customer(this);
        }
    }
}

package za.ac.cput.domain;

public class Service{
    private String serviceId;
    private String serviceName;
    private double price;
    private int duration;

    // builder constructor
    private Service(Builder builder) {
        this.serviceId = builder.serviceId;
        this.serviceName = builder.serviceName;
        this.price = builder.price;
        this.duration = builder.duration;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceName() {

        return serviceName;
    }

    public double getPrice() {

        return price;
    }



    public int getDuration() {

        return duration;
    }
    @Override
    public String toString() {
        return "Service{" +
                "serviceId='" + serviceId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", price=" + price +
                ", duration=" + duration +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Service)) return false;
        Service service = (Service) o;
        return serviceId != null && serviceId.equals(service.serviceId);
    }

    @Override
    public int hashCode() {
        return serviceId != null ? serviceId.hashCode() : 0;
    }

    public static class Builder {
        private String serviceId;
        private String serviceName;
        private double price;
        private int duration;


        public Builder setServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder setServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder setPrice(double price) {
            this.price = price;
            return this;
        }

        public Builder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Service build() {
            return new Service(this);
        }
    }
}

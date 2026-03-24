package za.ac.cput.domain;

public class Appointment {

    private String appointmentId;
    private String date;
    private String time;
    private String clientId;
    private String employeeId;
    private String serviceId;

    private Appointment(Builder builder) {
        this.appointmentId = builder.appointmentId;
        this.date = builder.date;
        this.time = builder.time;
        this.clientId = builder.clientId;
        this.employeeId = builder.employeeId;
        this.serviceId = builder.serviceId;
    }

    // Getters
    public String getAppointmentId() { return appointmentId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getClientId() { return clientId; }
    public String getEmployeeId() { return employeeId; }
    public String getServiceId() { return serviceId; }

    // Builder class
    public static class Builder {
        private String appointmentId;
        private String date;
        private String time;
        private String clientId;
        private String employeeId;
        private String serviceId;

        public Builder setAppointmentId(String appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }

        public Builder setDate(String date) {
            this.date = date;
            return this;
        }

        public Builder setTime(String time) {
            this.time = time;
            return this;
        }

        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder setServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Appointment build() {
            return new Appointment(this);
        }
    }
}
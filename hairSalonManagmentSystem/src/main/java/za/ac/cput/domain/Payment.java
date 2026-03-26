/* Payment.java
   Payment model class
   Author: Reece Josephs [218152701]
   Date: [23 March 2025]
*/

package za.ac.cput.domain;

public class Payment {
    private String paymentId;
    private double amount;
    private String paymentMethod;

    private Payment() {}

    private Payment(Builder builder) {
        this.paymentId = builder.paymentId;
        this.amount = builder.amount;
        this.paymentMethod = builder.paymentMethod;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", amount=" + amount +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }


    public static class Builder {
        private String paymentId;
        private double amount;
        private String paymentMethod;

        public Builder setPaymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder setAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }
}

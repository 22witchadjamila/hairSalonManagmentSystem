

package za.ac.cput.domain;

import jakarta.persistence.*;
import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.domain.enums.PaymentStatus;
import za.ac.cput.domain.valueobject.Money;

import java.time.LocalDateTime;

@Entity
@Table(name="payments")
public class Payment {

    @Id
    private String paymentId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "amount"))
    private Money amount;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "discount"))
    private Money discount;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "final_amount"))
    private Money finalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime paidAt;
    private String transactionRef;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    protected Payment(){}

    public Payment(Builder builder){
        this.paymentId      = builder.paymentId;
        this.amount        = builder.amount;
        this.discount        = builder.discount;
        this.finalAmount    = builder.finalAmount;
        this.method         = builder.method;
        this.status         = builder.status;
        this.paidAt         = builder.paidAt;
        this.transactionRef = builder.transactionRef;
        this.appointment    = builder.appointment;
        this.promotion      = builder.promotion;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public Money getAmount() {return amount;}

    public Money getDiscount() {return discount;}

    public Money getFinalAmount() {return finalAmount;}

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public PaymentMethod getMethod() {return method;}

    public PaymentStatus getStatus() {return status;}

    public Appointment getAppointment() {return appointment;}

    public Promotion getPromotion() {return promotion;}

    public static class Builder{
        private String paymentId;
        private Money amount;
        private Money discount;
        private Money finalAmount;
        private PaymentMethod method;
        private PaymentStatus status;
        private LocalDateTime paidAt;
        private String transactionRef;
        private Appointment appointment;
        private Promotion promotion;

        public Builder setPaymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder setAmount(Money amount) {
            this.amount = amount;
            return this;
        }

        public Builder setDiscount(Money discount) {
            this.discount = discount;
            return this;
        }

            public Builder setFinalAmount(Money finalAmount) {
            this.finalAmount = finalAmount;
            return this;
        }

        public Builder setPaidAt(LocalDateTime paidAt) {
            this.paidAt = paidAt;
            return this;
        }

        public Builder setTransactionRef(String transactionRef) {
            this.transactionRef = transactionRef;
            return this;
        }

        public Builder setMethod(PaymentMethod method) {
            this.method = method;
            return this;
        }

        public Builder setStatus(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder setAppointment(Appointment appointment) {
            this.appointment = appointment;
            return this;
        }

        public Builder setPromotion(Promotion promotion) {
            this.promotion = promotion;
            return this;
        }

        public Payment build(){
            return new Payment(this);
        }
    }
}

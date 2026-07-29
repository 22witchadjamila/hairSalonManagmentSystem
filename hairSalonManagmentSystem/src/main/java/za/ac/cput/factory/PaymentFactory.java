/* PaymentFactory.java
   Factory class for creating Payment domain objects
   Author: Reece Josephs [218152701]
   Date: [23 March 2025]
*/

package za.ac.cput.factory;

import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Payment;
import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentFactory {

    public static Payment buildPayment(Appointment appointment,
                                       BigDecimal amount,
                                       PaymentMethod method) {
        if (appointment == null) return null;
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) return null;
        if (method == null) return null;

        return new Payment.Builder()
                .setPaymentId(UUID.randomUUID().toString())
                .setAppointment(appointment)
                .setAmount(amount)
                .setDiscount(BigDecimal.ZERO)
                .setFinalAmount(amount)
                .setMethod(method)
                .setStatus(PaymentStatus.PENDING)
                .build();
    }

    public static Payment buildPaymentWithPromotion(Appointment appointment,
                                                    BigDecimal amount,
                                                    BigDecimal discount,
                                                    PaymentMethod method,
                                                    Promotion promotion){
        if (appointment == null || amount == null || method == null) return null;
        BigDecimal finalAmount = amount.subtract(discount != null ? discount : BigDecimal.ZERO);

        return new Payment.Builder()
                .setPaymentId(UUID.randomUUID().toString())
                .setAppointment(appointment)
                .setAmount(amount)
                .setDiscount(BigDecimal.ZERO)
                .setFinalAmount(amount)
                .setMethod(method)
                .setStatus(PaymentStatus.PENDING)
                .build();
    }
}

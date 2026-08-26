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
import za.ac.cput.domain.valueobject.Money;
import za.ac.cput.util.Helper;

import java.math.BigDecimal;

public class PaymentFactory {

    public static Payment buildPayment(Appointment appointment,
                                       BigDecimal amount,
                                       PaymentMethod method) {
        if (appointment == null) return null;
        if (!Helper.isValidAmount(amount)) return null;
        if (method == null) return null;

        return new Payment.Builder()
                .setPaymentId(Helper.generateId())
                .setAppointment(appointment)
                .setAmount(Money.of(amount))
                .setDiscount(Money.of(BigDecimal.ZERO))
                .setFinalAmount(Money.of(amount))
                .setMethod(method)
                .setStatus(PaymentStatus.PENDING)
                .setTransactionRef(Helper.generateTransactionReference())
                .build();
    }

    public static Payment buildPaymentWithPromotion(Appointment appointment,
                                                    BigDecimal amount,
                                                    PaymentMethod method,
                                                    Promotion promotion){
        if (appointment == null || method == null) return null;
        if (!Helper.isValidAmount(amount)) return null;

        BigDecimal discount = promotion == null ? BigDecimal.ZERO
                : Helper.calculateDiscount(amount, promotion.getDiscountType(), promotion.getDiscountValue());
        BigDecimal finalAmount = Helper.calculateFinalAmount(amount, discount);

        return new Payment.Builder()
                .setPaymentId(Helper.generateId())
                .setAppointment(appointment)
                .setAmount(Money.of(amount))
                .setDiscount(Money.of(discount))
                .setFinalAmount(Money.of(finalAmount))
                .setMethod(method)
                .setStatus(PaymentStatus.PENDING)
                .setTransactionRef(Helper.generateTransactionReference())
                .setPromotion(promotion)
                .build();
    }
}

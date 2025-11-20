package com.kickboard.domain.payment;

import com.kickboard.domain.rental.Payment;

import java.math.BigDecimal;

/* CreditCardPayment.java on 25/11/20, by BeomYeon Choi, cby9017@gmail.com*/
// 신용카드 결제 클래스 - Concrete Product
public class CreditCardPayment extends Payment {
    public CreditCardPayment(String paymentId, String rentalId, PaymentMethod method) {
        super(paymentId, rentalId, method);
    }

    // 신용카드만의 결제 로직
}

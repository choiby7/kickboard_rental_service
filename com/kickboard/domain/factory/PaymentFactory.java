package com.kickboard.domain.factory;

import com.kickboard.domain.rental.Payment;
import com.kickboard.domain.payment.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentFactory {
    // 1. 결제 수단 정보 객체 생성 (입력값을 받아 구체적인 Method 반환)
    PaymentMethod createPaymentMethod(String cardNumber, String cvc, String alias);

    // 2. 실제 결제를 수행할 객체 생성 (위에서 만든 Method와 금액을 주입)
    Payment createPayment(PaymentMethod method, BigDecimal amount, String rentalId);
}
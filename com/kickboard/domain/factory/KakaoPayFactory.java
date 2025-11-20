package com.kickboard.domain.factory;

import com.kickboard.domain.payment.*;
import com.kickboard.domain.rental.Payment;
import com.kickboard.domain.payment.PaymentMethod;

import java.math.BigDecimal;

public class KakaoPayFactory implements PaymentFactory {

    @Override
    public PaymentMethod createPaymentMethod(String identifier, String password, String alias) {
        return new KakaoPayMethod(identifier, password, alias);
    }

    @Override
    // rentalId를 이용해 고유한 결제 ID 생성 (카카오페이 결제 - "PAY-K-")
    public Payment createPayment(PaymentMethod method, BigDecimal amount, String rentalId) {
        if (!(method instanceof KakaoPayMethod)) {
            throw new IllegalArgumentException("카카오페이 결제에는 카카오페이 정보가 필요합니다.");
        }

        return new KakaoPayment("PAY-K-" + rentalId, rentalId, method);
    }
}
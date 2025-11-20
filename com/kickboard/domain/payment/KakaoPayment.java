package com.kickboard.domain.payment;

import com.kickboard.domain.rental.Payment;

import java.math.BigDecimal;

/* KakaoPayment.java on 25/11/20, by BeomYeon Choi, cby9017@gmail.com*/
// 카카오페이 결제 클래스 - Concrete Product
public class KakaoPayment extends Payment {
    public KakaoPayment(String paymentId, String rentalId, PaymentMethod method) {
        super(paymentId, rentalId, method);
    }

    // 카카오페이의 결제 로직 - 예: 카카오페이 API 연동 등
}

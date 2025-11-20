package com.kickboard.domain.factory;

import com.kickboard.domain.payment.CreditCardMethod;
import com.kickboard.domain.payment.CreditCardPayment;
import com.kickboard.domain.rental.Payment;
import com.kickboard.domain.payment.PaymentMethod;

import java.math.BigDecimal;

public class CreditCardFactory implements PaymentFactory {

    @Override
    public PaymentMethod createPaymentMethod(String cardNumber, String cvc, String alias) {
        return new CreditCardMethod(cardNumber, cvc, alias);
    }


    @Override
    // rentalId를 이용해 고유한 결제 ID 생성 (신용카드 결제 - "PAY-C-")
    public Payment createPayment(PaymentMethod method, BigDecimal amount, String rentalId) {
        // 카드 결제 처리를 담당하는 CreditCardPayment 생성
        // 안전장치: 만약 method가 CreditCardMethod가 아니면 에러 발생
        if (!(method instanceof CreditCardMethod)) {
            throw new IllegalArgumentException("신용카드 결제에는 신용카드 정보가 필요합니다.");
        }

        return new CreditCardPayment("PAY-C-" + rentalId, rentalId, method);
    }
}
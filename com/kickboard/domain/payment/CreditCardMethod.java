package com.kickboard.domain.payment;

/* CreditCardMethod.java on 25/11/20, by BeomYeon Choi, cby9017@gmail.com*/

// 신용카드 결제 수단 정보 클래스 - Concrete PaymentMethod
public class CreditCardMethod extends PaymentMethod {
    public CreditCardMethod(String cardNumber, String cvc, String alias) {
        super(cardNumber, cvc, alias, PaymentMethodType.CREDIT_CARD);
    }
}

package com.kickboard.domain.payment;


/* KakaoPayMethod.java on 25/11/20, by BeomYeon Choi, cby9017@gmail.com*/
// 카카오페이 결제 수단 정보 클래스 - Concrete PaymentMethod
public class KakaoPayMethod extends PaymentMethod {
    // 카카오페이 결제 수단 정보 (이름과 간편비밀번호)
    public KakaoPayMethod(String cardNumber, String cvc, String alias) {
        super(cardNumber, cvc, alias, PaymentMethodType.KAKAO_PAY);
    }
}


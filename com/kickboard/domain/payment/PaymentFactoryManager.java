package com.kickboard.domain.payment;

import com.kickboard.domain.factory.CreditCardFactory;
import com.kickboard.domain.factory.KakaoPayFactory;
import com.kickboard.domain.factory.PaymentFactory;

import java.util.HashMap;
import java.util.Map;

/* PaymentFactoryManager.java on 25/11/20, by BeomYeon Choi, cby9017@gmail.com*/
public class PaymentFactoryManager {
    // 미리 공장들을 등록해둠 (Map 활용)
    private static final Map<PaymentMethodType, PaymentFactory> factoryMap = new HashMap<>();

    static {
        factoryMap.put(PaymentMethodType.CREDIT_CARD, new CreditCardFactory());
        factoryMap.put(PaymentMethodType.KAKAO_PAY, new KakaoPayFactory());
    }

    // 타입을 주면 적절한 공장을 꺼내줌
    public static PaymentFactory getFactory(PaymentMethodType type) {
        return factoryMap.get(type);
    }
}
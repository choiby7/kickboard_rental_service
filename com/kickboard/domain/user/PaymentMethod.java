package com.kickboard.domain.user;

import java.io.Serializable;

/**
 * class PaymentMethod.java
 * @author: Choi BeomYeon
 * @email: cby9017@naver.com
 * @version: 1.0
 * @date: 2025.10.8
 */
public class PaymentMethod implements Serializable{ // implements Seriallizable 추가
    private final String cardNumber;
    private final String cvc;
    private String alias; // 결제수단 별칭 추가

    public PaymentMethod(String cardNumber, String cvc, String alias) {
        this.cardNumber = cardNumber;
        this.cvc = cvc;
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCardNumber() {
        return this.cardNumber;
    }

    public String getCvc() {
        return this.cvc;
    }
}

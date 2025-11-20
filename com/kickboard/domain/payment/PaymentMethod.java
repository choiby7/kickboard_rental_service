package com.kickboard.domain.payment;

import java.io.Serializable;
import java.math.BigDecimal; 
import java.util.Random; 

/**
 * class PaymentMethod.java
 * @author: Choi BeomYeon
 * @email: cby9017@naver.com
 * @version: 1.1
 * @date: 2025.10.8
 * @modified: PaymentMethodType 추가, Serializable 구현, Abstract Factory 패턴 도입을 위해 추상클래스(Abstarct Prouduct)로 변경
 */

public abstract class PaymentMethod implements Serializable{ // implements Seriallizable 추가
    private final String identifier; // 범용적인 사용을 위해 결제수단의 식별자로 변경 (카드번호, 휴대폰번호 등)
    private String password; // 결제수단의 비밀번호 (CVC, 간편비밀번호 등, 필요에 따라 null 가능)
    private String alias; // 결제수단 별칭 추가
    // 결제수단 타입 추가 (0-신용카드, 1-카카오페이)
    private PaymentMethodType type;
    private BigDecimal balance; // 결제수단 잔액

    protected PaymentMethod(String id, String password, String alias, PaymentMethodType type) {
        this.identifier = id;
        this.password = password;
        this.alias = alias;
        this.type = type;
        this.balance = BigDecimal.valueOf(new Random().nextInt(16) + 5)
                            .multiply(BigDecimal.valueOf(1000)); // 5000~20000 사이 임의의 잔액 설정
    }

    public void deductBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getPassword() {
        return this.password;
    }

    public PaymentMethodType getType() {
        return type;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }
}

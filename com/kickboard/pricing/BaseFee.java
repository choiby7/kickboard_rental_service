package com.kickboard.pricing;

import java.math.BigDecimal;

/**
 * BaseFee.java		: 기본 요금을 나타내는 클래스. Fee 인터페이스의 기본 구현.
 * @author		: Gemini
 * @version		: 1.0
 * @date		: 2025.10.11
 */
public class BaseFee implements Fee {

    private final BigDecimal amount;

    public BaseFee(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }
        this.amount = amount;
    }

    @Override
    public BigDecimal getFinalCost() {
        return this.amount;
    }

    @Override // getDisplayName 추가
    public String getDisplayName() {
        return "__BaseFee__";
    }
}

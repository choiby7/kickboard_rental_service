package com.kickboard.domain.pricing.discount;

import java.math.BigDecimal;
import com.kickboard.domain.pricing.Fee;

/**
 * PromotionDecorator.java	: 요금 할인 및 프로모션 적용을 위한 추상 데코레이터
 * @author	: Mingwan Kim
 * @email	: steven3407115@dankook.ac.kr
 * @version	: 1.0
 * @date	: 2025.10.07
 */
public abstract class PromotionDecorator implements Fee {

    // 데코레이션 대상 요금 객체
    protected final Fee decoratedFee;

    /**
     * 생성자: 감쌀 요금 객체를 전달받음
     * @param decoratedFee - 할인 적용 대상 Fee 객체
     */
    protected PromotionDecorator(Fee decoratedFee) {
        this.decoratedFee = decoratedFee;
    }

    /**
     * 최종 결제 금액 반환 (기본적으로 내부 Fee의 금액 그대로 반환)
     * 서브클래스에서 오버라이드하여 할인 로직 구현
     *
     * @return 최종 결제 금액 (BigDecimal)
     */
    @Override
    public BigDecimal getFinalCost() {
        return decoratedFee.getFinalCost();
    }

    @Override // 할인 정보 출력
    public String getDisplayName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 객체를 감싸기 위한 decorate 메서드
     */
    public Fee decorate(Fee fee) {
        try {
            return this.getClass()
                       .getConstructor(Fee.class, this.getClass())
                       .newInstance(fee, this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decorate fee", e);
        }
    }
}

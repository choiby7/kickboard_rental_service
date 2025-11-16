package com.kickboard.domain.pricing.discount;

import java.math.BigDecimal;
import com.kickboard.domain.pricing.Fee;

/**
 * DistanceDiscountDecorator.java	: 거리 비례 할인 적용 데코레이터
 * @author	: Minsu Kim
 * @email	: minsk0515@dankook.ac.kr
 * @version	: 1.0
 * @date	: 2025.11.16
 */

public class DistanceDiscountDecorator extends PromotionDecorator {

    //@param discountRate - 할인율
    private final BigDecimal discountRate; 
    //thresholdKm - 거리
    private final double thresholdKm;      

    /**
     * 생성자
     * @param decoratedFee - 할인 적용 대상 Fee 객체
     */
    public DistanceDiscountDecorator(Fee decoratedFee,
                                     double thresholdKm,
                                     BigDecimal discountRate) {
        super(decoratedFee);
        this.thresholdKm = thresholdKm;
        this.discountRate = discountRate;
    }

    // 복제 생성자 (Promotion 적용 시 필요)
    public DistanceDiscountDecorator(Fee decoratedFee, DistanceDiscountDecorator original) {
        this(decoratedFee, original.thresholdKm, original.discountRate);
    }

    /**
     * 최종 결제 금액 반환
     * 기본 요금에서 할인율을 적용한 금액을 반환
     * @return BigDecimal - 할인 적용 후 최종 금액
     */
    @Override
    public BigDecimal getFinalCost() {
        BigDecimal original = decoratedFee.getFinalCost();
        BigDecimal discount = original.multiply(discountRate);
        return original.subtract(discount);
    }

    @Override // 거리 할인 정보 자동 출력
    public String getDisplayName() {
        return String.format("거리 할인 (%.0fkm 이상 %.0f%%)",
                thresholdKm,
                discountRate.multiply(BigDecimal.valueOf(100)));
    }
}

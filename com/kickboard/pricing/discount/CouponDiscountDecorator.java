package com.kickboard.pricing.discount;


import java.math.BigDecimal;
import com.kickboard.pricing.Fee;

/**
 * CouponDiscountDecorator.java	: 쿠폰 사용 시 할인 적용 데코레이터 - PromotionDecorator를 상속받아 쿠폰 할인을 적용
 * @author	: Minsu Kim
 * @email	: minsk0515@dankook.ac.kr
 * @version	: 1.0
 * @date	: 2025.11.10
 */
public class CouponDiscountDecorator extends PromotionDecorator {

    //couponName - 쿠폰 이름
    private final String couponName;
	//couponId - 쿠폰ID
    private final String couponId;
    //@param discountRate - 할인율
    private final BigDecimal discountRate;

    /**
     * 생성자
     * @param decoratedFee - 할인 적용 대상 Fee 객체
     */
    public CouponDiscountDecorator(Fee decoratedFee, String couponName, String couponId, BigDecimal discountRate) {
        super(decoratedFee);  //부모(PromotionDecorator)의 생성자 호출
        this.couponName = couponName;
        this.couponId = couponId;
        this.discountRate = discountRate;
    }

    // 복제 생성자
    public CouponDiscountDecorator(Fee decoratedFee, CouponDiscountDecorator original) {
        this(decoratedFee, original.couponName, original.couponId, original.discountRate);
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

    public String getCouponId() {
        return couponId;
    }

    @Override // 할인 정보 출력
    public String getDisplayName() {
        return "쿠폰 할인 (" + couponName + ")";
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }
}

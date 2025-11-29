package com.kickboard.domain.pricing.discount;


import java.math.BigDecimal;
import com.kickboard.domain.pricing.Fee;

/**
 * CardDiscountDecorator.java	: 카드 결제 시 할인 적용 데코레이터 - PromotionDecorator를 상속받아 카드사별 할인율을 적용
 * @author	: Mingwan Kim
 * @email	: steven3407115@dankook.ac.kr
 * @version	: 1.0
 * @date	: 2025.10.07
 */
public class CardDiscountDecorator extends PromotionDecorator {

	//@param cardCompany - 카드사 이름
    private final String cardCompany;
    //@param discountRate - 할인율
    private final BigDecimal discountRate;

    /**
     * 생성자
     * @param decoratedFee - 할인 적용 대상 Fee 객체
     */
    public CardDiscountDecorator(Fee decoratedFee, String cardCompany, BigDecimal discountRate) {
        super(decoratedFee);  //부모(PromotionDecorator)의 생성자 호출
        this.cardCompany = cardCompany;
        this.discountRate = discountRate;
    }

    // 복제 생성자 (decorator 과정에 필요)
    public CardDiscountDecorator(Fee decoratedFee, CardDiscountDecorator original) {
        this(decoratedFee, original.cardCompany, original.discountRate);
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

    @Override // getCardCompany -> 상위(fee)에서 정의된 getDisplayName으로 대체
    public String getDisplayName() {
        return cardCompany + " 카드 할인";
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }
}

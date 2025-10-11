/**
 * FeeStrategy.java	: 요금 계산 전략 인터페이스 - 시간 기반, 거리 기반 방식을 정의한다.
 * @author	: Mingwan Kim
 * @email	: steven3407115@dankook.ac.kr
 * @version	: 1.0
 * @date	: 2025.10.07
 */
package com.kickboard.pricing.strategy;

import com.kickboard.domain.rental.RentalInfo;
import java.math.BigDecimal;

public interface FeeStrategy {

    BigDecimal calculateFee(com.kickboard.domain.rental.Rental rental);

    String name();
}
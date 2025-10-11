package com.kickboard.pricing.strategy;

import com.kickboard.domain.rental.Rental;
import com.kickboard.domain.rental.RentalInfo;
import java.math.BigDecimal;

/**
 * DistanceFeeStrategy.java : 거리 기반 요금 계산 전략 (1km당 ???원)
 * @author	: Mingwan Kim
 * @email	: steven3407115@dankook.ac.kr
 * @version	: 1.1
 * @date	: 2025.10.11
 */
public class DistanceFeeStrategy implements FeeStrategy {

    // Km당 요금 - 200원 가정
    private static final BigDecimal ratePerKilometer = new BigDecimal("200");

    /**
     * 거리 기반 요금 계산 (Km당 요금 × 거리)
     * @param rental 대여 정보 객체
     * @return 총 요금 (BigDecimal)
     */
    @Override
    public BigDecimal calculateFee(Rental rental) {
        // Rental 객체에서 RentalInfo를 가져와야 합니다.
        // Rental 클래스에 getRentalInfo() 메서드가 필요합니다.
        double distanceKm = rental.getRentalInfo().getTraveledDistance();
        return ratePerKilometer.multiply(BigDecimal.valueOf(distanceKm));
    }

    /**
     * 전략 이름 반환
     * 
     * @return "Distance-based"
     */
    @Override
    public String name() {
        return "Distance-based";
    }

}

package com.kickboard.domain.pricing.strategy;

import com.kickboard.domain.rental.Rental;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * TimeFeeStrategy.java : 시간 기반 요금 계산 전략 (분당 ???원)
 * @author	: Mingwan Kim
 * @email	: steven3407115@dankook.ac.kr
 * @version	: 1.1
 * @date	: 2025.10.11
 */
public class TimeFeeStrategy implements FeeStrategy {

    // 분당 요금 - 200원 가정
    private static final BigDecimal RATE_PER_MINUTE = new BigDecimal("200");

    /**
     * 시간 기반 요금 계산 (분당 요금 × 이용시간)
     * @param rental 대여 정보 객체
     * @return 총 요금
     */
    @Override
    public BigDecimal calculateFee(Rental rental) {
        LocalDateTime actualEndTime = rental.getEndTime();
        if (actualEndTime == null) {
            actualEndTime = LocalDateTime.now(); // 아직 반납 전이면 현재 시간을 기준으로 계산
        }
        long minutes = Duration.between(rental.getStartTime(), actualEndTime).toMinutes();
        return RATE_PER_MINUTE.multiply(BigDecimal.valueOf(minutes));
    }

    /**
     * 전략 이름 반환
     * @return "Time-based"
     */
    @Override
    public String name() {
        return "Time-based";
    }
}


package pricing.strategy;

import domain.rental.RentalInfo;
import java.math.BigDecimal;
import java.time.Duration;

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
     * @param rentalInfo 대여 정보 DTO
     * @return 총 요금
     */
    @Override
    public BigDecimal calculateFee(RentalInfo rentalInfo) {
        if (rentalInfo.getEndTime() == null) {
            return BigDecimal.ZERO; // 아직 운행 종료 전이면 0원
        }
        long minutes = Duration.between(rentalInfo.getStartTime(), rentalInfo.getEndTime()).toMinutes();
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

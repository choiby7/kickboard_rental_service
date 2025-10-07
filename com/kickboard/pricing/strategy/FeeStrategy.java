package pricing.strategy;

import java.math.BigDecimal;

/**
 * FeeStrategy.java	: 요금 계산 전략 인터페이스 - 시간 기반, 거리 기반 방식을 정의한다.
 * @author	: Mingwan Kim
 * @email	: steven3407115@dankook.ac.kr
 * @version	: 1.0
 * @date	: 2025.10.07
 */
public interface FeeStrategy {

    /**
     * 요금 계산 메서드
     * @return 계산된 요금 (BigDecimal)
     */
    BigDecimal calculateFee();

    /**
     * 전략 이름 반환 ("Time-based", "Distance-based")
     * @return 전략 이름 (String)
     */
    String name();
}

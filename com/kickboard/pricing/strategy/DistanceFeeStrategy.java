import java.math.BigDecimal;

/**
 * DistanceFeeStrategy.java : 거리 기반 요금 계산 전략 (1km당 ???원)
 * @author	: Mingwan Kim
 * @email	: steven3407115@dankook.ac.kr
 * @version	: 1.0
 * @date	: 2025.10.07
 */
public class DistanceFeeStrategy implements FeeStrategy {

    // Km당 요금 - 150원 가정
    private static final BigDecimal ratePerKilometer = new BigDecimal("200"); //수정 필요 - 일단 200원 가정

    // 이동 거리 (km)
    private final double distanceKm;

    /**
     * 생성자
     * @param distanceKm 이동 거리(km)
     */
    public DistanceFeeStrategy(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    /**
     * 거리 기반 요금 계산 (Km당 요금 × 거리)
     * @return 총 요금 (BigDecimal)
     */
    @Override
    public BigDecimal calculateFee() {
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

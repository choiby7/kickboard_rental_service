package pricing;

import java.math.BigDecimal;

/**
 * Fee.java			: 할인, 추가 요금 등이 모두 반영된최종 결제 금액을 반환
 * @author			: Mingwan Kim
 * @email			: steven3407115@dankook.ac.kr
 * @version			: 1.0
 * @date			: 2025.10.07
 */
public interface Fee {

	//최종 결제 금액을 반환
    BigDecimal getFinalCost();
}

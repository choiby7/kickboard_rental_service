package domain.rental;

import domain.user.User;
import domain.vehicle.Vehicle;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Rental {

    /**
    * Rental.java	: Rental 초기 구현
    * @author	: Minsu Kim
    * @email	: minsk05151@gmail.com
    * @version	: 1.1
    * @date	: 2025.10.11
    */
    private final String rentalId;
    private final User user;
    private final Vehicle vehicle;

    private final LocalDateTime startTime; // 운행 시작
    private LocalDateTime endTime; // 운행 종료
    private RentalInfo rentalInfo;
    private RentalStatus status;

    public Rental(String rentalId, User user, Vehicle vehicle, LocalDateTime startTime) {
        this.rentalId = Objects.requireNonNull(rentalId, "rentalId");
        this.user = Objects.requireNonNull(user, "user");
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.rentalInfo = new RentalInfo(startTime, null, 0.0); // 운행 종료 시간, 운행 거리 미정이라 null 과 0.0 으로 초기값 설정
        this.status = RentalStatus.ACTIVE;
    }

    /**
     * 대여를 완료 처리하는 메서드
     */
    public void complete() {
        if (this.status != RentalStatus.ACTIVE) {
            // 이미 처리된 대여건에 대한 중복 호출 방지
            return;
        }
        this.endTime = LocalDateTime.now();
        this.status = RentalStatus.COMPLETED;
        // TODO: 실제 주행 거리를 계산하는 로직이 있다면 여기에 추가
        double traveledDistance = 0.0; // 임시로 0.0 사용
        this.rentalInfo = new RentalInfo(this.startTime, this.endTime, traveledDistance);
    }

    public Fee calculateFinalFee(pricing.strategy.FeeStrategy strategy, List<pricing.discount.PromotionDecorator> discounts) {
        BigDecimal base = strategy.calculateFee(this.rentalInfo);
        if (base == null || base.signum() < 0) {
            throw new IllegalStateException("Base price must be a non-negative value.");
        }

        Fee fee = new pricing.BaseFee(base);

        // TODO: 데코레이터 적용 로직 구현 필요
        /*
        if (discounts != null){
            for (PromotionDecorator d : discounts) { 
              if (d == null) continue;
              fee = d.decorate(fee);   // 'decorate' 메서드는 현재 존재하지 않음
            }
        }
        */

        return fee;
    }

    // --- Getters ---
    public String getRentalId() {
        return rentalId;
    }

    public User getUser() {
        return user;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public RentalStatus getStatus() {
        return status;
    }
}

/**
* Rental.java	: Rental 초기 구현
* @author	: Minsu Kim
* @email	: minsk05151@gmail.com
* @version	: 1.0
* @date	: 2025.10.10
*/
package com.kickboard.domain.rental;

import com.kickboard.domain.user.User;
import com.kickboard.domain.vehicle.Vehicle;
import com.kickboard.pricing.BaseFee;
import com.kickboard.pricing.Fee;
import com.kickboard.pricing.discount.PromotionDecorator;
import com.kickboard.pricing.strategy.FeeStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Rental {

    private final String rentalId;
    private final User user;
    private final Vehicle vehicle;

    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private RentalInfo rentalInfo;
    private RentalStatus status;

    public Rental(String rentalId, User user, Vehicle vehicle, LocalDateTime startTime) {
        this.rentalId = Objects.requireNonNull(rentalId, "rentalId");
        this.user = Objects.requireNonNull(user, "user");
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.rentalInfo = new RentalInfo(startTime, null, 0.0);
        this.status = RentalStatus.ACTIVE;
    }

    public void complete() {
        if (this.status != RentalStatus.ACTIVE) {
            return;
        }
        this.endTime = LocalDateTime.now();
        this.status = RentalStatus.COMPLETED;
        double traveledDistance = 0.0; // 임시
        this.rentalInfo = new RentalInfo(this.startTime, this.endTime, traveledDistance);
    }

    public Fee calculateFinalFee(FeeStrategy strategy, List<PromotionDecorator> discounts) {
        // 변경된 부분: this.rentalInfo 대신 this(Rental 객체 자신)를 전달
        BigDecimal base = strategy.calculateFee(this);
        if (base == null || base.signum() < 0) {
            throw new IllegalStateException("Base price must be a non-negative value.");
        }

        Fee fee = new BaseFee(base);

        // TODO: 데코레이터 적용 로직 구현 필요
        /*
        if (discounts != null){
            for (PromotionDecorator d : discounts) { 
              if (d == null) continue;
              fee = d.decorate(fee);
            }
        }
        */

        return fee;
    }

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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public RentalInfo getRentalInfo() {
        return rentalInfo;
    }
}
package com.kickboard.domain.rental;

import com.kickboard.domain.user.PaymentMethod;
import com.kickboard.domain.user.User;
import com.kickboard.domain.vehicle.Vehicle;
import com.kickboard.pricing.BaseFee;
import com.kickboard.pricing.Fee;
import com.kickboard.pricing.discount.PromotionDecorator;
import com.kickboard.pricing.strategy.FeeStrategy;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Rental implements Serializable{

    /**
    * Rental.java	: processPayment 및 CalculateFinalFee 구현 -> 사용자가 적용할 프로모션을 선택한 후 decorator로 적용
    * @author	: Minsu Kim
    * @email	: minsk05151@gmail.com
    * @version	: 1.1
    * @date	: 2025.11.10
    */

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

    public void complete(double finalTraveledDistance) {
        if (this.status != RentalStatus.ACTIVE) {
            return;
        }
        this.endTime = LocalDateTime.now();
        this.status = RentalStatus.COMPLETED;
        this.rentalInfo = new RentalInfo(this.startTime, this.endTime, finalTraveledDistance);
    }

    // 시뮬레이션으로부터 주행 거리를 업데이트하기 위한 메서드
    public void updateTraveledDistance(double newDistance) {
        // RentalInfo는 불변(immutable) 객체이므로 새로 생성하여 교체
        this.rentalInfo = new RentalInfo(this.startTime, this.endTime, newDistance);
    }

    /**
     * 결제 실패 등으로 인해 완료 상태를 되돌리는 롤백 메서드.
     */
    public void revertComplete() {
        if (this.status != RentalStatus.COMPLETED) {
            return;
        }
        this.status = RentalStatus.ACTIVE;
        this.endTime = null;
    }
    public boolean processPayment(PaymentMethod method, BigDecimal cost) { // 결제 진행
        Payment payment = new Payment("PAY-" + rentalId, rentalId, method);
        payment.setAmount(cost);
        return payment.processPaymentCheck(); // 결제 성공 시 true 반환
    }
    
    public Fee calculateFinalFee(FeeStrategy strategy, List<PromotionDecorator> discounts, List<Integer> selectedIndexes) { // 최종 요금 계산

        BigDecimal base = strategy.calculateFee(this);
        if (base == null || base.signum() < 0) {
            throw new IllegalStateException("Base price must be a non-negative value.");
        }

        Fee fee = new BaseFee(base);

        // 선택된 프로모션만 적용
        for (int idx : selectedIndexes) {
            if (idx >= 0 && idx < discounts.size()) {
                PromotionDecorator d = discounts.get(idx);
                fee = d.decorate(fee);
            }
        }
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

package com.kickboard.domain.rental;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public final class RentalInfo implements Serializable{

    /**
    * RentalInfo.java	: RentalInfo 초기 구현
    * @author	: Minsu Kim
    * @email	: minsk05151@gmail.com
    * @version	: 1.0
    * @date	: 2025.10.10
    */
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;       
    private final double traveledDistance;
    private BigDecimal finalCost; 

    public RentalInfo(LocalDateTime startTime, LocalDateTime endTime, double traveledDistance) {
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = endTime;
        if (traveledDistance < 0) throw new IllegalArgumentException("distance < 0"); // distance는 항상 0 이상
            this.traveledDistance = traveledDistance;
    }

    // getter 구현
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public double getTraveledDistance() { return traveledDistance; }
    public BigDecimal getFinalCost() { return finalCost; } 

    // setter 구현
    public void setFinalCost(BigDecimal finalCost) { this.finalCost = finalCost; }
}

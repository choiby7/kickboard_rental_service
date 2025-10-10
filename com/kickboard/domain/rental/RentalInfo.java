package domain.rental;

import java.time.LocalDateTime;
import java.util.Objects;

public final class RentalInfo {

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

    public RentalInfo(LocalDateTime startTime, LocalDateTime endTime, double traveledDistance) {
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = endTime;
        if (traveledDistanceKm < 0) throw new IllegalArgumentException("distance < 0"); // distance는 항상 0 이상
            this.traveledDistanceKm = traveledDistanceKm;
    }
}

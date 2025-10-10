package domain.rental;

import java.time.LocalDateTime;
import java.util.Objects;

public final class RentalInfo {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;       
    private final double traveledDistance;

    public RentalInfo(LocalDateTime startTime, LocalDateTime endTime, double traveledDistance) {
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = endTime;
        if (traveledDistanceKm < 0) throw new IllegalArgumentException("distance < 0");
            this.traveledDistanceKm = traveledDistanceKm;
    }
}

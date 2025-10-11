package notification;

import domain.rental.Rental;
import domain.user.User;
import domain.vehicle.Vehicle;
import java.time.LocalDateTime;

/**
 * StatusEvent.java : 시스템 내의 상태 변경 이벤트를 나타내는 클래스
 * @author : ASDRAs
 * @version : 1.0
 * @date : 2025.10.11
 */
public class StatusEvent {

    public enum EventType {
        RENTAL_STARTED, // 대여 시작
        RENTAL_ENDED    // 대여 종료(반납)
    }

    private final EventType type;
    private final Rental rental;
    private final LocalDateTime eventTime;

    public StatusEvent(EventType type, Rental rental) {
        this.type = type;
        this.rental = rental;
        this.eventTime = LocalDateTime.now();
    }

    // --- Getters ---

    public EventType getType() {
        return type;
    }

    public Rental getRental() {
        return rental;
    }

    public User getUser() {
        return rental.getUser();
    }

    public Vehicle getVehicle() {
        return rental.getVehicle();
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }
}
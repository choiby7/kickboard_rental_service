package com.kickboard.domain.notification;

import com.kickboard.domain.rental.Rental;
import com.kickboard.domain.user.User;
import com.kickboard.domain.vehicle.Vehicle;

import java.time.LocalDateTime;

public class StatusEvent {

    public enum EventType {
        RENTAL_STARTED,
        RENTAL_ENDED
    }

    private final EventType type;
    private final Rental rental;
    private final LocalDateTime eventTime;

    public StatusEvent(EventType type, Rental rental) {
        this.type = type;
        this.rental = rental;
        this.eventTime = LocalDateTime.now();
    }

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

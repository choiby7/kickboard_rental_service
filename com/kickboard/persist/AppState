package com.kickboard.persist;

import com.kickboard.domain.rental.Rental;
import com.kickboard.domain.user.User;
import com.kickboard.domain.vehicle.Vehicle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AppState.java		: 애플리케이션 실행 상태(데이터)를 하나의 파일로 저장/복원하기 위한 DTO.
 * 						   users, vehicles, rentals, currentUserId만 포함한다. (런타임 객체 제외)
 * @author				: Mingwan Kim
 * @email				: steven3407115@dankook.ac.kr
 * @version				: 1.0
 * @date				: 2025.10.07
 */
public class AppState implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<User> users = new ArrayList<>();
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Rental> rentals = new ArrayList<>();
    private String currentUserId; // 로그인 중인 사용자 (없으면 null)

    public List<User> getUsers() { return users; }
    public List<Vehicle> getVehicles() { return vehicles; }
    public List<Rental> getRentals() { return rentals; }
    public String getCurrentUserId() { return currentUserId; }

    public void setUsers(List<User> users) { this.users = users; }
    public void setVehicles(List<Vehicle> vehicles) { this.vehicles = vehicles; }
    public void setRentals(List<Rental> rentals) { this.rentals = rentals; }
    public void setCurrentUserId(String currentUserId) { this.currentUserId = currentUserId; }
}

package com.kickboard.domain.vehicle;

import java.util.Objects;

public class Vehicle {

  /**
  * Vehicle.java	: Vehicle 초기 구현
  * @author	: Minsu Kim
  * @email	: minsk05151@gmail.com
  * @version	: 1.0
  * @date	: 2025.10.10
  */
  private final String vehicleId;
  private final String modelName;
  private VehicleStatus status;
  private String currentLocation;
  private int batteryLevel;

  public Vehicle(String vehicleId, String modelName, String currentLocation, int batteryLevel) {
      this.vehicleId = Objects.requireNonNull(vehicleId, "vehicleId");
      this.modelName = Objects.requireNonNull(modelName, "modelName");
      this.currentLocation = Objects.requireNonNull(currentLocation, "currentLocation");
      this.status = VehicleStatus.AVAILABLE; // 사용 가능 상태로 세팅
      setBatteryLevel(batteryLevel); // 배터리 양 -> 범위 검증 위해 함수 이용
  }

  // 잠금 해제(대여 시작 전).
  public boolean unlock() {
        if (status != VehicleStatus.AVAILABLE) return false; // 이미 inuse 이면 false 리턴
        status = VehicleStatus.IN_USE;
        return true;
  }

  // 잠금(반납 완료 시).
  public boolean lock() {
        if (status != VehicleStatus.IN_USE) return false; // 이미 available 이면 false 리턴
        status = VehicleStatus.AVAILABLE;
        return true;
  }

  // 정비 모드 진입
  public void moveToMaintenance() { this.status = VehicleStatus.MAINTENANCE; }

  // 정비 해제 → 사용 가능
  public void backToAvailable() { this.status = VehicleStatus.AVAILABLE; }

  // 배터리 초기 세팅
  public void setBatteryLevel(int level) {
      if (level < 0 || level > 100) { // 베터리 양 -> 범위 검증
          throw new IllegalArgumentException("batteryLevel must be 0..100");
      }
      this.batteryLevel = level;
  }

  // getters
  public String getVehicleId() { return vehicleId; }
  public String getModelName() { return modelName; }
  public VehicleStatus getStatus() { return status; }
  public String getCurrentLocation() { return currentLocation; }
  public int getBatteryLevel() { return batteryLevel; }
  
}





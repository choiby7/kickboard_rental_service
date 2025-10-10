package domain.vehicle;

import java.util.Objects;

public class Vehicle {

  private final String vehicleId;
  private final String modelName;
  private VehicleStatus status;
  private String currentLocation;
  private int batteryLevel;

  public Vehicle(String vehicleId, String modelName, String currentLocation, int batteryLevel) {
      this.vehicleId = Objects.requireNonNull(vehicleId, "vehicleId");
      this.modelName = Objects.requireNonNull(modelName, "modelName");
      this.currentLocation = Objects.requireNonNull(currentLocation, "currentLocation");
      this.status = VehicleStatus.AVAILABLE;
      this.batteryLevel = Objects.requireNonNull(batteryLevel, "batteryLevel");
  }

  /** 잠금 해제(대여 시작 전). */
  public boolean unlock() {
        if (status != VehicleStatus.AVAILABLE) return false;
        status = VehicleStatus.IN_USE;
        return true;
  }

  /** 잠금(반납 완료 시). */
  public boolean lock() {
        if (status != VehicleStatus.IN_USE) return false;
        status = VehicleStatus.AVAILABLE;
        return true;
  }

  /** 정비 모드 진입(서비스에서만 호출 권장). */
  public void moveToMaintenance() { this.status = VehicleStatus.MAINTENANCE; }

  /** 정비 해제 → 가용. */
  public void backToAvailable() { this.status = VehicleStatus.AVAILABLE; }
  
}





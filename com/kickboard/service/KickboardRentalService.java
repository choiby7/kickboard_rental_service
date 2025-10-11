package service;

import domain.rental.Rental;
import domain.rental.RentalStatus;
import domain.user.User;
import domain.vehicle.Vehicle;
import domain.vehicle.VehicleStatus;
import notification.StatusEvent;
import notification.StatusObserver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * KickboardRentalService.java : 킥보드 대여 서비스의 핵심 로직을 담당하는 서비스 클래스.
 * @author : ASDRAs
 * @version : 1.0
 * @date : 2025.10.11
 */
public class KickboardRentalService {

    private final List<User> users;
    private final List<Vehicle> kickboards;
    private final List<Rental> rentals;
    private final List<StatusObserver> observers;

    public KickboardRentalService() {
        this.users = new ArrayList<>();
        this.kickboards = new ArrayList<>();
        this.rentals = new ArrayList<>();
        this.observers = new ArrayList<>();

        System.out.println("KickboardRentalService가 생성되었습니다.");

        // =======================================================================
        // 테스트용 데이터 생성 로직 (Initialization for Test)
        // 설명:
        // 현재 시스템에는 데이터베이스나 외부 파일로부터 데이터를 읽어오는 기능이 없습니다.
        // 따라서, 프로그램을 실행하고 기능을 테스트하기 위해 서비스가 시작될 때
        // 임시로 몇 개의 킥보드 데이터를 메모리에 생성하여 추가합니다.
        // =======================================================================
        Vehicle kickboard1 = new Vehicle("KB001", "Model S", "판교역", 85);
        Vehicle kickboard2 = new Vehicle("KB002", "Model A", "정자역", 100);
        Vehicle kickboard3 = new Vehicle("KB003", "Model T", "미금역", 45);
        this.kickboards.add(kickboard1);
        this.kickboards.add(kickboard2);
        this.kickboards.add(kickboard3);
        System.out.println("[안내] 테스트용 킥보드 데이터 " + this.kickboards.size() + "개를 생성했습니다.");
        // =======================================================================
    }

    public void startService() {
        System.out.println("== 킥보드 대여 서비스 ==");

        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            while (true) {
                System.out.println("\n명령어를 입력하세요 (register, status, rent, return, exit):");
                String command = scanner.nextLine();

                switch (command) {
                    case "register":
                        System.out.println("-> 사용자 등록을 시작합니다.");
                        System.out.print("사용할 ID: ");
                        String userId = scanner.nextLine();
                        System.out.print("사용할 비밀번호: ");
                        String password = scanner.nextLine();
                        registerUser(userId, password);
                        break;
                    case "status":
                        System.out.println("--- 현재 킥보드 목록 ---");
                        if (this.kickboards.isEmpty()) {
                            System.out.println("등록된 킥보드가 없습니다.");
                        } else {
                            for (Vehicle kickboard : this.kickboards) {
                                System.out.printf("ID: %s | 모델: %s | 상태: %s | 위치: %s | 배터리: %d%%\n",
                                    kickboard.getVehicleId(),
                                    kickboard.getModelName(),
                                    kickboard.getStatus(),
                                    kickboard.getCurrentLocation(),
                                    kickboard.getBatteryLevel());
                            }
                        }
                        System.out.println("----------------------");
                        break;
                    case "rent":
                        System.out.println("-> 킥보드 대여를 시작합니다.");
                        System.out.print("사용자 ID: ");
                        String rentUserId = scanner.nextLine();
                        System.out.print("대여할 킥보드 ID: ");
                        String kickboardId = scanner.nextLine();
                        rentKickboard(rentUserId, kickboardId);
                        break;
                    case "return":
                        System.out.println("-> 킥보드 반납을 시작합니다.");
                        System.out.print("반납할 대여 ID: ");
                        String rentalId = scanner.nextLine();
                        returnKickboard(rentalId);
                        break;
                    case "exit":
                        System.out.println("서비스를 종료합니다. 이용해주셔서 감사합니다.");
                        return; // startService 메서드 종료
                    default:
                        System.out.println("알 수 없는 명령어입니다. 다시 입력해주세요.");
                        break;
                }
            }
        }
    }

    public void registerUser(String userId, String password) {
        for (User user : this.users) {
            if (user.getUserId().equals(userId)) {
                System.out.println("오류: '" + userId + "'는 이미 존재하는 ID입니다.");
                return;
            }
        }
        User newUser = new User(userId, password);
        this.users.add(newUser);
        System.out.println("'" + userId + "'님, 사용자 등록이 완료되었습니다.");
    }

    public void rentKickboard(String userId, String kickboardId) {
        User user = findUserById(userId);
        if (user == null) {
            System.out.println("오류: 등록되지 않은 사용자입니다.");
            return;
        }

        Vehicle vehicle = findVehicleById(kickboardId);
        if (vehicle == null) {
            System.out.println("오류: 존재하지 않는 킥보드 ID입니다.");
            return;
        }

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            System.out.println("오류: 해당 킥보드는 현재 대여할 수 없는 상태입니다. (상태: " + vehicle.getStatus() + ")");
            return;
        }

        for (Rental rental : this.rentals) {
            if (rental.getUser().equals(user) && rental.getStatus() == RentalStatus.ACTIVE) {
                System.out.println("오류: 이미 대여한 킥보드가 있습니다. 먼저 반납해주세요.");
                return;
            }
        }

        vehicle.unlock();
        String rentalId = "RNT-" + UUID.randomUUID().toString().substring(0, 8);
        Rental newRental = new Rental(rentalId, user, vehicle, LocalDateTime.now());
        this.rentals.add(newRental);

        System.out.printf("대여 완료! [사용자: %s, 킥보드: %s, 대여 ID: %s]\n", userId, kickboardId, rentalId);

        // 옵저버에게 대여 시작 이벤트 알림
        notifyAll(new StatusEvent(StatusEvent.EventType.RENTAL_STARTED, newRental));
    }

    public void returnKickboard(String rentalId) {
        Rental rental = findRentalById(rentalId);
        if (rental == null) {
            System.out.println("오류: 유효하지 않은 대여 ID입니다.");
            return;
        }

        if (rental.getStatus() != RentalStatus.ACTIVE) {
            System.out.println("오류: 이미 반납 처리된 대여 건입니다.");
            return;
        }

        rental.complete();
        rental.getVehicle().lock();

        System.out.printf("반납 완료! [대여 ID: %s, 사용자: %s, 킥보드: %s]\n",
            rental.getRentalId(), rental.getUser().getUserId(), rental.getVehicle().getVehicleId());
        
        // 옵저버에게 반납 완료 이벤트 알림
        notifyAll(new StatusEvent(StatusEvent.EventType.RENTAL_ENDED, rental));
    }

    public void addObserver(StatusObserver observer) {
        this.observers.add(observer);
    }

    public void notifyAll(StatusEvent e) {
        for (StatusObserver observer : this.observers) {
            observer.onEvent(e);
        }
    }

    // --- Private Helper Methods ---

    private User findUserById(String userId) {
        for (User user : this.users) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    private Vehicle findVehicleById(String kickboardId) {
        for (Vehicle vehicle : this.kickboards) {
            if (vehicle.getVehicleId().equals(kickboardId)) {
                return vehicle;
            }
        }
        return null;
    }

    private Rental findRentalById(String rentalId) {
        for (Rental rental : this.rentals) {
            if (rental.getRentalId().equals(rentalId)) {
                return rental;
            }
        }
        return null;
    }
}
}
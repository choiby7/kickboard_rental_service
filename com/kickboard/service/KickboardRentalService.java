package com.kickboard.service;

import com.kickboard.domain.rental.Rental;
import com.kickboard.domain.rental.RentalStatus;
import com.kickboard.domain.user.User;
import com.kickboard.domain.vehicle.Vehicle;
import com.kickboard.domain.vehicle.VehicleStatus;
import com.kickboard.notification.StatusEvent;
import com.kickboard.notification.StatusObserver;
import com.kickboard.pricing.Fee;
import com.kickboard.pricing.discount.PromotionDecorator;
import com.kickboard.pricing.strategy.DistanceFeeStrategy;
import com.kickboard.pricing.strategy.FeeStrategy;
import com.kickboard.pricing.strategy.TimeFeeStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
    private final List<FeeStrategy> feeStrategies;
    private final Scanner scanner;
    private User currentUser;

    public KickboardRentalService() {
        this.users = new ArrayList<>();
        this.kickboards = new ArrayList<>();
        this.rentals = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.feeStrategies = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        this.currentUser = null;

        System.out.println("KickboardRentalService가 생성되었습니다.");

        this.feeStrategies.add(new TimeFeeStrategy());
        this.feeStrategies.add(new DistanceFeeStrategy());
        
        // 상태 파일에서 로드
        com.kickboard.persist.AppState state = com.kickboard.persist.StateStore.loadOrCreate();
        if (state.getUsers() != null) this.users.addAll(state.getUsers());
        if (state.getVehicles() != null) this.kickboards.addAll(state.getVehicles());
        if (state.getRentals() != null) this.rentals.addAll(state.getRentals());
        if (state.getCurrentUserId() != null) {
            this.currentUser = findUserById(state.getCurrentUserId());
        }

        Vehicle kickboard1 = new Vehicle("KB001", "Model S", "판교역", 85);
        Vehicle kickboard2 = new Vehicle("KB002", "Model A", "정자역", 100);
        Vehicle kickboard3 = new Vehicle("KB003", "Model T", "미금역", 45);
        this.kickboards.add(kickboard1);
        this.kickboards.add(kickboard2);
        this.kickboards.add(kickboard3);
        System.out.println("[안내] 테스트용 킥보드 데이터 " + this.kickboards.size() + "개를 생성했습니다.");
    }

    public void startService() {
        System.out.println("== 킥보드 대여 서비스 ==");

        while (true) {
            String prompt = (this.currentUser == null)
                ? "\n명령어를 입력하세요 (login, register, status, exit):"
                : String.format("\n[%s님] 명령어를 입력하세요 (logout, whoami, status, rent, return, exit):", this.currentUser.getUserId());
            System.out.println(prompt);

            String command = scanner.nextLine();

            switch (command) {
                case "login":
                    if (this.currentUser != null) {
                        System.out.println("오류: 이미 로그인되어 있습니다. 먼저 로그아웃해주세요.");
                        break;
                    }
                    System.out.println("-> 로그인을 시작합니다.");
                    System.out.print("사용자 ID: ");
                    String loginId = scanner.nextLine();
                    System.out.print("비밀번호: ");
                    String loginPassword = scanner.nextLine();
                    User userToLogin = findUserById(loginId);
                    if (userToLogin != null && userToLogin.checkPassword(loginPassword)) {
                        this.currentUser = userToLogin;
                        saveState(); // 로그인 성공 시 상태 저장
                        System.out.println("로그인 성공! " + this.currentUser.getUserId() + "님, 환영합니다.");
                    } else {
                        System.out.println("오류: ID 또는 비밀번호가 일치하지 않습니다.");
                    }
                    break;
                case "logout":
                    if (this.currentUser == null) {
                        System.out.println("오류: 로그인 상태가 아닙니다.");
                    } else {
                        System.out.println(this.currentUser.getUserId() + "님이 로그아웃하셨습니다.");
                        this.currentUser = null;
                        saveState(); // 로그아웃 시 상태 저장
                    }
                    break;
                case "whoami":
                    if (this.currentUser != null) {
                        System.out.println("현재 로그인된 사용자: " + this.currentUser.getUserId());
                    } else {
                        System.out.println("로그인된 사용자가 없습니다.");
                    }
                    break;
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
                                kickboard.getVehicleId(), kickboard.getModelName(),
                                kickboard.getStatus(), kickboard.getCurrentLocation(), kickboard.getBatteryLevel());
                        }
                    }
                    System.out.println("----------------------");
                    break;
                case "rent":
                    if (this.currentUser == null) {
                        System.out.println("오류: 로그인이 필요합니다.");
                        break;
                    }
                    System.out.println("-> 킥보드 대여를 시작합니다.");
                    System.out.print("대여할 킥보드 ID: ");
                    String kickboardId = scanner.nextLine();
                    rentKickboard(this.currentUser, kickboardId);
                    break;
                case "return":
                    if (this.currentUser == null) {
                        System.out.println("오류: 로그인이 필요합니다.");
                        break;
                    }
                    returnKickboard(this.currentUser);
                    break;
                case "exit":
                	saveState();
                	// CSV로 현재 상태 내보내기
                    com.kickboard.persist.CsvExporter.exportToCsv(
                        com.kickboard.persist.StateStore.loadOrCreate()
                    );
                    System.out.println("서비스를 종료합니다. 이용해주셔서 감사합니다.");
                    this.scanner.close();
                    return;
                default:
                    System.out.println("알 수 없는 명령어입니다. 다시 입력해주세요.");
                    break;
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
        saveState(); // 엑셀 파일에 저장
    }

    public void rentKickboard(User user, String kickboardId) {
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
        System.out.printf("대여 완료! [사용자: %s, 킥보드: %s, 대여 ID: %s]\n", user.getUserId(), kickboardId, rentalId);
        notifyAll(new StatusEvent(StatusEvent.EventType.RENTAL_STARTED, newRental));
        saveState();
    }

    public void returnKickboard(User user) {
        Rental rental = findActiveRentalByUserId(user.getUserId());
        if (rental == null) {
            System.out.println("오류: 현재 대여 중인 킥보드가 없습니다.");
            return;
        }

        System.out.println("--- 현재 대여 정보 ---");
        System.out.printf("킥보드 ID: %s, 대여 시작 시간: %s\n", rental.getVehicle().getVehicleId(), rental.getStartTime());
        System.out.print("이 킥보드를 반납하시겠습니까? (y/n): ");
        String confirm = scanner.nextLine();

        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("반납을 취소했습니다.");
            return;
        }

        rental.complete();

        // 요금제 선택 로직 (예상 요금 표시 기능 추가)
        System.out.println("\n적용할 요금제를 선택해주세요.");
        for (int i = 0; i < this.feeStrategies.size(); i++) {
            FeeStrategy currentStrategy = this.feeStrategies.get(i);
            // 현재 전략으로 예상 요금을 미리 계산
            BigDecimal estimatedCost = currentStrategy.calculateFee(rental);
            System.out.printf("%d. %s [%s원]\n", i + 1, currentStrategy.name(), estimatedCost.toPlainString());
        }

        int choice = -1;
        try {
            System.out.print("선택: ");
            choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > this.feeStrategies.size()) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.println("오류: 잘못된 번호를 선택했습니다. 기본 요금제(1번)로 계산합니다.");
            choice = 1;
        }

        FeeStrategy chosenStrategy = this.feeStrategies.get(choice - 1);
        System.out.println(">> '" + chosenStrategy.name() + "' 요금제로 계산합니다.");

        List<PromotionDecorator> discounts = new ArrayList<>();
        Fee finalFee = rental.calculateFinalFee(chosenStrategy, discounts);
        BigDecimal cost = finalFee.getFinalCost();

        // --- 결제 프로세스 호출부 (Placeholder) ---
        // TODO: 아래 결제 로직은 향후 Rental 클래스의 processPaymentFlow 메서드로 이동.
        System.out.println("[결제 시도] ...");

        // TODO: User 클래스에 getPaymentMethods() 추가하고, 실제 사용자 결제 정보 가져오기.
        // PaymentMethod userPaymentMethod = rental.getUser().getPaymentMethods().get(0); // 예시 코드

        // TODO: Payment 클래스에 setAmount(BigDecimal amount) 메서드 추가.
        // Payment payment = new Payment("PAY-" + UUID.randomUUID().toString().substring(0,4), rental.getRentalId(), userPaymentMethod);
        // payment.setAmount(cost);
        // boolean paymentSuccess = payment.processPayment();

        // if (paymentSuccess) {
        //     System.out.println(">> 결제가 성공적으로 완료되었습니다.");
        // } else {
        //     System.out.println(">> 오류: 결제에 실패했습니다.");
        // }
        // --- 결제 프로세스 종료 ---

        rental.getVehicle().lock();

        System.out.printf("반납 완료! [대여 ID: %s, 사용자: %s, 킥보드: %s]\n",
            rental.getRentalId(), rental.getUser().getUserId(), rental.getVehicle().getVehicleId());
        
        notifyAll(new StatusEvent(StatusEvent.EventType.RENTAL_ENDED, rental));
        saveState();
    }

    public void addObserver(StatusObserver observer) {
        this.observers.add(observer);
    }

    public void notifyAll(StatusEvent e) {
        for (StatusObserver observer : this.observers) {
            observer.onEvent(e);
        }
    }

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

    private Rental findActiveRentalByUserId(String userId) {
        for (Rental rental : this.rentals) {
            if (rental.getUser().getUserId().equals(userId) && rental.getStatus() == RentalStatus.ACTIVE) {
                return rental;
            }
        }
        return null;
    }
    
    private void saveState() {
        com.kickboard.persist.AppState state = new com.kickboard.persist.AppState();
        state.setUsers(this.users);
        state.setVehicles(this.kickboards);
        state.setRentals(this.rentals);
        state.setCurrentUserId(this.currentUser == null ? null : this.currentUser.getUserId());

        com.kickboard.persist.StateStore.save(state);
    }
}

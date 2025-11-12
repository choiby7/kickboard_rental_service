package com.kickboard.service;

import com.kickboard.domain.rental.Rental;
import com.kickboard.domain.rental.RentalStatus;
import com.kickboard.domain.user.PaymentMethod;
import com.kickboard.domain.user.User;
import com.kickboard.domain.vehicle.Vehicle;
import com.kickboard.domain.vehicle.VehicleStatus;
import com.kickboard.notification.StatusEvent;
import com.kickboard.notification.StatusObserver;
import com.kickboard.pricing.Fee;
import com.kickboard.pricing.discount.CardDiscountDecorator;
import com.kickboard.pricing.discount.CouponDiscountDecorator;
import com.kickboard.pricing.discount.PromotionDecorator;
import com.kickboard.pricing.strategy.DistanceFeeStrategy;
import com.kickboard.pricing.strategy.FeeStrategy;
import com.kickboard.pricing.strategy.TimeFeeStrategy;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class KickboardRentalService {

    private List<User> users;
    private User currentUser;
    private final List<Vehicle> kickboards;
    private final List<Rental> rentals;
    private final List<StatusObserver> observers;
    private final List<FeeStrategy> feeStrategies;
    private final Scanner scanner;
    private final UserService userService;

    // 시뮬레이션 연동을 위한 변수 추가
    private static final Path SIMULATION_DIR = Paths.get("simulation");
    private static final Path DRIVING_STATUS_FILE = SIMULATION_DIR.resolve("driving_status.txt");

    public KickboardRentalService() {
        this.kickboards = new ArrayList<>();
        this.rentals = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.feeStrategies = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        this.currentUser = null;
        this.userService = new UserService();
        this.users = userService.getAllUsers();

        System.out.println("KickboardRentalService가 생성되었습니다.");

        this.feeStrategies.add(new TimeFeeStrategy());
        this.feeStrategies.add(new DistanceFeeStrategy());
        
        com.kickboard.persist.AppState state = com.kickboard.persist.StateStore.loadOrCreate();
        if (state.getUsers() != null) this.users.addAll(state.getUsers());
        if (state.getVehicles() != null) this.kickboards.addAll(state.getVehicles());
        if (state.getRentals() != null) this.rentals.addAll(state.getRentals());
        if (state.getCurrentUserId() != null) {
            this.currentUser = findUserById(state.getCurrentUserId());
        }

        if (this.kickboards.isEmpty()) {
            Vehicle kickboard1 = new Vehicle("KB001", "Model S", "5,5", 85);
            Vehicle kickboard2 = new Vehicle("KB002", "Model A", "10,10", 100);
            Vehicle kickboard3 = new Vehicle("KB003", "Model T", "0,0", 14); // 배터리 테스트용
            this.kickboards.add(kickboard1);
            this.kickboards.add(kickboard2);
            this.kickboards.add(kickboard3);
            System.out.println("[안내] 테스트용 킥보드 데이터 " + this.kickboards.size() + "개를 생성했습니다.");
        }
    }

    public void startService() {
        System.out.println("== 킥보드 대여 서비스 ==");

        while (true) {
            String prompt = (this.currentUser == null)
                ? "\n명령어를 입력하세요 (login, register, status, exit):"
                : String.format("\n[%s님] 명령어를 입력하세요 (logout, whoami, status, rent, return, driving_status, exit):", this.currentUser.getUserId());
            System.out.println(prompt);

            String command = scanner.nextLine();

            switch (command) {
                case "login":
                    login();
                    break;
                case "logout":
                    if (this.currentUser == null) {
                        System.out.println("오류: 로그인 상태가 아닙니다.");
                    } else {
                        System.out.println(this.currentUser.getUserId() + "님이 로그아웃하셨습니다.");
                        this.currentUser = null;
                        saveState();
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
                    registerUser();
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
                case "driving_status":
                    if (this.currentUser == null) {
                        System.out.println("오류: 로그인이 필요합니다.");
                        break;
                    }
                    updateAndDisplayDrivingStatus(this.currentUser);
                    break;
                case "payment":
                    if (this.currentUser == null) {
                        System.out.println("오류: 로그인이 필요합니다.");
                        break;
                    }
                    paymentProcess(this.currentUser);
                    break; 
                case "exit":
                	saveState();
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
    public void login() {
        if (this.currentUser != null) {
            System.out.println("오류: 이미 로그인되어 있습니다. 먼저 로그아웃해주세요.");
            return;
        }
        System.out.println("-> 로그인을 시작합니다.");
        System.out.print("사용자 ID: ");
        String loginId = scanner.nextLine();
        System.out.print("비밀번호: ");
        String loginPassword = scanner.nextLine();
        User userToLogin = findUserById(loginId);
        if (userToLogin != null && userToLogin.checkPassword(loginPassword)) {
            this.currentUser = userToLogin;
            saveState();
            System.out.println("로그인 성공! " + this.currentUser.getUserId() + "님, 환영합니다.");
        } else {
            System.out.println("오류: ID 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    public void registerUser() {
        // UserService에 모든 검증(중복, 형식)을 위임
        System.out.println("-> 사용자 등록을 시작합니다.");
        System.out.print("사용할 ID: ");
        String userId = scanner.nextLine();
        System.out.print("사용할 비밀번호: ");
        String password = scanner.nextLine();
        boolean created = userService.register(userId, password);
        if (!created) {
            System.out.println("오류: 사용자 등록 실패 (중복 ID 또는 형식 오류).");
            return;
        }
        // 성공 시 최신 사용자 목록 동기화
        this.users = userService.getAllUsers();
        System.out.println("'" + userId + "'님, 사용자 등록이 완료되었습니다.");
        saveState();
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
        if (vehicle.getBatteryLevel() < 15) {
            System.out.println("오류: 킥보드 배터리가 부족하여 대여할 수 없습니다. (현재: " + vehicle.getBatteryLevel() + "%%)");
            return;
        }
        for (Rental rental : this.rentals) {
            if (rental.getUser().equals(user) && rental.getStatus() == RentalStatus.ACTIVE) {
                System.out.println("오류: 이미 대여한 킥보드가 있습니다. 먼저 반납해주세요.");
                return;
            }
        }

        try {
            String[] parts = vehicle.getCurrentLocation().split(",");
            int startX = Integer.parseInt(parts[0]);
            int startY = Integer.parseInt(parts[1]);
            String initialState = String.format("DRIVING,%s,%d,%d,0.0,%d", 
                vehicle.getVehicleId(), startX, startY, vehicle.getBatteryLevel());
            
            if (!Files.exists(SIMULATION_DIR)) Files.createDirectories(SIMULATION_DIR);
            Files.writeString(DRIVING_STATUS_FILE, initialState, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            String command = String.format("java -cp bin com.kickboard.simulator.KickboardSimulator %s %d %d %d",
                vehicle.getVehicleId(), startX, startY, vehicle.getBatteryLevel());
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
            // Windows: 새 콘솔 창 열기
            new ProcessBuilder("cmd", "/c", "start", "cmd", "/k", command).start();
            }

            else if (os.contains("mac")) {
                // macOS: Terminal에 새 창/탭으로 명령 실행
                String projectDir = Paths.get("").toAbsolutePath().toString();
                String scriptCmd = "cd '" + projectDir.replace("'", "'\\''") + "'; " + command;
                // AppleScript로 Terminal에 전달
                new ProcessBuilder(
                    "osascript", "-e",
                    "tell application \"Terminal\" to do script \"" + scriptCmd.replace("\"", "\\\"") + "\""
                ).start();
            }
            try {
                new ProcessBuilder("x-terminal-emulator", "-e", "bash", "-lc", command + "; exec bash").start();
            } catch (IOException ignored) {
                new ProcessBuilder("bash", "-lc", command + " &").start();
            }

            System.out.println("[알림] 주행 시뮬레이터가 별도의 창에서 실행됩니다.");

        } catch (IOException | NumberFormatException e) {
            System.err.println("오류: 시뮬레이터를 시작하지 못했습니다: " + e.getMessage());
            return;
        }

        vehicle.unlock();
        String rentalId = "RNT-" + UUID.randomUUID().toString().substring(0, 8);
        Rental newRental = new Rental(rentalId, user, vehicle, LocalDateTime.now());
        this.rentals.add(newRental);
        System.out.printf("대여 완료! [사용자: %s, 킥보드: %s, 대여 ID: %s]\n", user.getUserId(), kickboardId, rentalId);
        notifyObservers(new StatusEvent(StatusEvent.EventType.RENTAL_STARTED, newRental));
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

        try {
            String statusLine = Files.readString(DRIVING_STATUS_FILE);
            String updatedLine = statusLine.replaceFirst("DRIVING", "STOP_REQUESTED");
            Files.writeString(DRIVING_STATUS_FILE, updatedLine);

            System.out.println("[알림] 시뮬레이터에 종료를 요청했습니다. 최종 데이터를 동기화합니다...");
            TimeUnit.SECONDS.sleep(2);

            updateAndDisplayDrivingStatus(user);

        } catch (IOException | InterruptedException e) {
            System.err.println("오류: 시뮬레이터와 통신 중 문제가 발생했습니다: " + e.getMessage());
        }

        rental.complete(rental.getRentalInfo().getTraveledDistance());

        System.out.println("\n적용할 요금제를 선택해주세요.");
        for (int i = 0; i < this.feeStrategies.size(); i++) {
            FeeStrategy currentStrategy = this.feeStrategies.get(i);
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
        discounts.add(new CardDiscountDecorator(null, "현대카드", new BigDecimal("0.10")));
        discounts.add(new CardDiscountDecorator(null, "삼성카드", new BigDecimal("0.05")));
        discounts.add(new CouponDiscountDecorator(null, "대학생 프로모션 쿠폰", "COLLEGE12345", new BigDecimal("0.05")));
        
        System.out.println("\n적용할 할인을 선택해주세요.");
        for (int i = 0; i < discounts.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, discounts.get(i).getDisplayName());
        }
        System.out.println("(공백으로 구분하여 입력, 예: 1 2)");

        List<Integer> selectedIndexes = new ArrayList<>();
        while (true) {
            System.out.print("선택: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) break;

            if (!input.matches("[0-9 ]+")) {
                System.out.println("오류: 숫자와 공백만 입력하세요.");
                continue;
            }
            String[] parts = input.split("\\s+");
            selectedIndexes.clear();
            boolean valid = true;
            for (String p : parts) {
                if (p.isEmpty()) continue;
                try {
                    int idx = Integer.parseInt(p);
                    if (idx < 1 || idx > discounts.size()) {
                        System.out.println("오류: " + idx + "번은 1~" + discounts.size() + " 범위를 벗어납니다.");
                        valid = false;
                        break;
                    }
                    if (!selectedIndexes.contains(idx - 1)) {
                        selectedIndexes.add(idx - 1);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("오류: 숫자만 입력하세요.");
                    valid = false;
                    break;
                }
            }
            if (!valid) continue;
            
            System.out.println("선택한 할인:");
            for (int idx : selectedIndexes) {
                System.out.println("- " + discounts.get(idx).getDisplayName());
            }
            System.out.print("이대로 진행하시겠습니까? (Y/N): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("Y")) break;
            else System.out.println("다시 선택해주세요.");
        }
        Fee finalFee = rental.calculateFinalFee(chosenStrategy, discounts, selectedIndexes);
        BigDecimal cost = finalFee.getFinalCost();
        System.out.printf("최종 결제금액: %s원\n", cost.setScale(0, RoundingMode.HALF_UP).toPlainString());

        System.out.println("[결제 시작]");
        List<PaymentMethod> methods = rental.getUser().getPaymentMethods();
        if (methods.isEmpty()) {
            System.out.println("오류: 등록된 결제수단이 없습니다. 결제를 진행할 수 없습니다.");
            System.out.println("반납 처리가 취소됩니다. 결제수단을 등록 후 다시 시도해주세요.");
            rental.revertComplete(); // 롤백
            return;
        }

        boolean paymentSuccess = false;
        while (true) {
            System.out.println("사용 가능한 결제수단:");
            for (int i = 0; i < methods.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, methods.get(i));
            }
            System.out.print("결제수단 선택 (취소하려면 'c' 입력): ");
            String paymentInput = scanner.nextLine();

            if (paymentInput.equalsIgnoreCase("c")) {
                break; // 결제 시도 루프 탈출
            }
            
            int paymentChoice = 1;
            try {
                if (!paymentInput.isEmpty()) paymentChoice = Integer.parseInt(paymentInput);
            } catch (NumberFormatException ignored) {
                System.out.println("오류: 잘못된 입력입니다.");
                continue;
            }

            if (paymentChoice < 1 || paymentChoice > methods.size()){
                System.out.printf("오류: 1 ~ %d 사이의 숫자를 입력하세요.\n", methods.size());
                continue;
            } 
            PaymentMethod selected = methods.get(paymentChoice - 1);
            if (rental.processPayment(selected, cost)){
                System.out.println("결제가 성공적으로 완료되었습니다!");
                paymentSuccess = true;
                break;
            }
            System.out.println("결제 실패. 다른 카드로 다시 시도해주세요.");
        }

        if (!paymentSuccess) {
             System.out.println("결제가 최종적으로 실패했거나 취소되었습니다. 반납 처리가 취소됩니다.");
             rental.revertComplete(); // 롤백
             return;
        }

        // 결제가 성공했을 때만 아래 로직 실행
        rental.getVehicle().lock();
        System.out.printf("반납 완료! [대여 ID: %s, 사용자: %s, 킥보드: %s]\n",
            rental.getRentalId(), rental.getUser().getUserId(), rental.getVehicle().getVehicleId());
        notifyObservers(new StatusEvent(StatusEvent.EventType.RENTAL_ENDED, rental));
        saveState();
    }

    private void updateAndDisplayDrivingStatus(User user) {
        Rental rental = findActiveRentalByUserId(user.getUserId());
        if (rental == null) {
            System.out.println("오류: 현재 대여 중인 킥보드가 없습니다.");
            return;
        }
        try {
            if (!Files.exists(DRIVING_STATUS_FILE)) {
                System.out.println("주행 정보가 없습니다.");
                return;
            }
            String statusLine = Files.readString(DRIVING_STATUS_FILE);
            String[] parts = statusLine.split(",");
            String newLocation = String.format("%s,%s", parts[2], parts[3]);
            double newDistance = Double.parseDouble(parts[4]);
            int newBattery = Integer.parseInt(parts[5]);

            rental.getVehicle().setCurrentLocation(newLocation);
            rental.getVehicle().setBatteryLevel(newBattery);
            rental.updateTraveledDistance(newDistance);

            System.out.println("--- 실시간 주행 정보 ---");
            System.out.println("킥보드 위치: " + newLocation);
            System.out.println("누적 주행 거리: " + newDistance + "m");
            System.out.println("남은 배터리: " + newBattery + "%%");
            System.out.println("--- 예상 요금 ---");
            for (FeeStrategy strategy : this.feeStrategies) {
                BigDecimal estimatedCost = strategy.calculateFee(rental);
                System.out.printf("- %s: %s원\n", strategy.name(), estimatedCost.toPlainString());
            }
            System.out.println("---------------------");
        } catch (IOException e) {
            System.err.println("오류: 주행 정보를 읽어오지 못했습니다: " + e.getMessage());
        }
    }

    private void paymentProcess(User currentUser2) {
        
        throw new UnsupportedOperationException("Unimplemented method 'paymentProcess'");
    }

    public void addObserver(StatusObserver observer) {
        this.observers.add(observer);
    }

    public void notifyObservers(StatusEvent e) {
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

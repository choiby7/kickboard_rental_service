package com.kickboard.service;

import com.kickboard.domain.factory.CreditCardFactory;
import com.kickboard.domain.factory.KakaoPayFactory;
import com.kickboard.domain.factory.PaymentFactory;
import com.kickboard.domain.rental.Payment;
import com.kickboard.domain.rental.Rental;
import com.kickboard.domain.rental.RentalStatus;
import com.kickboard.domain.payment.PaymentMethod;
import com.kickboard.domain.user.User;
import com.kickboard.domain.vehicle.Vehicle;
import com.kickboard.domain.vehicle.VehicleStatus;
import com.kickboard.domain.notification.StatusEvent;
import com.kickboard.domain.notification.StatusObserver;
import com.kickboard.domain.pricing.Fee;
import com.kickboard.domain.pricing.discount.CardDiscountDecorator;
import com.kickboard.domain.pricing.discount.CouponDiscountDecorator;
import com.kickboard.domain.pricing.discount.DistanceDiscountDecorator; 
import com.kickboard.domain.pricing.discount.PromotionDecorator;
import com.kickboard.domain.pricing.strategy.DistanceFeeStrategy;
import com.kickboard.domain.pricing.strategy.FeeStrategy;
import com.kickboard.domain.pricing.strategy.TimeFeeStrategy;
import com.kickboard.repository.CsvExporter;
import com.kickboard.repository.StateStore;


import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap; // 추가
import java.util.List;
import java.util.Map; // 추가
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class KickboardRentalService {

    private static KickboardRentalService instance;

    private User currentUser;
    private final List<Vehicle> kickboards;
    private final List<Rental> rentals;
    private final List<StatusObserver> observers;
    private final List<FeeStrategy> feeStrategies;
    private final UserService userService;
    private final Map<String, BigDecimal> cardDiscountTable; // 추가
    private PaymentFactory paymentFactory; // 추가 1120

    // 시뮬레이션 연동을 위한 변수 추가
    private static final Path SIMULATION_DIR = Paths.get("simulation");
    private static final Path DRIVING_STATUS_FILE = SIMULATION_DIR.resolve("driving_status.txt");

    private KickboardRentalService() {
        this.kickboards = new ArrayList<>();
        this.rentals = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.feeStrategies = new ArrayList<>();
        this.currentUser = null;
        this.userService = new UserService();
        this.cardDiscountTable = new HashMap<>(); // 초기화

        System.out.println("KickboardRentalService가 생성되었습니다.");

        // Factory로 전환 예정.
        this.feeStrategies.add(new TimeFeeStrategy());
        this.feeStrategies.add(new DistanceFeeStrategy());
        
        // 카드 할인 정보 추가
        this.cardDiscountTable.put("현대카드", new BigDecimal("0.10"));
        this.cardDiscountTable.put("삼성카드", new BigDecimal("0.05"));
        
        com.kickboard.repository.AppState state = com.kickboard.repository.StateStore.loadOrCreate();
        // UserService에 사용자 데이터 로드 위임
        userService.loadUsers(state.getUsers());
        
        if (state.getVehicles() != null) this.kickboards.addAll(state.getVehicles());
        if (state.getRentals() != null) this.rentals.addAll(state.getRentals());
        
        // UserService에 인증 위임
        if (state.getCurrentUserId() != null) {
            userService.authenticateWithId(state.getCurrentUserId());
            this.currentUser = userService.getCurrentUser();
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

    public static KickboardRentalService getInstance() {
        if (instance == null) {
            instance = new KickboardRentalService();
        }
        return instance;
    }

    // =================== Public API for UI Layer ===================

    public User login(String userId, String password) {
        if (userService.authenticate(userId, password)) {
            this.currentUser = userService.getCurrentUser();
            saveState();
            return this.currentUser;
        }
        return null;
    }

    public void logout() {
        if (this.currentUser == null) {
            return; // Or throw exception
        }
        userService.logout();
        this.currentUser = null;
        saveState();
    }

    public boolean register(String userId, String password) {
        boolean success = userService.register(userId, password);
        if (success) {
            saveState();
        }
        return success;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public List<Vehicle> getKickboards() {
        return new ArrayList<>(this.kickboards); // Return a copy
    }

    public UserService getUserService() {
        return this.userService;
    }

    public void shutdown() {
        saveState();
        CsvExporter.exportToCsv(StateStore.loadOrCreate());
    }

    public Rental findActiveRentalForUser(User user) {
        if (user == null) return null;
        for (Rental rental : this.rentals) {
            if (rental.getUser().getUserId().equals(user.getUserId()) && rental.getStatus() == RentalStatus.ACTIVE) {
                return rental;
            }
        }
        return null;
    }

    public List<Rental> getRentalHistoryForUser(User user) {
        List<Rental> result = new ArrayList<>();
        if (user == null) return result;

        for (Rental r : this.rentals) {
            if (r.getUser() != null &&
                r.getUser().getUserId().equals(user.getUserId())) {
                result.add(r);
            }
        }
        return result;
    }
    
    public List<FeeStrategy> getFeeStrategies() {
        return new ArrayList<>(this.feeStrategies);
    }

    //getAvailablePromitions 수정
    public List<PromotionDecorator> getAvailablePromotions(Rental rental) {
        List<PromotionDecorator> discounts = new ArrayList<>();
        User user = rental.getUser();

        // 1) 카드 할인
        if (user != null) {
            for (Map.Entry<String, BigDecimal> entry : cardDiscountTable.entrySet()) {
                discounts.add(new CardDiscountDecorator(
                    null,
                    entry.getKey(),
                    entry.getValue()
                ));
            }
        }

        // 2) 사용자 보유 쿠폰 할인
        if (user != null && user.getCoupons() != null) {
            for (Map.Entry<String, BigDecimal> e : user.getCoupons().entrySet()) {
                discounts.add(new CouponDiscountDecorator(
                    null,
                    "쿠폰(" + e.getKey() + ")",
                    e.getKey(),
                    e.getValue()
                ));
            }
        }

        // 3) 거리 조건 할인 -- distance 기반으로만 할인.
        double distance = rental.getRentalInfo().getTraveledDistance();
        if (distance >= 1.5) {
            discounts.add(new DistanceDiscountDecorator(null, 1.5, new BigDecimal("0.05")));
        }
        if (distance >= 2.0) {
            discounts.add(new DistanceDiscountDecorator(null, 2.0, new BigDecimal("0.10")));
        }

        return discounts;
    }


    // 쿠폰을 User에 추가하는 래퍼 메서드
    public boolean addCouponForUser(String userId, String couponId, BigDecimal rate) {
        boolean ok = userService.addCouponToUser(userId, couponId, rate);
        if (ok) saveState();   
        return ok;
    }

    // 사용된 쿠폰을 제거하는 메소드
    public void removeUsedCoupons(Rental rental, List<PromotionDecorator> promotions, List<Integer> selectedIndexes) {
        User user = rental.getUser();
        if (user == null) return;

        for (Integer idx : selectedIndexes) {
            if (idx < 0 || idx >= promotions.size()) continue;

            PromotionDecorator promo = promotions.get(idx);

            if (promo instanceof CouponDiscountDecorator c) {
                if (user.getCoupons().containsKey(c.getCouponId())) {
                    user.getCoupons().remove(c.getCouponId());
                    System.out.println("[안내] 쿠폰 '" + c.getCouponId() + "'은 사용되어 삭제되었습니다.");
                }
            }
        }
        saveState();
    }

    public Rental stopSimulatorAndUpdateRental(Rental rental) throws com.kickboard.exception.KickboardException {
        try {
            // 1. 반납 요청 명령 전송
            Files.writeString(DRIVING_STATUS_FILE, "RETURN_REQUESTED", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 2. 시뮬레이터가 응답(LOCKED)할 때까지 최대 5초간 폴링
            String finalStatusLine = null;
            for (int i = 0; i < 50; i++) { // 50 * 100ms = 5 seconds
                String currentStatusLine = Files.readString(DRIVING_STATUS_FILE);
                if (currentStatusLine.startsWith("LOCKED")) {
                    finalStatusLine = currentStatusLine;
                    break; // 시뮬레이터의 응답 확인, 루프 탈출
                }
                TimeUnit.MILLISECONDS.sleep(100);
            }

            if (finalStatusLine == null) {
                // 시뮬레이터가 응답하지 않으면, 현재 rental 객체의 마지막 정보를 사용합니다.
                System.err.println("[경고] 시뮬레이터가 최종 상태를 응답하지 않았습니다. 마지막으로 알려진 주행 정보를 사용합니다.");
                return rental; // 현재 rental 객체를 그대로 반환
            }

            // 3. 최종 상태 파싱 및 업데이트
            String[] parts = finalStatusLine.split(",");
            if (parts.length < 6) {
                throw new com.kickboard.exception.KickboardException("오류: 시뮬레이터로부터 잘못된 최종 상태를 받았습니다.");
            }
            
            String newLocation = String.format("%s,%s", parts[2], parts[3]);
            double newDistance = Double.parseDouble(parts[4]);
            int newBattery = Integer.parseInt(parts[5]);

            rental.getVehicle().setCurrentLocation(newLocation);
            rental.getVehicle().setBatteryLevel(newBattery);
            rental.updateTraveledDistance(newDistance);
            return rental;

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new com.kickboard.exception.KickboardException("오류: 시뮬레이터와 통신 중 문제가 발생했습니다: " + e.getMessage());
        }
    }

    public boolean processPaymentAndFinalize(Rental rental, Fee finalFee, PaymentMethod paymentMethod) {
        BigDecimal cost = finalFee.getFinalCost();
        
        boolean paymentSuccess = processPayment(rental, paymentMethod, cost);

        if (paymentSuccess) {
            rental.getVehicle().lock();
            notifyObservers(new StatusEvent(StatusEvent.EventType.RENTAL_ENDED, rental));
            writeShutdownCommand(); // 시뮬레이터에 최종 종료 명령
            saveState();
            return true;
        } else {
            rental.revertComplete();
            return false;
        }
    }

    public boolean processPayment(Rental rental, PaymentMethod method, BigDecimal cost) { // 결제 진행
        String rentalId = rental.getRentalId();

        switch (method.getType()) {
            case CREDIT_CARD:
                paymentFactory = new CreditCardFactory();
                break;
            case KAKAO_PAY:
                paymentFactory = new KakaoPayFactory();
                break;
            default:
                paymentFactory = new CreditCardFactory(); // 기본값
        }


        Payment payment = paymentFactory.createPayment(method, cost, rentalId);
        payment.setAmount(cost);
        return payment.processPaymentCheck(); // 결제 성공 시 true 반환
    }

    private void writeShutdownCommand() {
        try {
            Files.writeString(DRIVING_STATUS_FILE, "SHUTDOWN", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("[경고] 시뮬레이터 종료 명령 전송 실패: " + e.getMessage());
        }
    }

    public Rental updateDrivingStatus(Rental rental) throws com.kickboard.exception.KickboardException {
        if (rental == null) {
            throw new com.kickboard.exception.KickboardException("오류: 대여 정보가 없습니다.");
        }
        try {
            if (!Files.exists(DRIVING_STATUS_FILE)) {
                throw new com.kickboard.exception.KickboardException("주행 정보 파일을 찾을 수 없습니다.");
            }
            String statusLine = Files.readString(DRIVING_STATUS_FILE);
            String[] parts = statusLine.split(",");
            if (parts.length < 6) {
                 throw new com.kickboard.exception.KickboardException("오류: 시뮬레이터로부터 상태를 읽어오는 데 실패했습니다.");
            }
            String newLocation = String.format("%s,%s", parts[2], parts[3]);
            double newDistance = Double.parseDouble(parts[4]);
            int newBattery = Integer.parseInt(parts[5]);

            rental.getVehicle().setCurrentLocation(newLocation);
            rental.getVehicle().setBatteryLevel(newBattery);
            rental.updateTraveledDistance(newDistance);
            return rental;
        } catch (IOException e) {
            throw new com.kickboard.exception.KickboardException("오류: 주행 정보를 읽어오지 못했습니다: " + e.getMessage());
        }
    }

    // =================== Private Helper Methods ===================

    private Vehicle findVehicleById(String kickboardId) {
        for (Vehicle vehicle : this.kickboards) {
            if (vehicle.getVehicleId().equals(kickboardId)) {
                return vehicle;
            }
        }
        return null;
    }

    public void notifyObservers(StatusEvent e) {
        for (StatusObserver observer : this.observers) {
            observer.onEvent(e);
        }
    }




    public Rental rentKickboard(User user, String kickboardId) throws com.kickboard.exception.KickboardException {
        Vehicle vehicle = findVehicleById(kickboardId);
        if (vehicle == null) {
            throw new com.kickboard.exception.KickboardException("오류: 존재하지 않는 킥보드 ID입니다.");
        }
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new com.kickboard.exception.KickboardException("오류: 해당 킥보드는 현재 대여할 수 없는 상태입니다. (상태: " + vehicle.getStatus() + ")");
        }
        if (vehicle.getBatteryLevel() < 15) {
            throw new com.kickboard.exception.KickboardException("오류: 킥보드 배터리가 부족하여 대여할 수 없습니다. (현재: " + vehicle.getBatteryLevel() + "%)");
        }
        for (Rental rental : this.rentals) {
            if (rental.getUser().equals(user) && rental.getStatus() == RentalStatus.ACTIVE) {
                throw new com.kickboard.exception.KickboardException("오류: 이미 대여한 킥보드가 있습니다. 먼저 반납해주세요.");
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
                new ProcessBuilder("cmd", "/c", "start", "cmd", "/k", command).start();
            } else if (os.contains("mac")) { // 맥 인경우 실행
                String projectDir = Paths.get("").toAbsolutePath().toString();
                String scriptCmd = "cd '" + projectDir.replace("'", "'\\''") + "'; " + command;
                new ProcessBuilder("osascript",
                        "-e", "tell application \"Terminal\" to activate", // 터미널 포커스
                        "-e", "tell application \"Terminal\" to do script \"" + scriptCmd.replace("\"", "\\\"") + "\"").start();
            } else { // linux 등 기타 OS
                 try {
                    new ProcessBuilder("x-terminal-emulator", "-e", "bash", "-lc", command + "; exec bash").start();
                } catch (IOException ignored) {
                    new ProcessBuilder("bash", "-lc", command + " &").start();
                }
            }
        } catch (IOException | NumberFormatException e) { // 이외의 OS의 경우 혹은 오류 발생 시
            throw new com.kickboard.exception.KickboardException("오류: 시뮬레이터를 시작하지 못했습니다: " + e.getMessage());
        }

        vehicle.unlock();
        String rentalId = "RNT-" + UUID.randomUUID().toString().substring(0, 8);
        Rental newRental = new Rental(rentalId, user, vehicle, LocalDateTime.now());
        this.rentals.add(newRental);
        
        notifyObservers(new StatusEvent(StatusEvent.EventType.RENTAL_STARTED, newRental));
        saveState();
        
        return newRental;
    }

    private void saveState() {
        com.kickboard.repository.AppState state = new com.kickboard.repository.AppState();
        state.setUsers(userService.getAllUsers());
        state.setVehicles(this.kickboards);
        state.setRentals(this.rentals);
        User user = userService.getCurrentUser();
        state.setCurrentUserId(user == null ? null : user.getUserId());
        com.kickboard.repository.StateStore.save(state);
    }
}

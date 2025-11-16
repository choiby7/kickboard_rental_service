package com.kickboard.ui;

import com.kickboard.domain.rental.Rental;
import com.kickboard.domain.user.PaymentMethod;
import com.kickboard.domain.user.PaymentMethod; 
import com.kickboard.domain.user.User;
import com.kickboard.domain.vehicle.Vehicle;
import com.kickboard.exception.KickboardException;
import com.kickboard.domain.pricing.Fee;
import com.kickboard.domain.pricing.discount.PromotionDecorator;
import com.kickboard.domain.pricing.strategy.FeeStrategy;
import com.kickboard.service.KickboardRentalService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; 
import java.util.Scanner;

public class KickboardConsoleUI {

    private final Scanner scanner;
    private final KickboardRentalService kickboardService;

    public KickboardConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.kickboardService = new KickboardRentalService();
    }

    public void start() {
        System.out.println("== 킥보드 대여 서비스 ==");

        while (true) {
            User currentUser = kickboardService.getCurrentUser();
            String prompt = (currentUser == null)
                ? "\n명령어를 입력하세요 (login, register, status, exit):"
                : String.format("\n[%s님] 명령어를 입력하세요 (logout, whoami, payment, status, rent, return, driving_status, history, coupon, exit):", currentUser.getUserId()); //(history, coupon 추가)
            System.out.println(prompt);

            String command = scanner.nextLine();

            switch (command) {
                case "login":
                    loginUser();
                    break;
                case "logout":
                    logoutUser();
                    break;
                case "whoami":
                    showCurrentUser();
                    break;
                case "register":
                    registerUser();
                    break;
                case "status":
                    displayKickboardStatus();
                    break;
                case "rent":
                    rentKickboard();
                    break;
                case "return":
                    returnKickboard();
                    break;
                case "driving_status":
                    showDrivingStatus();
                    break;
                case "payment":
                    managePayment();
                    break;
                case "history":
                    showRentalHistory();
                    break;
                case "coupon":
                    manageCoupons();
                    break;
                case "exit":
                    kickboardService.shutdown();
                    System.out.println("서비스를 종료합니다. 이용해주셔서 감사합니다.");
                    scanner.close();
                    return;
                default:
                    System.out.println("알 수 없는 명령어입니다.");
                    break;
            }
        }
    }

    private void loginUser() {
        if (kickboardService.getCurrentUser() != null) {
            System.out.println("오류: 이미 로그인되어 있습니다.");
            return;
        }
        System.out.println("-> 로그인을 시작합니다.");
        System.out.print("사용자 ID: ");
        String userId = scanner.nextLine();
        System.out.print("비밀번호: ");
        String password = scanner.nextLine();

        User user = kickboardService.login(userId, password);
        if (user != null) {
            System.out.println("로그인 성공! " + user.getUserId() + "님, 환영합니다.");
        } else {
            System.out.println("오류: ID 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    private void logoutUser() {
        User currentUser = kickboardService.getCurrentUser();
        if (currentUser == null) {
            System.out.println("오류: 로그인 상태가 아닙니다.");
            return;
        }
        kickboardService.logout();
        System.out.println(currentUser.getUserId() + "님이 로그아웃하셨습니다.");
    }

    private void showCurrentUser() {
        User currentUser = kickboardService.getCurrentUser();
        if (currentUser != null) {
            System.out.println("현재 로그인된 사용자: " + currentUser.getUserId());
        } else {
            System.out.println("로그인된 사용자가 없습니다.");
        }
    }

    private void registerUser() {
        System.out.println("-> 사용자 등록을 시작합니다.");
        System.out.print("사용할 ID: ");
        String userId = scanner.nextLine();
        System.out.print("사용할 비밀번호: ");
        String password = scanner.nextLine();

        boolean success = kickboardService.register(userId, password);
        if (success) {
            System.out.println("'" + userId + "'님, 사용자 등록이 완료되었습니다.");
        } else {
            System.out.println("오류: 사용자 등록 실패 (중복 ID 또는 형식 오류).");
        }
    }

    private void displayKickboardStatus() {
        System.out.println("--- 현재 킥보드 목록 ---");
        List<Vehicle> kickboards = kickboardService.getKickboards();
        if (kickboards.isEmpty()) {
            System.out.println("등록된 킥보드가 없습니다.");
        } else {
            for (Vehicle kickboard : kickboards) {
                System.out.printf("ID: %s | 모델: %s | 상태: %s | 위치: %s | 배터리: %d%%\n",
                    kickboard.getVehicleId(), kickboard.getModelName(),
                    kickboard.getStatus(), kickboard.getCurrentLocation(), kickboard.getBatteryLevel());
            }
        }
        System.out.println("----------------------");
    }

    private void rentKickboard() {
        User currentUser = kickboardService.getCurrentUser();
        if (currentUser == null) {
            System.out.println("오류: 로그인이 필요합니다.");
            return;
        }
        System.out.println("-> 킥보드 대여를 시작합니다.");
        System.out.print("대여할 킥보드 ID: ");
        String kickboardId = scanner.nextLine();

        try {
            Rental rental = kickboardService.rentKickboard(currentUser, kickboardId);
            System.out.println("[알림] 주행 시뮬레이터가 별도의 창에서 실행됩니다.");
            System.out.printf("대여 완료! [사용자: %s, 킥보드: %s, 대여 ID: %s]\n",
                rental.getUser().getUserId(), rental.getVehicle().getVehicleId(), rental.getRentalId());
        } catch (KickboardException e) {
            System.err.println(e.getMessage());
        }
    }

    private void showDrivingStatus() {
        User currentUser = kickboardService.getCurrentUser();
        if (currentUser == null) {
            System.out.println("오류: 로그인이 필요합니다.");
            return;
        }
        Rental rental = kickboardService.findActiveRentalForUser(currentUser);
        if (rental == null) {
            System.out.println("오류: 현재 대여 중인 킥보드가 없습니다.");
            return;
        }
        try {
            rental = kickboardService.updateDrivingStatus(rental);
            System.out.println("--- 실시간 주행 정보 ---");
            System.out.println("킥보드 위치: " + rental.getVehicle().getCurrentLocation());
            System.out.println("누적 주행 거리: " + rental.getRentalInfo().getTraveledDistance() + "m");
            System.out.println("남은 배터리: " + rental.getVehicle().getBatteryLevel() + "%");
            System.out.println("--- 예상 요금 ---");
            for (FeeStrategy strategy : kickboardService.getFeeStrategies()) {
                BigDecimal estimatedCost = strategy.calculateFee(rental);
                System.out.printf("- %s: %s원\n", strategy.name(), estimatedCost.toPlainString());
            }
            System.out.println("---------------------");
        } catch (KickboardException e) {
            System.err.println(e.getMessage());
        }
    }

    private void managePayment() {
        User currentUser = kickboardService.getCurrentUser();
        if (currentUser == null) {
            System.out.println("오류: 로그인이 필요합니다.");
            return;
        }

        List<PaymentMethod> paymentMethods = currentUser.getPaymentMethods();
        System.out.println("--- 사용 가능한 결제 수단 ---");
        if (paymentMethods.isEmpty()) {
            System.out.println("등록된 결제수단이 없습니다.");
        } else {
            for (PaymentMethod method : paymentMethods) {
                System.out.println("별명: " + method.getAlias() + ", 카드번호: " + method.getCardNumber());
            }
        }
        System.out.println("--------------------------");

        System.out.print("새 결제수단을 추가하시겠습니까? (y/n): ");
        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("y")) {
            System.out.print("카드 번호: ");
            String cardNumber = scanner.nextLine();
            System.out.print("CVC: ");
            String cvc = scanner.nextLine();
            System.out.print("결제 수단 별칭을 입력하세요 (예: '내 주카드'): ");
            String alias = scanner.nextLine();
            
            boolean added = kickboardService.getUserService().addPaymentMethod(currentUser.getUserId(), cardNumber, cvc, alias);
            if (added) {
                System.out.println("결제수단이 성공적으로 추가되었습니다.");
            } else {
                System.out.println("오류: 결제수단 추가에 실패했습니다.");
            }
        }
    }

    private void returnKickboard() {
        User currentUser = kickboardService.getCurrentUser();
        if (currentUser == null) {
            System.out.println("오류: 로그인이 필요합니다.");
            return;
        }

        Rental rental = kickboardService.findActiveRentalForUser(currentUser);
        if (rental == null) {
            System.out.println("오류: 현재 대여 중인 킥보드가 없습니다.");
            return;
        }

        System.out.println("--- 현재 대여 정보 ---");
        System.out.printf("킥보드 ID: %s, 대여 시작 시간: %s\n", rental.getVehicle().getVehicleId(), rental.getStartTime());
        System.out.print("이 킥보드를 반납하시겠습니까? (y/n): ");
        if (!scanner.nextLine().equalsIgnoreCase("y")) {
            System.out.println("반납을 취소했습니다.");
            return;
        }

        try {
            System.out.println("[알림] 시뮬레이터에 종료를 요청했습니다. 최종 데이터를 동기화합니다...");
            rental = kickboardService.stopSimulatorAndUpdateRental(rental);
            rental.complete(rental.getRentalInfo().getTraveledDistance());
        } catch (KickboardException e) {
            System.err.println(e.getMessage());
            return;
        }

        // 1. 요금제 선택
        List<FeeStrategy> strategies = kickboardService.getFeeStrategies();
        System.out.println("\n적용할 요금제를 선택해주세요.");
        for (int i = 0; i < strategies.size(); i++) {
            System.out.printf("%d. %s [%s원]\n", i + 1, strategies.get(i).name(), strategies.get(i).calculateFee(rental).toPlainString());
        }
        int choice = -1;
        try {
            System.out.print("선택: ");
            choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > strategies.size()) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("오류: 잘못된 번호를 선택했습니다. 기본 요금제(1번)로 계산합니다.");
            choice = 1;
        }
        FeeStrategy chosenStrategy = strategies.get(choice - 1);
        System.out.println(">> '" + chosenStrategy.name() + "' 요금제로 계산합니다.");

        // 2. 할인 선택
        List<PromotionDecorator> promotions = kickboardService.getAvailablePromotions(rental); 
        List<Integer> selectedIndexes = new ArrayList<>();
        if (!promotions.isEmpty()) {
            System.out.println("\n적용할 할인을 선택해주세요. (공백으로 구분, 예: 1 2)");
            for (int i = 0; i < promotions.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, promotions.get(i).getDisplayName());
            }
            while (true) {
                System.out.print("선택: ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) break;
                // ... (입력 검증 로직은 생략, 원본 코드와 유사)
                String[] parts = input.split("\\s+");
                selectedIndexes.clear();
                for (String p : parts) {
                    if (!p.isEmpty()) selectedIndexes.add(Integer.parseInt(p) - 1);
                }
                break;
            }
        }

        // 3. 최종 요금 계산
        Fee finalFee = rental.calculateFinalFee(chosenStrategy, promotions, selectedIndexes);
        BigDecimal cost = finalFee.getFinalCost();
        System.out.printf("최종 결제금액: %s원\n", cost.setScale(0, RoundingMode.HALF_UP).toPlainString());

        // 4. 결제 수단 선택
        List<PaymentMethod> methods = currentUser.getPaymentMethods();
        if (methods.isEmpty()) {
            System.out.println("오류: 등록된 결제수단이 없습니다. 반납 처리가 취소됩니다.");
            rental.revertComplete();
            return;
        }
        System.out.println("\n사용 가능한 결제수단:");
        for (int i = 0; i < methods.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, methods.get(i).getAlias());
        }

        PaymentMethod selectedMethod = null;
        while (true) {
            System.out.print("결제수단 선택 (취소하려면 'c' 입력): ");
            String paymentInput = scanner.nextLine();
            if (paymentInput.equalsIgnoreCase("c")) {
                System.out.println("결제를 취소했습니다. 반납 처리가 취소됩니다.");
                rental.revertComplete();
                return;
            }
            try {
                int paymentChoice = Integer.parseInt(paymentInput);
                if (paymentChoice < 1 || paymentChoice > methods.size()) {
                    System.out.println("오류: 1 ~ " + methods.size() + " 사이의 숫자를 입력하세요.");
                    continue;
                }
                selectedMethod = methods.get(paymentChoice - 1);
                break;
            } catch (NumberFormatException e) {
                System.out.println("오류: 숫자만 입력해주세요.");
            }
        }

        // 5. 결제 및 반납 완료
        boolean success = kickboardService.processPaymentAndFinalize(rental, finalFee, selectedMethod);
        if (success) {
            rental.getRentalInfo().setFinalCost(finalFee.getFinalCost());//최종 결제 금액 저장 
            kickboardService.removeUsedCoupons(rental, promotions, selectedIndexes); //사용된 쿠폰 삭제 
            System.out.printf("반납 완료! [대여 ID: %s, 사용자: %s, 킥보드: %s]\n",
                rental.getRentalId(), rental.getUser().getUserId(), rental.getVehicle().getVehicleId());
        } else {
            System.out.println("결제에 실패했습니다. 다른 결제수단으로 다시 시도해주세요. 반납 처리가 취소됩니다.");
        }
    }

    //사용자의 rental history를 보여주는 method
    private void showRentalHistory() {
        User currentUser = kickboardService.getCurrentUser();
        if (currentUser == null) {
            System.out.println("오류: 로그인이 필요합니다.");
            return;
        }

        List<Rental> history = kickboardService.getRentalHistoryForUser(currentUser);
        if (history.isEmpty()) {
            System.out.println("이용 내역이 없습니다.");
            return;
        }

        System.out.println("=== 이용 내역 ===");
        for (Rental r : history) {
            RentalInfo info = r.getRentalInfo();
            System.out.printf(
                "- 대여ID: %s | 킥보드: %s | 상태: %s\n  시작: %s | 종료: %s | 거리: %.2f km | 결제 금액: %s원\n",
                r.getRentalId(),
                r.getVehicle() != null ? r.getVehicle().getVehicleId() : "-",
                r.getStatus(),
                info != null ? info.getStartTime() : r.getStartTime(),
                info != null ? info.getEndTime() : r.getEndTime(),
                info != null ? info.getTraveledDistance() : 0.0,
                (info != null && info.getFinalCost() != null)
                        ? info.getFinalCost().setScale(0, RoundingMode.HALF_UP).toPlainString()
                        : "-"
                );
        }
    }

    // 사용자가 쿠폰을 추가하는 메소드
    private void manageCoupons() {
        User currentUser = kickboardService.getCurrentUser();
        if (currentUser == null) {
            System.out.println("오류: 로그인이 필요합니다.");
            return;
        }

        Map<String, BigDecimal> coupons = currentUser.getCoupons();

        System.out.println("--- 보유 쿠폰 목록 ---");
        if (coupons == null || coupons.isEmpty()) {
            System.out.println("보유한 쿠폰이 없습니다.");
        } else {
            for (Map.Entry<String, BigDecimal> entry : coupons.entrySet()) {
                String couponId = entry.getKey();
                BigDecimal rate = entry.getValue();
                String percent = rate.multiply(new BigDecimal("100"))
                                .setScale(0, RoundingMode.HALF_UP)
                                .toPlainString();

                System.out.println("쿠폰ID: " + couponId + ", 할인율: " + percent + "%");
            }
        }
        System.out.println("--------------------------");

        System.out.print("새 쿠폰을 추가하시겠습니까? (y/n): ");
        String input = scanner.nextLine();

        if (input.equalsIgnoreCase("y")) {
            System.out.print("쿠폰 ID를 입력하세요: ");
            String couponId = scanner.nextLine();

            System.out.print("할인율을 입력하세요 (예: 0.10): ");
            BigDecimal rate = new BigDecimal(scanner.nextLine());

            boolean added = kickboardService.addCouponForUser(
                    currentUser.getUserId(),
                    couponId,
                    rate
            );

            if (added) {
                System.out.println("쿠폰이 성공적으로 추가되었습니다.");
            } else {
                System.out.println("오류: 쿠폰 추가에 실패했습니다.");
            }
        }
    }
}

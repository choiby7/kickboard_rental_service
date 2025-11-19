package com.kickboard.simulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * KickboardSimulator.java : 킥보드 주행 시뮬레이션을 담당하는 독립적인 프로그램.
 *                           driving_status.txt 파일을 통해 메인 서비스와 통신한다.
 * @author : ASDRAs
 * @version : 1.0
 * @date : 2025.10.11
 */
public class KickboardSimulator {

    private static final Path SIMULATION_DIR = Paths.get("simulation");
    private static final Path DRIVING_STATUS_FILE = SIMULATION_DIR.resolve("driving_status.txt");

    private String vehicleId;
    private int currentX, currentY;
    private double traveledDistance;
    private int batteryLevel;
    private LocalDateTime simulationStartTime;
    private Scanner simulatorScanner; // 시뮬레이터 전용 스캐너

    public KickboardSimulator(String vehicleId, int startX, int startY, int initialBattery) {
        this.vehicleId = Objects.requireNonNull(vehicleId);
        this.currentX = startX;
        this.currentY = startY;
        this.traveledDistance = 0.0;
        this.batteryLevel = initialBattery;
        this.simulationStartTime = LocalDateTime.now();
        this.simulatorScanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("사용법: java KickboardSimulator <vehicleId> <startX> <startY> <initialBattery>");
            return;
        }

        String vehicleId = args[0];
        int startX = Integer.parseInt(args[1]);
        int startY = Integer.parseInt(args[2]);
        int initialBattery = Integer.parseInt(args[3]);

        KickboardSimulator simulator = new KickboardSimulator(vehicleId, startX, startY, initialBattery);
        simulator.run();
    }

    private enum State { DRIVING, LOCKED }
    private State currentState = State.DRIVING;

    public void run() {
        System.out.println("시뮬레이터 시작: " + vehicleId + " (초기 위치: " + currentX + "," + currentY + ")");
        printStatus();

        try {
            writeDrivingStatus("DRIVING");

            while (true) {
                // 1. 메인 앱의 명령 확인
                String command = readDrivingStatusFile().split(",")[0];
                if ("RETURN_REQUESTED".equals(command) && currentState == State.DRIVING) {
                    currentState = State.LOCKED;
                    System.out.println("\n[알림] 반납이 요청되었습니다. 메인 앱에서 결제를 완료해주세요.");
                    System.out.println("더 이상 주행할 수 없습니다.");
                    // 최종 상태를 한 번 더 기록
                    writeDrivingStatus("LOCKED");
                } else if ("SHUTDOWN".equals(command)) {
                    System.out.println("\n[알림] 결제가 완료되어 시뮬레이터를 종료합니다.");
                    break;
                }

                // 2. 사용자 입력 확인 (논블로킹)
                if (System.in.available() > 0) {
                    String input = simulatorScanner.nextLine().trim().toLowerCase();
                    if (batteryLevel == 0) {
                    	System.out.println("========== No Battery ==========");
                    	continue;
                    }
                    if (currentState == State.DRIVING) {
                        if (java.util.Arrays.asList("w", "a", "s", "d").contains(input)) {
                            moveKickboard(input);
                            writeDrivingStatus("DRIVING");
                            printStatus(); // 이동 시에만 상태 출력
                        } else {
                            System.out.print("잘못된 입력입니다. (w/a/s/d): ");
                        }
                    } else { // LOCKED 상태일 때
                        System.out.println("\n[알림] 반납이 요청되어 주행할 수 없습니다. 메인 앱에서 결제를 완료해주세요.");
                    }
                }

                TimeUnit.MILLISECONDS.sleep(200); // CPU 사용량 감소를 위한 짧은 대기
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("시뮬레이터 스레드 중단됨: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("파일 통신 오류: " + e.getMessage());
        } finally {
            simulatorScanner.close();
            System.out.println("시뮬레이터가 안전하게 종료되었습니다.");
        }
    }

    private void moveKickboard(String direction) {
        int oldX = currentX, oldY = currentY;
        switch (direction) {
            case "w": currentY++; break;
            case "s": currentY--; break;
            case "a": currentX--; break;
            case "d": currentX++; break;
        }
        traveledDistance += 3.0;
        batteryLevel = Math.max(0, batteryLevel - 1);
        System.out.printf("\n[%s] 이동: (%d,%d) -> (%d,%d)\n", vehicleId, oldX, oldY, currentX, currentY);
    }

    private void printStatus() {
        System.out.println("========== 킥보드 주행 정보 ==========");
        System.out.printf("ID: %s | 위치: (%d, %d) | 주행 거리: %.1fm | 배터리: %d%%\n",
            vehicleId, currentX, currentY, traveledDistance, batteryLevel);
        System.out.println("=========================================");
        System.out.print("방향 입력 (w/a/s/d): ");
    }

    private String readDrivingStatusFile() throws IOException {
        if (!Files.exists(DRIVING_STATUS_FILE)) {
            // 파일이 없으면 초기 상태 반환 (메인 프로그램이 아직 쓰지 않았을 경우)
            return "INITIAL," + vehicleId + ",0,0,0.0,0";
        }
        return Files.readString(DRIVING_STATUS_FILE);
    }

    private void writeDrivingStatus(String status) throws IOException {
        String content = String.format("%s,%s,%d,%d,%.1f,%d",
            status, vehicleId, currentX, currentY, traveledDistance, batteryLevel);
        Files.writeString(DRIVING_STATUS_FILE, content,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }
}

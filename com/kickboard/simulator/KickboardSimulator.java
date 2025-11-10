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

    public void run() {
        System.out.println("시뮬레이터 시작: " + vehicleId + " (초기 위치: " + currentX + "," + currentY + ")");
        System.out.println("--------------------------------------------------");

        try {
            // 통신용 디렉토리 생성
            if (!Files.exists(SIMULATION_DIR)) {
                Files.createDirectories(SIMULATION_DIR);
            }

            // 초기 상태 파일 작성
            writeDrivingStatus("DRIVING");

            while (true) {
                // 1초 대기
                TimeUnit.SECONDS.sleep(1);

                // driving_status.txt 파일 읽기 (메인 프로그램의 명령 확인)
                String statusLine = readDrivingStatusFile();
                String[] parts = statusLine.split(",");
                String mainProgramStatus = parts[0];

                if ("STOP_REQUESTED".equals(mainProgramStatus)) {
                    System.out.println("메인 프로그램으로부터 종료 요청 수신.");
                    writeDrivingStatus("STOPPED"); // 최종 상태 기록 후 종료
                    break; // 루프 종료
                }

                // 사용자 입력 처리 (w,a,s,d,status)
                displayCurrentStatus();
                System.out.print("방향 입력 (w/a/s/d): ");
                String input = simulatorScanner.nextLine().trim().toLowerCase();

                if (Arrays.asList("w", "a", "s", "d").contains(input)) {
                    moveKickboard(input);
                    writeDrivingStatus("DRIVING"); // 이동 후 상태 업데이트
                } else {
                    System.out.println("잘못된 입력입니다. w,a,s,d를 입력해주세요.");
                }
                // 화면 갱신 후 3초 지연
                TimeUnit.SECONDS.sleep(3);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("시뮬레이터 스레드 중단됨: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("파일 통신 오류: " + e.getMessage());
        } finally {
            simulatorScanner.close();
            System.out.println("시뮬레이터 종료.");
        }
    }

    private void moveKickboard(String direction) {
        int oldX = currentX, oldY = currentY;
        switch (direction) {
            case "w": currentY++; break; // 위
            case "s": currentY--; break; // 아래
            case "a": currentX--; break; // 왼쪽
            case "d": currentX++; break; // 오른쪽
        }
        // TODO: 지도 경계 확인 로직 추가

        traveledDistance += 3.0; // 1회 이동당 3.0m 증가
        batteryLevel = Math.max(0, batteryLevel - 1); // 1회 이동당 배터리 1% 감소

        System.out.printf("[%s] 이동: (%d,%d) -> (%d,%d), 주행거리: %.1fm, 배터리: %d%%\n",
            vehicleId, oldX, oldY, currentX, currentY, traveledDistance, batteryLevel);
    }

    private void displayCurrentStatus() {
        // 콘솔 화면 지우기 (Windows의 'cls' 효과)
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException e) {
            // 콘솔 지우기 실패 시 무시
        }
        System.out.println("========== 킥보드 주행 시뮬레이션 ==========");
        System.out.printf("킥보드 ID: %s\n", vehicleId);
        System.out.printf("현재 위치: (%d, %d)\n", currentX, currentY);
        System.out.printf("주행 거리: %.1fm\n", traveledDistance);
        System.out.printf("배터리 잔량: %d%%\n", batteryLevel);
        System.out.println("=========================================");
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

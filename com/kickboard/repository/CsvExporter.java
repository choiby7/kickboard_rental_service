package com.kickboard.repository;

import com.kickboard.domain.rental.Rental;
import com.kickboard.domain.user.User;
import com.kickboard.domain.vehicle.Vehicle;
import com.kickboard.repository.AppState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.function.Function;

/**
 * CsvExporter.java      : AppState 데이터를 단일 CSV로 내보내는 유틸리티.
 *                         data/kickboard.csv 파일 하나만 생성한다.
 * @author				: Mingwan Kim
 * @email				: steven3407115@dankook.ac.kr
 * @version				: 1.0
 * @date				: 2025.10.07
 */
public final class CsvExporter {

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path CSV_FILE = DATA_DIR.resolve("kickboard.csv");

    private CsvExporter() {}

    public static Path exportToCsv(AppState state) {
        try {
            if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);

            StringBuilder sb = new StringBuilder(8_192);

            // --- Users ---
            sb.append("[Users]\n");
            sb.append("userId,registeredAt,status\n");
            for (User u : state.getUsers()) {
                // registeredAt/status 필드가 없다면 필요 시 빈 칸으로 남김
                sb.append(csv(u.getUserId())).append(',')
                  .append(csv("")).append(',')
                  .append(csv("ACTIVE")).append('\n');
            }
            sb.append('\n');

            // --- Vehicles ---
            sb.append("[Vehicles]\n");
            sb.append("vehicleId,modelName,status,location,battery(%)\n");
            for (Vehicle v : state.getVehicles()) {
                sb.append(csv(v.getVehicleId())).append(',')
                  .append(csv(v.getModelName())).append(',')
                  .append(csv(v.getStatus().name())).append(',')
                  .append(csv(v.getCurrentLocation())).append(',')
                  .append(csv(Integer.toString(v.getBatteryLevel()))).append('\n');
            }
            sb.append('\n');

            // --- Rentals ---
            sb.append("[Rentals]\n");
            sb.append("rentalId,userId,vehicleId,startTime,endTime,status\n");
            for (Rental r : state.getRentals()) {
                sb.append(csv(r.getRentalId())).append(',')
                  .append(csv(r.getUser() != null ? r.getUser().getUserId() : "")).append(',')
                  .append(csv(r.getVehicle() != null ? r.getVehicle().getVehicleId() : "")).append(',')
                  .append(csv(r.getStartTime() != null ? r.getStartTime().toString() : "")).append(',')
                  .append(csv(r.getEndTime() != null ? r.getEndTime().toString() : "")).append(',')
                  .append(csv(r.getStatus() != null ? r.getStatus().name() : "")).append('\n');
            }
            sb.append('\n');

            // --- Meta (현재 로그인 사용자) ---
            sb.append("[Meta]\n");
            sb.append("currentUserId\n");
            sb.append(csv(state.getCurrentUserId() == null ? "" : state.getCurrentUserId())).append('\n');

            // 파일로 기록 (덮어쓰기)
            Files.writeString(
                CSV_FILE,
                sb.toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            );

            System.out.println("[CSV 내보내기 완료] " + CSV_FILE.toAbsolutePath());
            return CSV_FILE;

        } catch (IOException e) {
            throw new RuntimeException("CSV 내보내기 실패: " + e.getMessage(), e);
        }
    }

    // CSV 안전 이스케이프: 콤마/따옴표/개행 포함 시 "..." 로 감싸고 내부 " → ""
    private static String csv(String s) {
        if (s == null) return "";
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!needQuote) return s;
        String esc = s.replace("\"", "\"\"");
        return "\"" + esc + "\"";
    }
}

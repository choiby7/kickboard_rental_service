package com.kickboard.repository;

import com.kickboard.domain.rental.Rental;
import com.kickboard.domain.user.User;
import com.kickboard.domain.vehicle.Vehicle;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;

/**
 * CsvExporter.java      : AppState 데이터를 단일 CSV로 내보내는 유틸리티.
 *                         data/kickboard.csv 파일 하나만 생성한다.
 * @author              : Mingwan Kim
 * @email               : steven3407115@dankook.ac.kr
 * @version             : 1.1
 * @date                : 2025.11.17
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
            // registeredAt/status 칼럼은 예비 칼럼 유지, couponCount 추가
            sb.append("userId,registeredAt,status,couponCount\n");
            for (User u : state.getUsers()) {
                int couponCount = (u.getCoupons() == null) ? 0 : u.getCoupons().size();
                sb.append(csv(u.getUserId())).append(',')
                  .append(csv("")).append(',')
                  .append(csv("ACTIVE")).append(',')
                  .append(csv(Integer.toString(couponCount))).append('\n');
            }
            sb.append('\n');

            // --- Coupons ---
            // 각 사용자 보유 쿠폰을 userId,couponId,rate 로 펼쳐 쓴다.
            sb.append("[Coupons]\n");
            sb.append("userId,couponId,rate\n");
            for (User u : state.getUsers()) {
                Map<String, BigDecimal> coupons = u.getCoupons();
                if (coupons == null || coupons.isEmpty()) continue;

                // 정렬(선택): couponId 기준 오름차순
                coupons.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(e -> {
                            sb.append(csv(u.getUserId())).append(',')
                              .append(csv(e.getKey())).append(',')
                              .append(csv(toNum(e.getValue()))).append('\n');
                        });
            }
            sb.append('\n');

            // --- Vehicles ---
            sb.append("[Vehicles]\n");
            sb.append("vehicleId,modelName,status,location,battery(%)\n");
            for (Vehicle v : state.getVehicles()) {
                sb.append(csv(v.getVehicleId())).append(',')
                  .append(csv(v.getModelName())).append(',')
                  .append(csv(v.getStatus() == null ? "" : v.getStatus().name())).append(',')
                  .append(csv(v.getCurrentLocation())).append(',')
                  .append(csv(Integer.toString(v.getBatteryLevel()))).append('\n');
            }
            sb.append('\n');

            // --- Rentals ---
            sb.append("[Rentals]\n");
            sb.append("rentalId,userId,vehicleId,startTime,endTime,status\n");
            for (Rental r : state.getRentals()) {
                sb.append(csv(r.getRentalId())).append(',')
                  .append(csv(r.getUser()    != null ? r.getUser().getUserId()          : "")).append(',')
                  .append(csv(r.getVehicle() != null ? r.getVehicle().getVehicleId()    : "")).append(',')
                  .append(csv(r.getStartTime()!= null ? r.getStartTime().toString()     : "")).append(',')
                  .append(csv(r.getEndTime()  != null ? r.getEndTime().toString()       : "")).append(',')
                  .append(csv(r.getStatus()   != null ? r.getStatus().name()            : "")).append('\n');
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

    // ===== Helpers =====

    // CSV 안전 이스케이프: 콤마/따옴표/개행 포함 시 "..." 로 감싸고 내부 " → ""
    private static String csv(String s) {
        if (s == null) return "";
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!needQuote) return s;
        String esc = s.replace("\"", "\"\"");
        return "\"" + esc + "\"";
    }

    private static String toNum(BigDecimal v) {
        return (v == null) ? "" : v.toPlainString();
    }
}

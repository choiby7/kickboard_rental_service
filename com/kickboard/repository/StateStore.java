package com.kickboard.repository;

import com.kickboard.repository.AppState;
import java.io.*;
import java.nio.file.*;

/**
 * StateStore.java       : AppState를 단일 파일로 저장/복원하는 유틸리티
 * 						   data/kickboard.state 파일 사용
 * @author				: Mingwan Kim
 * @email				: steven3407115@dankook.ac.kr
 * @version				: 1.0
 * @date				: 2025.10.07
 */
public final class StateStore {

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path STATE_FILE = DATA_DIR.resolve("kickboard.state");

    private StateStore() {}

    public static AppState loadOrCreate() {
        try {
            if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);
            if (!Files.exists(STATE_FILE)) {
                return new AppState(); // 빈 상태
            }
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(STATE_FILE))) {
                Object obj = ois.readObject();
                return (AppState) obj;
            }
        } catch (Exception e) {
            // 손상/버전 불일치 등 문제 시 새 상태로 시작
            System.out.println("[경고] 상태 파일을 읽지 못해 새 상태로 시작합니다: " + e.getMessage());
            return new AppState();
        }
    }

    public static void save(AppState state) {
        try {
            if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(STATE_FILE,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                oos.writeObject(state);
            }
        } catch (IOException e) {
            throw new RuntimeException("상태 저장 실패: " + e.getMessage(), e);
        }
    }

    public static Path stateFilePath() {
        return STATE_FILE;
    }
}


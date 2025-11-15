package com.kickboard.exception;

/**
 * KickboardException.java: 킥보드 대여 서비스에서 발생하는 모든 비즈니스 관련 예외의 부모 클래스입니다.
 */
public class KickboardException extends Exception {
    public KickboardException(String message) {
        super(message);
    }
}

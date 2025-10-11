package com.kickboard.domain.user;

import java.util.List;

/**
 * class User: 사용자관련 정보 저장 도메인
 * @author : Choi BeomYeon
 * @email : cby9017@gmail.com
 * @version : 1.0
 * @date : 2025.10.08
 */
public class User {

    private final String userId;
    private final String password;
    private String email;
    private DriverLicense license;
    private List<PaymentMethod> paymentMethods;

    /**
     *
     * @param userId
     * @param password
     */
    public User(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    /**
     * 로그인 함수 -
     * 
     * @param paramId       : 입력 아이디
     * @param paramPassword : 입력 비밀번호
     * @return this: User : 본인 객체 반환
     */
    /**
     * 입력된 비밀번호가 사용자의 비밀번호와 일치하는지 확인합니다.
     * @param password 확인할 비밀번호
     * @return 일치하면 true, 아니면 false
     */
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    /**
     * 로그아웃 함수 - 미구현 (세션 관리는 서비스 레이어에서 처리)
     */
    public void logout() {
        // Domain object itself doesn't manage session state.
    }

    // getter & setter

    // id & password : final이라 setter X
    public String getUserId() {
        return this.userId;
    }

    public String getPassword() {
        return this.password;
    }

    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public DriverLicense getDriverLicense() {
        return this.license;
    }
    public void setDriverLicense(DriverLicense license) {
        this.license = license;
    }



}
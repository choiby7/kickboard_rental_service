package com.kickboard.domain.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * class User: 사용자관련 정보 저장 도메인
 * @author : Choi BeomYeon
 * @email : cby9017@gmail.com
 * @version : 1.0
 * @date : 2025.10.08
 */
public class User implements Serializable{

    private final String userId;
    private final String password;
    private String email;
    private DriverLicense license;
    private List<PaymentMethod> paymentMethods;
    private Map<String, BigDecimal> coupons;

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

    public Map<String, BigDecimal> getCoupons() {
        return this.coupons;
    }

    public void setCoupons(Map<String, BigDecimal> coupons) {
        this.coupons = coupons;
    }

    /**
     * 사용자에 운전면허 정보를 설정합니다.
     * @param license 설정할 운전면허 정보 (null 허용)
     */
    public void setDriverLicense(DriverLicense license) {
        this.license = license;
    }

    /**
     * 사용자에 결제수단을 추가합니다.
     * @param method 추가할 결제수단 (null 허용 안함)
     */
    public void addPaymentMethod(PaymentMethod method) {
        if (method == null) throw new IllegalArgumentException("payment method must not be null");
        if (this.paymentMethods == null) this.paymentMethods = new java.util.ArrayList<>();
        this.paymentMethods.add(method);
    }

    /**
     * 사용자에 연결된 결제수단 목록을 읽기전용으로 반환합니다.
     * - null 반환 x: 내부 리스트가 아직 생성되지 않았으면 빈 리스트를 반환합니다.
     * - 외부에서 직접 수정하면 안되므로 읽기전용 뷰를 제공합니다.
     *
     * @return 결제수단 리스트 (수정 불가 뷰)
     */
    public List<PaymentMethod> getPaymentMethods() {
        if (this.paymentMethods == null) return java.util.Collections.emptyList();
        return java.util.Collections.unmodifiableList(this.paymentMethods);
    }
    
}

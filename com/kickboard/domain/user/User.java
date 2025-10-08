package domain.user;

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
    public User login(String paramId, String paramPassword) {
        if (userId.equals(paramId) && password.equals(paramPassword)) {
            // login success
            return this;
        }
        throw new RuntimeException("login fail");
    }

    /**
     * 로그아웃 함수 - 미구현
     */
    public void logout() {

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
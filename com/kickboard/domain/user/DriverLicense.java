package domain.user;

/**
 * class DriverLicense.java: 면허정보 + 인증
 * @author: Choi BeomYeon
 * @email: cby9017@naver.com
 * @version: 1.0
 * @date: 2025.10.8
 */
public class DriverLicense {
    private String licenseNumber;
    private boolean valid;

    public DriverLicense(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    /**
     * 면허증 유효성 검사
     */
    public void validate() {
        if (licenseNumber.length() == 12) valid = true;
        else valid = false;
    }

    /**
     * 유효성 여부 반환
     * @return valid
     */
    public boolean isVaild() {
        return valid;
    }
}


package com.kickboard.service;

import com.kickboard.domain.user.DriverLicense;
import com.kickboard.domain.user.PaymentMethod;
import com.kickboard.domain.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * UserService: 사용자 관련 서비스 레이어
 * @author : choiby
 * @version : 1.0
 * @date : 2025.11.2
 * 구현 목표
 * <=== 메서드 ===>
 * - 회원가입(register)
 * - 로그인(authenticate)
 * - 로그아웃(logout)
 * - 운전면허 등록(registerDriverLicense)
 * - 결제수단 추가(addPaymentMethod)
 *
 * 각 메서드는 도메인 객체(`User`, `DriverLicense`, `PaymentMethod`)와 연동되며
 * 단위 테스트와 서비스 계층에서 호출하기 쉽도록 단순한 시그니처로 제공.
 *
 * 설계 :
 * - register: 중복 ID 검사 -> 새 User 생성 및 목록에 추가
 * - authenticate: 아이디로 사용자 조회 -> 비밀번호 확인(true/false)
 * - logout: 현재 로그인된 사용자를 null 처리 (세션 관리는 상위 레이어에서 담당 가능)
 * - registerDriverLicense: 면허번호를 받아 DriverLicense 객체를 생성/검증 후 사용자에 등록
 * - addPaymentMethod: 간단한 결제수단(카드) 정보를 받아 User에 추가
 * 
 * 유의사항
 * - 이 서비스는 메모리 내 사용자 목록을 관리하므로, 실제 애플리케이션에서는 영속성 계층과 연동 필요.
 * - 동시성 제어나 보안 강화는 별도 고려 필요.
 * - null 입력 방지를 위해 Objects.requireNonNull 사용.
 */
public class UserService {

    private final List<User> users = new ArrayList<>();
    private User currentUser = null; // 간단한 세션 시뮬레이션

    // --------------------------- 회원 관리 ---------------------------

    /**
     * 회원가입
     * feat : 중복되는 ID 가입 불가.
     * @param userId 가입할 ID
     * @param password 비밀번호
     * @return true: 등록 성공, false: 이미 존재하는 ID
     */
    public boolean register(String userId, String password) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(password, "password");

        if (findUserById(userId) != null) {
            return false; // 중복 ID
        }
        if (userId.isEmpty() || password.isEmpty()) {
            return false; // 빈 문자열 허용 안 함
        }
        User user = new User(userId, password);
        users.add(user);
        return true;
    }

    /**
     * 로그인 시도 함수
     * feat : 아이디와 비밀번호로 로그인 시도. 메모리에 현재 로그인 한 유저 Service 객체의 필드로 저장 (세션 시뮬레이션)
     * @param userId 아이디
     * @param password 비밀번호
     * @return true: 로그인 성공(내부 currentUser에 저장), false: 실패
     */
    public boolean authenticate(String userId, String password) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(password, "password");

        User user = findUserById(userId);
        if (user == null) return false;
        if (user.checkPassword(password)) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    /**
     * 로그아웃 : 현재 로그인된 사용자 null 초기화
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * 현재 로그인된 사용자 반환 (없으면 null)
     */
    public User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * 외부(e.g. 상태 파일)에서 사용자 목록을 로드합니다.
     * @param users 로드할 사용자 목록
     */
    public void loadUsers(List<User> users) {
        if (users == null) return;
        this.users.clear();
        this.users.addAll(users);
    }

    /**
     * ID만으로 사용자를 인증합니다. (상태 복원용)
     * @param userId 사용자 ID
     * @return 인증 성공 시 true
     */
    public boolean authenticateWithId(String userId) {
        if (userId == null) return false;
        User user = findUserById(userId);
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    // --------------------------- 운전면허 관리 ---------------------------

    /**
     * 사용자에게 운전면허 정보를 등록하고 유효성 검사를 실행합니다.
     * - 사용자 존재 검증, 면허 생성 및 validate 호출, User에 set
     *
     * @param userId 대상 사용자 ID
     * @param licenseNumber 면허번호
     * @return true: 등록 및 유효성 검사 통과, false: 사용자 없음 또는 유효하지 않음
     */
    public boolean registerDriverLicense(String userId, String licenseNumber) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(licenseNumber, "licenseNumber");

        User user = findUserById(userId);
        if (user == null) return false;

        DriverLicense license = new DriverLicense(licenseNumber);
        if (!license.isValid()) {
            // 유효하지 않은 면허는 등록하지 않는다.
            return false;
        }
        user.setDriverLicense(license);
        return true;
    }

    // --------------------------- 결제수단 관리 ---------------------------

    /**
     * 사용자에게 결제수단(카드)을 추가합니다.
     * - User#addPaymentMethod를 사용하여 내부 리스트에 추가합니다.
     *
     * @param userId 대상 사용자 ID
     * @param cardNumber 카드번호
     * @param cvc CVC
     * @return true: 추가 성공, false: 사용자 없음
     */
    public boolean addPaymentMethod(String userId,String cardNumber, String cvc, String alias) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(cardNumber, "cardNumber");
        Objects.requireNonNull(cvc, "cvc");
        Objects.requireNonNull(alias, "alias");

        User user = findUserById(userId);
        if (user == null) return false;

        PaymentMethod method = new PaymentMethod(cardNumber, cvc, alias);
        user.addPaymentMethod(method);
        return true;
    }

    // --------------------------- 헬퍼 ---------------------------

    public User findUserById(String userId) {
        for (User u : this.users) {
            if (u.getUserId().equals(userId)) return u;
        }
        return null;
    }

    /**
     * 내부에 보관된 (registered) 모든 사용자(읽기용) 반환
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(this.users);
    }

}
package com.kickboard.test;

import com.kickboard.domain.payment.PaymentMethodType;
import com.kickboard.service.UserService;

// UserServiceTest.java : UserService 단위 테스트
// @author : choiby
// @version : 1.0
// @date : 2025.11.2

/**
 * UserServiceTest: UserService 단위 테스트 클래스
 * 기본적인 회원가입, 로그인, 로그아웃, 운전면허 등록, 결제수단 추가 기능 기본 테스트를 수행하는 클래스.
 */
public class UserServiceTest {

    private UserService userService;

    public static void main(String[] args) {
        
        UserServiceTest test = new UserServiceTest();
        test.testRegisterAndAuthenticate();
    }
    // 테스트 메서드 예시
    public void testRegisterAndAuthenticate() {
        userService = new UserService();

        System.out.println(userService.getAllUsers());

        String userId = "testUser";
        String password = "securePass";     

        // 회원가입 테스트
        boolean registerResult1 = userService.register(userId, password);
        // boolean registerResult2 = userService.register(userId, password);

        // if (!registerResult2) {
        //     System.out.println("회원가입 실패: 이미 존재하는 ID");
        //     return;
        // }


        System.out.println("회원가입 성공");

        System.out.println(userService.getAllUsers());

        // 로그인 테스트
        if (userService.authenticate(userId, password)) {
            System.out.println("로그인 성공");
            System.out.println("현재 로그인된 사용자: " + userService.getCurrentUser().getUserId());
        } else {
            System.out.println("로그인 실패");
        }

        // 로그아웃 테스트
        userService.logout();
        System.out.println("로그아웃 성공");
        System.out.println("현재 로그인된 사용자: " + userService.getCurrentUser());
        System.out.println(userService.getAllUsers());


        // 결제수단 추가 테스트
        if (userService.authenticate(userId, password)) {
            System.out.println("로그인 성공");
            System.out.println("현재 로그인된 사용자: " + userService.getCurrentUser().getUserId());
        } else {
            System.out.println("로그인 실패");
        }
        userService.addPaymentMethod(PaymentMethodType.CREDIT_CARD, userService.getCurrentUser().getUserId(), "9430567890123456", "123", "myhyundai", "Hyundai"); 
        userService.addPaymentMethod(PaymentMethodType.CREDIT_CARD, userService.getCurrentUser().getUserId(), "9400567890123456", "456", "mysamsung", "Samsung"); 
        System.out.println("결제수단 추가 성공");
        System.out.println(userService.getCurrentUser().getPaymentMethods());
        System.out.println(userService.getAllUsers()); 

        // 운전면허 추가 테스트
        userService.registerDriverLicense(userService.getCurrentUser().getUserId(), "D12345678912");
        System.out.println("운전면허 추가 성공");
        System.out.println(userService.getCurrentUser().getDriverLicense());
        System.out.println(userService.getAllUsers());

        
    }

    

}

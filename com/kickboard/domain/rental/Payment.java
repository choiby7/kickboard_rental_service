package com.kickboard.domain.rental;

import com.kickboard.domain.user.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Payment {

    /**
    * Payment.java	: 요금 결재 클래스 payment 초기 구현
    * @author	: Minsu Kim
    * @email	: minsk05151@gmail.com
    * @version	: 1.0
    * @date	: 2025.10.10
    */
    private final String paymentId;
    private final String rentalId;
    private PaymentMethod paymentMethod;
    private LocalDateTime transactionDate;
    private BigDecimal amount;
    private PaymentStatus status;

    public Payment(String paymentId, String rentalId, PaymentMethod method) {
        this.paymentId = Objects.requireNonNull(paymentId, "paymentId");
        this.rentalId = Objects.requireNonNull(rentalId, "rentalId");
        this.paymentMethod = Objects.requireNonNull(method, "paymentMethod");
        this.status = PaymentStatus.FAILED; // 초기 상태
    }

    public boolean processPayment() {
        if (amount == null) throw new IllegalStateException("amount not set"); // amount가 null일 경우
        if (paymentMethod.getCardNumber() == null || paymentMethod.getCvc() == null) throw new IllegalStateException("card info wrong");
        // 카드번호 값이 존재하지 않을 경우. 

        // 결재 불가한 케이스를 작성
        // ex) 카드의 잔액 부족

        // 일단 결재 성공한다는 가정
        this.transactionDate = LocalDateTime.now();
        this.status = PaymentStatus.SUCCESS;
        return this.status == PaymentStatus.SUCCESS;
    }
}

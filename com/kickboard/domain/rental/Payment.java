package com.kickboard.domain.rental;

import com.kickboard.domain.payment.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Payment {

    /**
    * Payment.java	: setamout 및 processPayment 구현
    * @author	: Minsu Kim
    * @email	: minsk05151@gmail.com
    * @version	: 1.1
    * @date	: 2025.11.10
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

    public boolean processPaymentCheck() {
        if (amount == null) throw new IllegalStateException("amount not set"); // amount가 null일 경우
        if (paymentMethod.getIdentifier() == null || paymentMethod.getPassword() == null) throw new IllegalStateException("card info wrong"); // 카드번호 값이 존재하지 않을 경우.
        

        if (paymentMethod.getBalance().compareTo(amount) < 0){ // 결제 실패 
            this.status = PaymentStatus.FAILED;
            this.transactionDate = LocalDateTime.now();
            return false;
        }
        // 결제 성공
        paymentMethod.deductBalance(amount);
        this.transactionDate = LocalDateTime.now();
        this.status = PaymentStatus.SUCCESS;
        return this.status == PaymentStatus.SUCCESS;
    }

    public void setAmount(BigDecimal amount){ // 최종 요금 호출
        this.amount = amount;
    }

}

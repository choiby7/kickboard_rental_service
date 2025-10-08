package domain.user;

/**
 * class PaymentMethod.java
 * @author: Choi BeomYeon
 * @email: cby9017@naver.com
 * @version: 1.0
 * @date: 2025.10.8
 */
public class PaymentMethod {
    private final String cardNumber;
    private final String cvc;

    public PaymentMethod(String cardNumber, String cvc) {
        this.cardNumber = cardNumber;
        this.cvc = cvc;
    }

    public String getCardNumber() {
        return this.cardNumber;
    }

    public String getCvc() {
        return this.cvc;
    }
}

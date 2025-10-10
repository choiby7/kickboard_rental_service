package domain.rental;

public class Rental {

    /**
    * Rental.java	: Rental 초기 구현
    * @author	: Minsu Kim
    * @email	: minsk05151@gmail.com
    * @version	: 1.0
    * @date	: 2025.10.10
    */
    private final String rentalId;
    private final User user;
    private final Vehicle vehicle;

    private LocalDateTime startTime; // 운행 시작
    private LocalDateTime endTime; // 운행 종료
    private RentalInfo rentalInfo;  
    private RentalStatus status;

    public Rental(String rentalId, User user, Vehicle vehicle, LocalDateTime startTime) {
        this.rentalId = Objects.requireNonNull(rentalId, "rentalId");
        this.user = Objects.requireNonNull(user, "user");
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.rentalInfo = new RentalInfo(startTime, null, 0.0); // 운행 종료 시간, 운행 거리 미정이라 null 과 0.0 으로 초기값 설정
        this.status = RentalStatus.ACTIVE;
    }

    public Fee calculateFinalFee(FeeStrategy strategy, List<PromotionDecorator> discounts) { 
        BigDecimal base = strategy.calculatePrice(this.rentalInfo); // base -> rentalInfo 정보 받아와서 지정된 FeeStrategy 방법으로 초기 요금 계산(할인 전)
        if (base == null || base.signum() < 0) {
            throw new IllegalStateException("base price must be >= 0");
        }
      
        Fee fee = new BaseFee(base); // Fee의 BaseFee 객체 생성(할인 전 초기값)

        // 리스트에 있는 discount 조건들을 데코레이터로 적용
        if (discounts != null){
            for (PromotionDecorator d : discounts) { // for문으로 하나씩 적용
              if (d == null) continue;
              fee = d.decorate(fee);   // PromotionDecorator의 decorate 메서드 이용 -> 새로 감싼 데코레이터를 반환 (decorate 메서드 구현 필요)
            }
        }
      
        return fee;
    }
}

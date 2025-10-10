package domain.rental;

public class Rental {

    private final String rentalId;
    private final User user;
    private final Vehicle vehicle;

    private LocalDateTime startTime;
    private LocalDateTime endTime; 
    private RentalInfo rentalInfo;  
    private RentalStatus status;

    public Rental(String rentalId, User user, Vehicle vehicle, LocalDateTime startTime) {
        this.rentalId = Objects.requireNonNull(rentalId, "rentalId");
        this.user = Objects.requireNonNull(user, "user");
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.rentalInfo = new RentalInfo(startTime, null, 0.0);
        this.status = RentalStatus.ACTIVE;
    }

    public Fee calculateFinalFee(FeeStrategy strategy, List<PromotionDecorator> discounts) {
        BigDecimal base = strategy.calculatePrice(this.rentalInfo);
        if (base == null || base.signum() < 0) {
            throw new IllegalStateException("base price must be >= 0");
        }
      
        // 기본 요금
        Fee fee = new BaseFee(base);

        // 리스트에 있는 discount 조건들을 데코레이터로 적용
        if (discounts != null){
            for (PromotionDecorator d : discounts) {
              if (d == null) continue;
              fee = d.decorate(fee);   // 새로 감싼 데코레이터를 반환
            }
        }
      
        return fee;
    }
}

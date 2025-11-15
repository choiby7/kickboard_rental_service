package com.kickboard.domain.notification;

/**
 * AllKickboardObserver.java : 모든 킥보드 관련 이벤트를 감시하고 콘솔에 출력하는 옵저버
 * @author : ASDRAs
 * @version : 1.0
 * @date : 2025.10.11
 */
public class AllKickboardObserver implements StatusObserver {

    @Override
    public void onEvent(StatusEvent event) {
        // 이벤트 타입에 따라 다른 메시지를 출력합니다.
        switch (event.getType()) {
            case RENTAL_STARTED:
                System.out.printf("[이벤트 로그] 킥보드 대여 발생 | 킥보드 ID: %s, 사용자 ID: %s, 시간: %s\n",
                    event.getVehicle().getVehicleId(),
                    event.getUser().getUserId(),
                    event.getEventTime());
                break;
            case RENTAL_ENDED:
                System.out.printf("[이벤트 로그] 킥보드 반납 발생 | 킥보드 ID: %s, 사용자 ID: %s, 시간: %s\n",
                    event.getVehicle().getVehicleId(),
                    event.getUser().getUserId(),
                    event.getEventTime());
                break;
        }
    }
}
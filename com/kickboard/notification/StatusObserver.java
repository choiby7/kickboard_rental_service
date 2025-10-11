package notification;

/**
 * StatusObserver.java : 상태 변경 이벤트를 수신하는 옵저버의 인터페이스
 * @author : ASDRAs
 * @version : 1.0
 * @date : 2025.10.11
 */
public interface StatusObserver {

    /**
     * 이벤트가 발생했을 때 호출되는 메서드
     * @param event 발생한 이벤트에 대한 정보를 담은 객체
     */
    void onEvent(StatusEvent event);
}
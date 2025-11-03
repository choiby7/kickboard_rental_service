package com.kickboard;

import com.kickboard.notification.AllKickboardObserver;
import com.kickboard.service.KickboardRentalService;

/**
 * Main.java			: 애플리케이션의 시작점. KickboardRentalService를 생성하고 실행한다.
 * @author			: ASDRAs
 * @version			: 1.0
 * @date			: 2025.10.11
 */
public class Main {

    /**
     * 애플리케이션의 메인 메서드
     * @param args
     */
    public static void main(String[] args) {
    	// CSV 시작 파일 준비 (없으면 생성, 있으면 건드리지 않음)
        System.out.println("Kickboard Rental Service를 시작합니다.");
        
        // 1. 서비스(Subject) 생성
        KickboardRentalService service = new KickboardRentalService();

        // 2. 옵저버(Observer) 생성 및 등록
        AllKickboardObserver kickboardLogger = new AllKickboardObserver();
        service.addObserver(kickboardLogger);

        // 3. 서비스 시작
        service.startService();
    }
}

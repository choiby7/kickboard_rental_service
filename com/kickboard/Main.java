package com.kickboard;

import com.kickboard.ui.KickboardConsoleUI;

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
        KickboardConsoleUI ui = new KickboardConsoleUI();
        ui.start();
    }
}

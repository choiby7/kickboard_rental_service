package com.kickboard.ui.command;

import com.kickboard.ui.KickboardConsoleUI;

public class CouponCommand implements Command {
    private final KickboardConsoleUI consoleUI;

    public CouponCommand(KickboardConsoleUI consoleUI) {
        this.consoleUI = consoleUI;
    }

    @Override
    public void execute() {
        consoleUI.manageCoupons();
    }
}

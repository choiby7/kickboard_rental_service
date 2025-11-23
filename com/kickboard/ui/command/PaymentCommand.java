package com.kickboard.ui.command;

import com.kickboard.ui.KickboardConsoleUI;

public class PaymentCommand implements Command {
    private final KickboardConsoleUI consoleUI;

    public PaymentCommand(KickboardConsoleUI consoleUI) {
        this.consoleUI = consoleUI;
    }

    @Override
    public void execute() {
        consoleUI.managePayment();
    }
}

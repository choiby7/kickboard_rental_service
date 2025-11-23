package com.kickboard.ui.command;

import com.kickboard.ui.KickboardConsoleUI;

public class RentCommand implements Command {
    private final KickboardConsoleUI consoleUI;

    public RentCommand(KickboardConsoleUI consoleUI) {
        this.consoleUI = consoleUI;
    }

    @Override
    public void execute() {
        consoleUI.rentKickboard();
    }
}

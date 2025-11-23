package com.kickboard.ui.command;

import com.kickboard.ui.KickboardConsoleUI;

public class DrivingStatusCommand implements Command {
    private final KickboardConsoleUI consoleUI;

    public DrivingStatusCommand(KickboardConsoleUI consoleUI) {
        this.consoleUI = consoleUI;
    }

    @Override
    public void execute() {
        consoleUI.showDrivingStatus();
    }
}

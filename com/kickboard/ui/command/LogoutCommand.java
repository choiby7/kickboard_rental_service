package com.kickboard.ui.command;

import com.kickboard.ui.KickboardConsoleUI;

public class LogoutCommand implements Command {
    private final KickboardConsoleUI consoleUI;

    public LogoutCommand(KickboardConsoleUI consoleUI) {
        this.consoleUI = consoleUI;
    }

    @Override
    public void execute() {
        consoleUI.logoutUser();
    }
}

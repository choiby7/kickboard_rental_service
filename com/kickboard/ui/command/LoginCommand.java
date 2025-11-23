package com.kickboard.ui.command;

import com.kickboard.ui.KickboardConsoleUI;

public class LoginCommand implements Command {
    private final KickboardConsoleUI consoleUI;

    public LoginCommand(KickboardConsoleUI consoleUI) {
        this.consoleUI = consoleUI;
    }

    @Override
    public void execute() {
        consoleUI.loginUser();
    }
}

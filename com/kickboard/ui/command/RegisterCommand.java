package com.kickboard.ui.command;

import com.kickboard.ui.KickboardConsoleUI;

public class RegisterCommand implements Command {
    private final KickboardConsoleUI consoleUI;

    public RegisterCommand(KickboardConsoleUI consoleUI) {
        this.consoleUI = consoleUI;
    }

    @Override
    public void execute() {
        consoleUI.registerUser();
    }
}

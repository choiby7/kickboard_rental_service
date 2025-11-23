package com.kickboard.ui.command;

import com.kickboard.ui.KickboardConsoleUI;

public class UserMenuCommand implements Command {
    private final KickboardConsoleUI consoleUI;

    public UserMenuCommand(KickboardConsoleUI consoleUI) {
        this.consoleUI = consoleUI;
    }

    @Override
    public void execute() {
        consoleUI.handleUserMenu();
    }
}

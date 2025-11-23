package com.kickboard.ui.command;

import com.kickboard.ui.KickboardConsoleUI;

public class ReturnCommand implements Command {
    private final KickboardConsoleUI consoleUI;

    public ReturnCommand(KickboardConsoleUI consoleUI) {
        this.consoleUI = consoleUI;
    }

    @Override
    public void execute() {
        consoleUI.returnKickboard();
    }
}

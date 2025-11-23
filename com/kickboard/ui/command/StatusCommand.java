package com.kickboard.ui.command;

import com.kickboard.ui.KickboardConsoleUI;

public class StatusCommand implements Command {
    private final KickboardConsoleUI consoleUI;

    public StatusCommand(KickboardConsoleUI consoleUI) {
        this.consoleUI = consoleUI;
    }

    @Override
    public void execute() {
        consoleUI.displayKickboardStatus();
    }
}

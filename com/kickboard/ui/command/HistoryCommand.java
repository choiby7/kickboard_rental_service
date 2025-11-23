package com.kickboard.ui.command;

import com.kickboard.ui.KickboardConsoleUI;

public class HistoryCommand implements Command {
    private final KickboardConsoleUI consoleUI;

    public HistoryCommand(KickboardConsoleUI consoleUI) {
        this.consoleUI = consoleUI;
    }

    @Override
    public void execute() {
        consoleUI.showRentalHistory();
    }
}

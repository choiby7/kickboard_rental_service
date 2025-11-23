package com.kickboard.ui.command;

import com.kickboard.ui.KickboardConsoleUI;

public class ExitCommand implements Command {
    private final KickboardConsoleUI consoleUI;

    public ExitCommand(KickboardConsoleUI consoleUI) {
        this.consoleUI = consoleUI;
    }

    @Override
    public void execute() {
        consoleUI.exitApplication();
    }
}

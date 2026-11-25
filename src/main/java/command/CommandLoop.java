package command;

import exception.InputEndedException;
import exception.InvalidInputException;
import exception.LoanLoadException;
import view.ConsoleView;

import java.util.List;
import java.util.Optional;

public class CommandLoop {

    private final ConsoleView view;
    private final CommandRegistry registry;

    public CommandLoop(ConsoleView view, CommandRegistry registry) {
        this.view = view;
        this.registry = registry;
    }

    public void run() {
        view.showMessage("Credit Simulator - ketik 'show' untuk melihat semua perintah.");
        while (true) {
            String line = readLine();
            if (line == null) {
                return;
            }
            if (line.isBlank()) {
                continue;
            }
            if (handle(line.trim()) == CommandResult.EXIT) {
                view.showMessage("Sampai jumpa.");
                return;
            }
            view.showMessage("");
        }
    }

    private String readLine() {
        try {
            return view.ask("Perintah");
        } catch (InputEndedException e) {
            view.showMessage("");
            view.showMessage("Input berakhir, keluar.");
            return null;
        }
    }

    private CommandResult handle(String line) {
        String[] parts = line.split("\\s+");
        Optional<Command> command = registry.find(parts[0]);
        if (command.isEmpty()) {
            view.showError("Perintah tidak dikenal: '" + parts[0]
                    + "'. Ketik 'show' untuk melihat semua perintah.");
            return CommandResult.CONTINUE;
        }
        List<String> args = List.of(parts).subList(1, parts.length);
        try {
            return command.get().execute(args);
        } catch (InvalidInputException | LoanLoadException e) {
            view.showError(e.getMessage());
            return CommandResult.CONTINUE;
        }
    }
}
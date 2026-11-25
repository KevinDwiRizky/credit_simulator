package command;

import java.util.List;

public final class ExitCommand extends AbstractCommand {

    public ExitCommand() {
        super("exit", "exit", "Keluar dari aplikasi", 0, 0);
    }

    @Override
    protected CommandResult run(List<String> args) {
        return CommandResult.EXIT;
    }
}
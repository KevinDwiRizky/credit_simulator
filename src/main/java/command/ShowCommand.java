package command;

import view.ConsoleView;

import java.util.List;

public final class ShowCommand extends AbstractCommand {

    private final CommandRegistry registry;
    private final ConsoleView view;

    public ShowCommand(CommandRegistry registry, ConsoleView view) {
        super("show", "show", "Tampilkan semua perintah yang tersedia", 0, 0);
        this.registry = registry;
        this.view = view;
    }

    @Override
    protected CommandResult run(List<String> args) {
        view.showMessage("Perintah yang tersedia:");
        for (Command command : registry.all()) {
            view.showMessage(String.format("  %-16s %s", command.usage(), command.description()));
        }
        return CommandResult.CONTINUE;
    }
}
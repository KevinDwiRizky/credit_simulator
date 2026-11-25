package command;

import controller.CreditSimulatorController;

import java.util.List;

public final class NewCommand extends AbstractCommand {

    private final CreditSimulatorController controller;

    public NewCommand(CreditSimulatorController controller) {
        super("new", "new", "Hitung cicilan baru (data diketik)", 0, 0);
        this.controller = controller;
    }

    @Override
    protected CommandResult run(List<String> args) {
        controller.run();
        return CommandResult.CONTINUE;
    }
}
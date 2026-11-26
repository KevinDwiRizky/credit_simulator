package command;

import controller.CreditSimulatorController;
import service.Workbook;

import java.util.List;

public final class NewCommand extends AbstractCommand {

    private final CreditSimulatorController controller;
    private final Workbook workbook;

    public NewCommand(CreditSimulatorController controller, Workbook workbook) {
        super("new", "new", "Hitung cicilan baru (data diketik)", 0, 0);
        this.controller = controller;
        this.workbook = workbook;
    }

    @Override
    protected CommandResult run(List<String> args) {
        workbook.setCurrent(controller.run());
        return CommandResult.CONTINUE;
    }
}
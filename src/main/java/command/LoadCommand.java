package command;

import client.LoanDataSource;
import controller.CreditSimulatorController;
import service.Workbook;

import java.util.List;

public final class LoadCommand extends AbstractCommand {

    private final CreditSimulatorController controller;
    private final LoanDataSource source;
    private final Workbook workbook;

    public LoadCommand(CreditSimulatorController controller, LoanDataSource source, Workbook workbook) {
        super("load", "load", "Muat data dari web service, lalu hitung otomatis", 0, 0);
        this.controller = controller;
        this.source = source;
        this.workbook = workbook;
    }

    @Override
    protected CommandResult run(List<String> args) {
        workbook.setCurrent(controller.loadAndCalculate(source));
        return CommandResult.CONTINUE;
    }
}
package command;

import client.LoanDataSource;
import controller.CreditSimulatorController;

import java.util.List;

public final class LoadCommand extends AbstractCommand {

    private final CreditSimulatorController controller;
    private final LoanDataSource source;

    public LoadCommand(CreditSimulatorController controller, LoanDataSource source) {
        super("load", "load", "Muat data dari web service, lalu hitung otomatis", 0, 0);
        this.controller = controller;
        this.source = source;
    }

    @Override
    protected CommandResult run(List<String> args) {
        controller.loadAndCalculate(source);
        return CommandResult.CONTINUE;
    }
}
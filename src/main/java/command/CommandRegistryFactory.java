package command;

import client.LoanDataSource;
import controller.CreditSimulatorController;
import service.Workbook;
import view.ConsoleView;

public final class CommandRegistryFactory {

    private CommandRegistryFactory() {
    }

    public static CommandRegistry createDefault(ConsoleView view,
                                                CreditSimulatorController controller,
                                                LoanDataSource loanSource) {
        Workbook workbook = new Workbook();
        CommandRegistry registry = new CommandRegistry();
        registry.register(new ShowCommand(registry, view));
        registry.register(new NewCommand(controller, workbook));
        registry.register(new LoadCommand(controller, loanSource, workbook));
        registry.register(new SaveCommand(workbook, view));
        registry.register(new SheetCommand(workbook, view));
        registry.register(new ExitCommand());
        return registry;
    }
}
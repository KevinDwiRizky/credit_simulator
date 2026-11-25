package command;

import client.LoanDataSource;
import controller.CreditSimulatorController;
import view.ConsoleView;

public final class CommandRegistryFactory {

    private CommandRegistryFactory() {
    }

    public static CommandRegistry createDefault(ConsoleView view,
                                                CreditSimulatorController controller,
                                                LoanDataSource loanSource) {
        CommandRegistry registry = new CommandRegistry();
        registry.register(new ShowCommand(registry, view));
        registry.register(new NewCommand(controller));
        registry.register(new LoadCommand(controller, loanSource));
        registry.register(new ExitCommand());
        return registry;
    }
}
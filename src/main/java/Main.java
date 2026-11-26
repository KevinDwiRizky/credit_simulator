import client.JdkHttpFetcher;
import client.LoanDataSource;
import client.LoanRequestMapper;
import client.RemoteLoanDataSource;
import command.CommandLoop;
import command.CommandRegistryFactory;
import controller.CreditSimulatorController;
import exception.InputEndedException;
import exception.InvalidInputException;
import exception.LoanLoadException;
import service.LoanCalculator;
import service.StepUpInterestPolicy;
import util.InputFileLoader;
import validation.LoanValidator;
import view.ConsoleView;

import java.nio.file.Path;

public class Main {

    static final String DEFAULT_LOAD_URL =
            "https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e8091955666c";

    static final String LOAD_URL_ENV = "CREDIT_SIMULATOR_LOAD_URL";

    public static void main(String[] args) {
        int exitCode = run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int run(String[] args) {
        if (args.length > 1) {
            System.out.println("Pemakaian: credit_simulator [file_input.txt]");
            return 2;
        }
        boolean fromFile = args.length == 1;

        try {
            LoanValidator validator = new LoanValidator();
            ConsoleView view = fromFile
                    ? new ConsoleView(InputFileLoader.open(Path.of(args[0])), System.out, true)
                    : new ConsoleView();
            CreditSimulatorController controller = new CreditSimulatorController(
                    view,
                    validator,
                    new LoanCalculator(new StepUpInterestPolicy()),
                    !fromFile);

            if (fromFile) {
                controller.run();
            } else {
                LoanDataSource loanSource = new RemoteLoanDataSource(
                        new JdkHttpFetcher(), loadUrl(), new LoanRequestMapper(validator));
                new CommandLoop(view,
                        CommandRegistryFactory.createDefault(view, controller, loanSource)).run();
            }
            return 0;
        } catch (InvalidInputException | InputEndedException | LoanLoadException e) {
            System.out.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private static String loadUrl() {
        String fromEnv = System.getenv(LOAD_URL_ENV);
        return (fromEnv == null || fromEnv.isBlank()) ? DEFAULT_LOAD_URL : fromEnv.trim();
    }
}
import controller.CreditSimulatorController;
import exception.InputEndedException;
import exception.InvalidInputException;
import service.LoanCalculator;
import service.StepUpInterestPolicy;
import util.InputFileLoader;
import validation.LoanValidator;
import view.ConsoleView;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        int exitCode = run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /** @return 0 = sukses, 1 = input salah / file bermasalah, 2 = cara pakai salah */
    static int run(String[] args) {
        if (args.length > 1) {
            System.out.println("Pemakaian: credit_simulator [file_input.txt]");
            return 2;
        }
        boolean fromFile = args.length == 1;

        try {
            ConsoleView view = fromFile
                    ? new ConsoleView(InputFileLoader.open(Path.of(args[0])), System.out, true)
                    : new ConsoleView();

            new CreditSimulatorController(
                    view,
                    new LoanValidator(),
                    new LoanCalculator(new StepUpInterestPolicy()),
                    !fromFile).run();
            return 0;
        } catch (InvalidInputException | InputEndedException e) {
            System.out.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
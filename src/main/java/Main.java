import controller.CreditSimulatorController;
import exception.InputEndedException;
import exception.InvalidInputException;
import service.LoanCalculator;
import service.StepUpInterestPolicy;
import validation.LoanValidator;
import view.ConsoleView;

public class Main {

    public static void main(String[] args) {
        ConsoleView view = new ConsoleView();
        CreditSimulatorController controller = new CreditSimulatorController(
                view,
                new LoanValidator(),
                new LoanCalculator(new StepUpInterestPolicy()),
                true);

        try {
            controller.run();
        } catch (InvalidInputException | InputEndedException e) {
            view.showError(e.getMessage());
            System.exit(1);
        }
    }
}
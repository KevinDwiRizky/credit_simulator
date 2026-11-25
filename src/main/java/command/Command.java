package command;

import java.util.List;

public interface Command {

    String name();

    String usage();

    String description();

    CommandResult execute(List<String> args);
}

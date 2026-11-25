package command;

import exception.InvalidInputException;

import java.util.List;
import java.util.Locale;

public abstract class AbstractCommand implements Command {

    private final String name;
    private final String usage;
    private final String description;
    private final int minArgs;
    private final int maxArgs;

    protected AbstractCommand(String name, String usage, String description, int minArgs, int maxArgs) {
        if (!name.equals(name.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Nama perintah harus huruf kecil: " + name);
        }
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String usage() {
        return usage;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public final CommandResult execute(List<String> args) {
        if (args.size() < minArgs || args.size() > maxArgs) {
            throw new InvalidInputException("Pemakaian: " + usage);
        }
        return run(args);
    }

    protected abstract CommandResult run(List<String> args);
}
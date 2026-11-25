package command;

import java.util.*;

public class CommandRegistry {

    private final Map<String, Command> commands = new LinkedHashMap<>();

    public void register(Command command) {
        if (commands.putIfAbsent(command.name(), command) != null) {
            throw new IllegalArgumentException("Perintah sudah terdaftar: " + command.name());
        }
    }

    public Optional<Command> find(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase(Locale.ROOT)));
    }

    public List<Command> all() {
        return List.copyOf(commands.values());
    }
}
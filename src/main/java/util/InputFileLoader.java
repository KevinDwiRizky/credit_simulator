package util;

import exception.InvalidInputException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class InputFileLoader {

    private static final String BOM = "\uFEFF";

    private InputFileLoader() {
    }

    public static BufferedReader open(Path path) {
        if (!Files.exists(path)) {
            throw new InvalidInputException("File input tidak ditemukan: " + path);
        }
        if (!Files.isRegularFile(path)) {
            throw new InvalidInputException("Path bukan sebuah file: " + path);
        }

        List<String> rawLines;
        try {
            rawLines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InvalidInputException("Gagal membaca file " + path + ": " + e.getMessage());
        }

        List<String> answers = new ArrayList<>();
        for (int i = 0; i < rawLines.size(); i++) {
            String line = rawLines.get(i);
            if (i == 0 && line.startsWith(BOM)) {
                line = line.substring(1);
            }
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            answers.add(line);
        }
        return new BufferedReader(new StringReader(String.join("\n", answers)));
    }
}
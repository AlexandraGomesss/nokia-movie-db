package com.alexandra.nokia.cli;

import java.util.ArrayList;
import java.util.List;

public class CommandTokenizer {
    public static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        if (input == null) return tokens;

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (!inQuotes && Character.isWhitespace(c)) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (inQuotes) throw new IllegalArgumentException("Bad input format: unclosed quotes");
        if (!current.isEmpty()) tokens.add(current.toString());

        return tokens;
    }
}

package com.alexandra.nokia.cli;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ListQueryParser {

    public static ListQuery parse(List<String> tokens) {
        boolean verbose = false;
        String titleRegex = null;
        String directorRegex = null;
        String actorRegex = null;
        boolean la = false;
        boolean ld = false;

        for (int i = 1; i < tokens.size(); i++) {
            String t = tokens.get(i);

            switch (t) {
                case "-v" -> verbose = true;

                case "-la" -> {
                    if (ld) throw new IllegalArgumentException("Bad input format: both -la and -ld present");
                    la = true;
                }

                case "-ld" -> {
                    if (la) throw new IllegalArgumentException("Bad input format: both -la and -ld present");
                    ld = true;
                }

                case "-t", "-d", "-a" -> {
                    if (i + 1 >= tokens.size())
                        throw new IllegalArgumentException("Bad input format: missing parameter after " + t);

                    String param = tokens.get(i + 1);
                    i++;

                    assertValidRegex(param);

                    if (t.equals("-t")) titleRegex = param;
                    if (t.equals("-d")) directorRegex = param;
                    if (t.equals("-a")) actorRegex = param;
                }

                default -> throw new IllegalArgumentException("Bad input format: unknown switch " + t);
            }
        }

        ListQuery.Ordering ordering =
                la ? ListQuery.Ordering.LENGTH_ASC :
                        ld ? ListQuery.Ordering.LENGTH_DESC :
                                ListQuery.Ordering.TITLE_DEFAULT;

        return new ListQuery(verbose, titleRegex, directorRegex, actorRegex, ordering);
    }

    private static void assertValidRegex(String regex) {
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Bad input format: corrupted regex");
        }
    }
}

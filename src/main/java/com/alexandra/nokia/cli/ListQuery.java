package com.alexandra.nokia.cli;

public record ListQuery(
        boolean verbose,
        String titleRegex,
        String directorRegex,
        String actorRegex,
        Ordering ordering
) {
    public enum Ordering { TITLE_DEFAULT, LENGTH_ASC, LENGTH_DESC }
}

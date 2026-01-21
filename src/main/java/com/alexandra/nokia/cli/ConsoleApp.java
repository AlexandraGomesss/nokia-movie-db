package com.alexandra.nokia.cli;

import com.alexandra.nokia.model.Movie;
import com.alexandra.nokia.model.MovieWithActors;
import com.alexandra.nokia.service.MovieService;
import com.alexandra.nokia.util.TimeUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {
    private final MovieService service;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleApp(MovieService service) {
        this.service = service;
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== MENU ===");
        System.out.println("1) List movies");
        System.out.println("   - type: l");
        System.out.println("   - with details (actors): l -v");
        System.out.println("   - filter by title:   l -t \"Die .*\" -v");
        System.out.println("   - filter by director: l -d \"Spielberg\" ");
        System.out.println("   - filter by actor:    l -a \"Liam.*\" ");
        System.out.println("   - sort by duration:   l -la (asc)  |  l -ld (desc)");
        System.out.println();
        System.out.println("2) Add person (actor/director)");
        System.out.println("   - type: a -p");
        System.out.println();
        System.out.println("3) Add movie");
        System.out.println("   - type: a -m");
        System.out.println("   (Note: the director and actors must already exist as people and the name must match EXACTLY)");
        System.out.println();
        System.out.println("4) Delete person");
        System.out.println("   - type: d -p Exact Name");
        System.out.println("   (If this person is a director in any movie, deletion is not allowed)");
        System.out.println();
        System.out.println("Other:");
        System.out.println("   - menu   (show this menu again)");
        System.out.println("   - help   (show quick examples)");
        System.out.println("   - exit   (quit)");
        System.out.println("============");
        System.out.println();
    }

    public void run() {
        printMenu();

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line == null) continue;

            line = line.trim();
            if (line.equalsIgnoreCase("exit")) break;
            if (line.isEmpty()) continue;

            if (line.equalsIgnoreCase("menu")) {
                printMenu();
                continue;
            }

            if (line.equalsIgnoreCase("help")) {
                System.out.println("Examples:");
                System.out.println("  l");
                System.out.println("  l -v");
                System.out.println("  l -t \"Star.*\" -v");
                System.out.println("  a -p");
                System.out.println("  a -m");
                System.out.println("  d -p George Lucas");
                continue;
            }

            try {
                handleLine(line);
            } catch (Exception e) {
                System.out.println("- " + e.getMessage());
                System.out.println("- Type 'menu' to see the available options.");
            }
        }
    }

    private void handleLine(String line) throws Exception {
        List<String> tokens = CommandTokenizer.tokenize(line);
        if (tokens.isEmpty()) return;

        switch (tokens.get(0)) {
            case "l" -> handleList(tokens);
            case "a" -> handleAdd(tokens);
            case "d" -> handleDelete(tokens);
            default -> throw new IllegalArgumentException("Unknown command");
        }
    }

    private void handleList(List<String> tokens) throws SQLException {
        ListQuery q = ListQueryParser.parse(tokens);

        if (!q.verbose()) {
            for (Movie m : service.listMovies(q)) {
                System.out.println(formatMovieLine(m));
            }
        } else {
            for (MovieWithActors mwa : service.listMoviesVerbose(q)) {
                System.out.println(formatMovieLine(mwa.movie()));
                System.out.println("\tStarring:");
                for (var actor : mwa.actors()) {
                    System.out.println("\t\t- " + actor.name());
                }
            }
        }
    }

    private String formatMovieLine(Movie m) {
        return m.title() + " by " + m.director().name() + ", " +
                TimeUtil.formatSecondsToHhMmSs(m.lengthSeconds());
    }

    private void handleAdd(List<String> tokens) throws Exception {
        if (tokens.size() < 2) throw new IllegalArgumentException("Bad input format: use a -p or a -m");

        if (tokens.get(1).equals("-p")) {
            addPersonFlow();
            return;
        }
        if (tokens.get(1).equals("-m")) {
            addMovieFlow();
            return;
        }
        throw new IllegalArgumentException("Bad input format: unknown add switch");
    }

    private void addPersonFlow() throws Exception {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Nationality: ");
        String nat = scanner.nextLine().trim();

        if (name.isEmpty() || nat.isEmpty()) {
            throw new IllegalArgumentException("Name and nationality are required");
        }

        service.addPerson(name, nat);
        System.out.println("- Person added");
    }

    private void addMovieFlow() throws Exception {
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) throw new IllegalArgumentException("Title is required");

        int lengthSeconds;
        while (true) {
            System.out.print("Length: ");
            String raw = scanner.nextLine().trim();
            try {
                lengthSeconds = TimeUtil.parseHhMmSsToSeconds(raw);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("- Bad input format (hh:mm:ss), try again!");
            }
        }

        String directorName;
        while (true) {
            System.out.print("Director (exact existing name): ");
            directorName = scanner.nextLine().trim();
            if (directorName.isEmpty()) continue;

            if (service.findPersonExact(directorName).isPresent()) break;
            System.out.println("- We could not find \"" + directorName + "\". Use 'a -p' to add the person first, then try again."); //helping the user a little more

        }

        System.out.println("Starring (exact existing names). Type 'exit' to finish:");

        List<String> actorNames = new ArrayList<>();
        while (true) {
            String actor = scanner.nextLine().trim();
            if (actor.equalsIgnoreCase("exit")) break;
            if (actor.isEmpty()) continue;

            if (service.findPersonExact(actor).isPresent()) {
                actorNames.add(actor);
            } else {
                System.out.println("- We could not find \"" + actor + "\", try again!");
            }
        }

        service.addMovie(title, lengthSeconds, directorName, actorNames);
        System.out.println("- Movie saved");
    }

    private void handleDelete(List<String> tokens) throws SQLException {
        if (tokens.size() < 3) throw new IllegalArgumentException("Bad input format: use d -p <exactName>");
        if (!tokens.get(1).equals("-p")) throw new IllegalArgumentException("Bad input format: only d -p is supported");

        String exactName = String.join(" ", tokens.subList(2, tokens.size())).trim();
        if (exactName.isEmpty()) throw new IllegalArgumentException("Bad input format: missing person name");

        service.deletePersonByExactName(exactName);
        System.out.println("- Person deleted");
    }
}

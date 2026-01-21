package com.alexandra.nokia;

import com.alexandra.nokia.cli.ConsoleApp;
import com.alexandra.nokia.db.Database;
import com.alexandra.nokia.db.Schema;
import com.alexandra.nokia.service.MovieService;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:h2:mem:moviedb;DB_CLOSE_DELAY=-1";


        try (Connection conn = new Database(url).connect()) {
            Schema.init(conn);
            System.out.println("Connected to database successfully.");

            MovieService service = new MovieService(conn);
            new ConsoleApp(service).run();

        } catch (Exception e) {
            System.out.println("Failed to connect to database. Terminating.");
            System.exit(1);
        }
    }
}

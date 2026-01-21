package com.alexandra.nokia.service;

import com.alexandra.nokia.cli.ListQuery;
import com.alexandra.nokia.model.Movie;
import com.alexandra.nokia.model.MovieWithActors;
import com.alexandra.nokia.model.Person;
import com.alexandra.nokia.repo.MovieRepository;
import com.alexandra.nokia.repo.PersonRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MovieService {
    private final PersonRepository people;
    private final MovieRepository movies;

    public MovieService(Connection conn) {
        this.people = new PersonRepository(conn);
        this.movies = new MovieRepository(conn);
    }
    public Person addPerson(String name, String nationality) throws SQLException {
        try {
            return people.insert(name, nationality);
        } catch (SQLException e) {
            // H2 duplicate key often contains "Unique index or primary key violation"
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                throw new IllegalArgumentException("Person already exists: \"" + name + "\"");
            }
            throw e;
        }
    }

    public Optional<Person> findPersonExact(String name) throws SQLException {
        return people.findByNameExact(name);
    }

    public void addMovie(String title, int lengthSeconds, String directorName, List<String> actorNames) throws SQLException {
        Person director = people.findByNameExact(directorName)
                .orElseThrow(() -> new IllegalArgumentException("We could not find \"" + directorName + "\""));

        // Same title + same director => same movie record
        Optional<Long> existingId = movies.findMovieIdByTitleAndDirector(title, director.id());
        long movieId;

        if (existingId.isPresent()) {
            movieId = existingId.get();
            movies.updateMovieLength(movieId, lengthSeconds);
        } else {
            movieId = movies.insertMovie(title, lengthSeconds, director.id());
        }

        for (String actorName : actorNames) {
            Person actor = people.findByNameExact(actorName)
                    .orElseThrow(() -> new IllegalArgumentException("We could not find \"" + actorName + "\""));
            movies.addActorToMovie(movieId, actor.id());
        }
    }

    public List<Movie> listMovies(ListQuery q) throws SQLException {
        return movies.listMovies(q);
    }

    public List<MovieWithActors> listMoviesVerbose(ListQuery q) throws SQLException {
        return movies.listMoviesVerbose(q);
    }

    public void deletePersonByExactName(String exactName) throws SQLException {
        Person p = people.findByNameExact(exactName)
                .orElseThrow(() -> new IllegalStateException("Person not found"));

        if (people.isDirectorInAnyMovie(p.id())) {
            throw new IllegalStateException("Cannot delete person: they is a director in at least one movie");
        }

        people.deletePerson(p.id());
    }
}

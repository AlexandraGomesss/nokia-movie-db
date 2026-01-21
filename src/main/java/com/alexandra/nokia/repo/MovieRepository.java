package com.alexandra.nokia.repo;

import com.alexandra.nokia.cli.ListQuery;
import com.alexandra.nokia.model.Movie;
import com.alexandra.nokia.model.MovieWithActors;
import com.alexandra.nokia.model.Person;

import java.sql.*;
import java.util.*;

public class MovieRepository {
    private final Connection conn;

    public MovieRepository(Connection conn) {
        this.conn = conn;
    }

    public Optional<Long> findMovieIdByTitleAndDirector(String title, long directorId) throws SQLException {
        String sql = "SELECT id FROM movies WHERE title = ? AND director_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setLong(2, directorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(rs.getLong("id"));
            }
        }
    }

    public long insertMovie(String title, int lengthSeconds, long directorId) throws SQLException {
        String sql = "INSERT INTO movies(title, length_seconds, director_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setInt(2, lengthSeconds);
            ps.setLong(3, directorId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void updateMovieLength(long movieId, int lengthSeconds) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE movies SET length_seconds = ? WHERE id = ?")) {
            ps.setInt(1, lengthSeconds);
            ps.setLong(2, movieId);
            ps.executeUpdate();
        }
    }

    public void addActorToMovie(long movieId, long actorId) throws SQLException {

        String sql = "MERGE INTO movie_actors(movie_id, actor_id) KEY(movie_id, actor_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, movieId);
            ps.setLong(2, actorId);
            ps.executeUpdate();
        }
    }

    public List<Movie> listMovies(ListQuery q) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT m.id AS movie_id, m.title, m.length_seconds,
                   d.id AS director_id, d.name AS director_name, d.nationality AS director_nat
            FROM movies m
            JOIN people d ON d.id = m.director_id
        """);

        List<Object> params = new ArrayList<>();
        List<String> where = new ArrayList<>();

        if (q.titleRegex() != null) {
            where.add("REGEXP_LIKE(m.title, ?)");
            params.add(q.titleRegex());
        }
        if (q.directorRegex() != null) {
            where.add("REGEXP_LIKE(d.name, ?)");
            params.add(q.directorRegex());
        }
        if (q.actorRegex() != null) {
            where.add("""
                EXISTS (
                  SELECT 1
                  FROM movie_actors ma
                  JOIN people a ON a.id = ma.actor_id
                  WHERE ma.movie_id = m.id AND REGEXP_LIKE(a.name, ?)
                )
            """);
            params.add(q.actorRegex());
        }

        if (!where.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", where));
        }

        if (q.ordering() == ListQuery.Ordering.LENGTH_ASC) {
            sql.append(" ORDER BY m.length_seconds ASC, m.title ASC");
        } else if (q.ordering() == ListQuery.Ordering.LENGTH_DESC) {
            sql.append(" ORDER BY m.length_seconds DESC, m.title ASC");
        } else {
            sql.append(" ORDER BY m.title ASC");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Movie> out = new ArrayList<>();
                while (rs.next()) {
                    Person director = new Person(
                            rs.getLong("director_id"),
                            rs.getString("director_name"),
                            rs.getString("director_nat")
                    );
                    out.add(new Movie(
                            rs.getLong("movie_id"),
                            rs.getString("title"),
                            rs.getInt("length_seconds"),
                            director
                    ));
                }
                return out;
            }
        }
    }

    public List<Person> findActorsForMovie(long movieId) throws SQLException {
        String sql = """
            SELECT p.id, p.name, p.nationality
            FROM movie_actors ma
            JOIN people p ON p.id = ma.actor_id
            WHERE ma.movie_id = ?
            ORDER BY p.name ASC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Person> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new Person(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("nationality")
                    ));
                }
                return out;
            }
        }
    }

    public List<MovieWithActors> listMoviesVerbose(ListQuery q) throws SQLException {
        List<Movie> movies = listMovies(q);
        List<MovieWithActors> out = new ArrayList<>();
        for (Movie m : movies) {
            out.add(new MovieWithActors(m, findActorsForMovie(m.id())));
        }
        return out;
    }
}

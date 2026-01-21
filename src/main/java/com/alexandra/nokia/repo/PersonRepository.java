package com.alexandra.nokia.repo;

import com.alexandra.nokia.model.Person;

import java.sql.*;
import java.util.Optional;

public class PersonRepository {
    private final Connection conn;

    public PersonRepository(Connection conn) {
        this.conn = conn;
    }

    public Optional<Person> findByNameExact(String name) throws SQLException {
        String sql = "SELECT id, name, nationality FROM people WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Person(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("nationality")
                ));
            }
        }
    }

    public Person insert(String name, String nationality) throws SQLException {
        String sql = "INSERT INTO people(name, nationality) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, nationality);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Person(keys.getLong(1), name, nationality);
            }
        }
    }

    public boolean isDirectorInAnyMovie(long personId) throws SQLException {
        String sql = "SELECT 1 FROM movies WHERE director_id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, personId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void deletePerson(long personId) throws SQLException {
        // requirement: delete them from starring
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM movie_actors WHERE actor_id = ?")) {
            ps.setLong(1, personId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM people WHERE id = ?")) {
            ps.setLong(1, personId);
            int changed = ps.executeUpdate();
            if (changed == 0) {
                throw new IllegalStateException("Person not found");
            }
        }
    }
}

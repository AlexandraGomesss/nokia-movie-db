package com.alexandra.nokia.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Schema {
    public static void init(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS people (
                  id IDENTITY PRIMARY KEY,
                  name VARCHAR(255) NOT NULL UNIQUE,
                  nationality VARCHAR(100) NOT NULL
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS movies (
                  id IDENTITY PRIMARY KEY,
                  title VARCHAR(255) NOT NULL,
                  length_seconds INT NOT NULL,
                  director_id BIGINT NOT NULL,
                  CONSTRAINT fk_movies_director FOREIGN KEY (director_id) REFERENCES people(id),
                  CONSTRAINT uq_movie_title_director UNIQUE (title, director_id)
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS movie_actors (
                  movie_id BIGINT NOT NULL,
                  actor_id BIGINT NOT NULL,
                  PRIMARY KEY (movie_id, actor_id),
                  CONSTRAINT fk_ma_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
                  CONSTRAINT fk_ma_actor FOREIGN KEY (actor_id) REFERENCES people(id) ON DELETE CASCADE
                );
            """);
        }
    }
}


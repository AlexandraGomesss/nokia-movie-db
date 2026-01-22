
# Developer Notes

## Quick overview
- Console app using Java + JDBC
- H2 database (in-memory by default)
- Tables: people, movies, movie_actors

## Why a `people` table?
Actors and directors share the same basic fields:
- name
- nationality

So I store both in a single `people` table to avoid duplicating data.
A movie stores:
- one director via `movies.director_id`
- multiple actors via the join table `movie_actors`

## Movie uniqueness rule (title + director)
The requirement says movies can share the same title, but:
- same title + same director should be treated as the same movie

I enforce that with a database constraint:
- `UNIQUE(title, director_id)`

In code, when adding a movie:
- if (title + director) already exists → update length and add actors (without duplicates)
- else → insert a new movie row

## Delete person rule
When deleting a person:
- it is not allowed if that person is a director of any movie
- otherwise, the person is removed and also removed from casting (mo

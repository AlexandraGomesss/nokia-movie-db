# Nokia Movie DB (Console App)

Java console application for managing a small movie database.

The app stores:
- Movie title
- Movie director
- Movie actors
- Movie length (stored as seconds, shown as `hh:mm:ss`)
- Person nationality (for directors and actors)

Movies with the same **title + director** are treated as the same movie.

This project uses:
- Java (console app)
- Maven
- H2 database
- JDBC (no frameworks)

---

## Features

### Menu / Commands
When the app starts, it prints a menu. You can type commands at the `>` prompt.

#### List movies
- `l`  
  Lists movies line by line:  
  `title by director, hh:mm:ss`

- `l -v`  
  Verbose mode: prints actors under each movie.

Optional filters (regex must be in quotes):
- `l -t "Die .*"` filters by title
- `l -d "Spielberg"` filters by director name
- `l -a "Liam.*"` filters by actor name (movie must have at least one matching actor)

Ordering:
- Default: alphabetical by title
- `l -la` length ascending (ties sorted by title)
- `l -ld` length descending (ties sorted by title)

Switches can be in any order. Wrong formats are handled with a friendly error message.

#### Add data
- `a -p`  
  Adds a person (actors/directors are both “people”): prompts for name + nationality.  
  Names must be unique.

- `a -m`  
  Adds a movie: prompts for title, length (`hh:mm:ss`), director name, then actors line-by-line.  
  Director and actors must already exist as people (exact name match).  
  Type `exit` to finish the actors list.

#### Delete person
- `d -p Exact Name`  
  Deletes a person by exact name and removes them from any movie casting.
  If the person is a director of any movie, deletion is not allowed (exception).

#### Extra convenience
- `menu` shows menu again
- `help` prints quick examples
- `exit` closes the app

---

## Database choice

This project uses **H2 in-memory** database by default:

`jdbc:h2:mem:moviedb;DB_CLOSE_DELAY=-1`

This means:
- Data exists while the app is running
- When the app exits, data is cleared
- This is ideal for tests/review because every run starts clean

If you want persistence, you can switch to file-based H2 in `Main.java`.

---

## How to run

### Option A: IntelliJ
1. Open the project
2. Maven reload (if requested)
3. Run `Main.java`

### Option B: Terminal (from project root)
Compile and run:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass=com.alexandra.nokia.Main

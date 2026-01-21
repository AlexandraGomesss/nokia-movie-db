package com.alexandra.nokia.model;

import java.util.List;

public record MovieWithActors(Movie movie, List<Person> actors) {}

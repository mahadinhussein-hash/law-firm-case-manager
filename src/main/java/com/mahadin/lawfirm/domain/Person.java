package com.mahadin.lawfirm.domain;

import com.mahadin.lawfirm.exception.ValidationException;

import java.util.Objects;

public abstract class Person {

    private final int id;
    private final String name;

    protected Person(int id, String name) {
        if (id <= 0) {
            throw new ValidationException("Id must be positive.");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Name cannot be empty.");
        }
        if (name.trim().split("\\s+").length < 2) {
            throw new ValidationException("Enter the first name and the last name.");
        }
        this.id = id;
        this.name = name.trim();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Person person = (Person) other;
        return id == person.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

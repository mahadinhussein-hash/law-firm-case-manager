package com.mahadin.lawfirm.domain;

import com.mahadin.lawfirm.exception.ValidationException;

import java.io.Serializable;
import java.util.Objects;

public abstract class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;

    protected Person(int id, String name) {
        if (id <= 0) {
            throw new ValidationException("Id must be positive.");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Name cannot be empty.");
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

package com.mahadin.lawfirm.domain; // package that groups classes together

import com.mahadin.lawfirm.exception.ValidationException;

import java.io.Serializable; // import allows me to use Serializable instead of full name
import java.util.Objects; // imports helper methods for working with objects

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

    //public means other classes can use this method to return this data
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

//This method compares two objects and returns true if they are the same kind of person with the same ID
    // used my own method overide doubles checks if it was done correctly
    //my method checks if ids are equal normal checks if they are the exact same one
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

//calculate the hashcode and use the id to get the hash code
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

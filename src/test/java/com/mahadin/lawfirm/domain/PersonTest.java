package com.mahadin.lawfirm.domain;

import com.mahadin.lawfirm.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonTest {

    @Test
    void clientNeedsValidEmail() {
        assertThrows(ValidationException.class, () ->
                new Client(1, "Anna Kovacs", "not-an-email", "+36 30 123 4567"));
    }

    @Test
    void personNeedsName() {
        assertThrows(ValidationException.class, () -> new Lawyer(1, "  ", 100.0));
    }

    @Test
    void lawyerNeedsPositiveRate() {
        assertThrows(ValidationException.class, () -> new Lawyer(1, "Peter Nagy", 0));
    }

    @Test
    void nameIsTrimmed() {
        Client client = new Client(1, "  Anna Kovacs  ", "anna@example.com", "123");

        assertEquals("Anna Kovacs", client.getName());
    }

    @Test
    void sameIdMeansEqual() {
        Client first = new Client(1, "Anna Kovacs", "anna@example.com", "123");
        Client second = new Client(1, "Anna K.", "anna.k@example.com", "456");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void clientAndLawyerWithSameIdAreNotEqual() {
        Client client = new Client(1, "Anna Kovacs", "anna@example.com", "123");
        Lawyer lawyer = new Lawyer(1, "Anna Kovacs", 100.0);

        assertNotEquals(client, lawyer);
    }

    @Test
    void timeEntryHoursMustBeBetweenZeroAndTwentyFour() {
        LocalDate today = LocalDate.of(2026, 11, 1);

        assertThrows(ValidationException.class, () -> new TimeEntry(today, 0, "Nothing"));
        assertThrows(ValidationException.class, () -> new TimeEntry(today, 25, "Too much"));
    }
}

package com.example.notes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class NoteResponseTest {

    @Test
    void fromCopiesAllFieldsOfTheNote() {
        Note note = new Note("Title", "Body");
        NoteResponse response = NoteResponse.from(note);
        assertThat(response.getTitle()).isEqualTo("Title");
        assertThat(response.getContent()).isEqualTo("Body");
        assertThat(response.getCreatedAt()).isEqualTo(note.getCreatedAt());
    }
}

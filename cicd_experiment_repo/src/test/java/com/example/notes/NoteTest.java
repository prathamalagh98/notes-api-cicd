package com.example.notes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class NoteTest {

    @Test
    void constructorStoresTitleAndContent() {
        Note note = new Note("Title", "Body");
        assertThat(note.getTitle()).isEqualTo("Title");
        assertThat(note.getContent()).isEqualTo("Body");
    }

    @Test
    void newNoteHasNoIdAndHasCreationTime() {
        Note note = new Note("Title", "Body");
        assertThat(note.getId()).isNull();
        assertThat(note.getCreatedAt()).isNotNull();
    }
}

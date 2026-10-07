package com.example.notes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/** Integration test: runs against the real MySQL instance (service container in CI). */
@Tag("integration")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NoteRepositoryTest {

    @Autowired
    private NoteRepository repository;

    @Test
    void savePersistsNoteAndAssignsId() {
        Note saved = repository.save(new Note("Persisted", "body"));
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void findByTitleIgnoresCase() {
        repository.save(new Note("Weekly Plan", "mon-fri"));
        List<Note> found = repository.findByTitleContainingIgnoreCase("weekly plan");
        assertThat(found).extracting(Note::getTitle).contains("Weekly Plan");
    }

    @Test
    void findByTitleReturnsEmptyListWhenNothingMatches() {
        assertThat(repository.findByTitleContainingIgnoreCase("no-such-title-zzz")).isEmpty();
    }

    @Test
    void deleteRemovesTheRow() {
        Note saved = repository.save(new Note("Temporary", "x"));
        repository.deleteById(saved.getId());
        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}

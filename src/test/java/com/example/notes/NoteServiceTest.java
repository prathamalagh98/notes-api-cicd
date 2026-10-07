package com.example.notes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository repository;

    @InjectMocks
    private NoteService service;

    @Test
    void createTrimsTitleAndSaves() {
        when(repository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));
        Note saved = service.create("  Shopping  ", "milk");
        assertThat(saved.getTitle()).isEqualTo("Shopping");
        assertThat(saved.getContent()).isEqualTo("milk");
    }

    @Test
    void createRejectsBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> service.create("   ", "x"));
        verify(repository, never()).save(any(Note.class));
    }

    @Test
    void createRejectsNullTitle() {
        assertThrows(IllegalArgumentException.class, () -> service.create(null, "x"));
    }

    @Test
    void createRejectsTooLongTitle() {
        String longTitle = "a".repeat(NoteService.MAX_TITLE_LENGTH + 1);
        assertThrows(IllegalArgumentException.class, () -> service.create(longTitle, "x"));
    }

    @Test
    void createAcceptsTitleAtMaximumLength() {
        when(repository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));
        String title = "a".repeat(NoteService.MAX_TITLE_LENGTH);
        assertThat(service.create(title, "x").getTitle()).hasSize(NoteService.MAX_TITLE_LENGTH);
    }

    @Test
    void createDefaultsNullContentToEmptyString() {
        when(repository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));
        assertThat(service.create("Title", null).getContent()).isEmpty();
    }

    @Test
    void getReturnsExistingNote() {
        Note note = new Note("Title", "body");
        when(repository.findById(1L)).thenReturn(Optional.of(note));
        assertThat(service.get(1L)).isSameAs(note);
    }

    @Test
    void getThrowsWhenNoteIsMissing() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        assertThrows(NoteNotFoundException.class, () -> service.get(42L));
    }

    @Test
    void searchWithBlankTitleReturnsAllNotes() {
        when(repository.findAll()).thenReturn(List.of(new Note("A", "a"), new Note("B", "b")));
        assertThat(service.search("  ")).hasSize(2);
        verify(repository, never()).findByTitleContainingIgnoreCase(any(String.class));
    }

    @Test
    void searchDelegatesToRepositoryWithTrimmedTitle() {
        when(repository.findByTitleContainingIgnoreCase("plan")).thenReturn(List.of(new Note("Plan", "p")));
        assertThat(service.search(" plan ")).hasSize(1);
    }

    @Test
    void deleteThrowsWhenNoteIsMissing() {
        when(repository.existsById(7L)).thenReturn(false);
        assertThrows(NoteNotFoundException.class, () -> service.delete(7L));
        verify(repository, never()).deleteById(any(Long.class));
    }

    @Test
    void deleteRemovesExistingNote() {
        when(repository.existsById(7L)).thenReturn(true);
        service.delete(7L);
        verify(repository).deleteById(7L);
    }

    @Test
    void summaryKeepsShortContentUnchanged() {
        assertThat(service.summary(new Note("T", "short text"))).isEqualTo("short text");
    }

    @Test
    void summaryTruncatesLongContent() {
        String content = "x".repeat(NoteService.SUMMARY_LENGTH + 10);
        String summary = service.summary(new Note("T", content));
        assertThat(summary).endsWith("...").hasSize(NoteService.SUMMARY_LENGTH + 3);
    }
}

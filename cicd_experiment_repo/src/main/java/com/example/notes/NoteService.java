package com.example.notes;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    static final int MAX_TITLE_LENGTH = 100;
    static final int SUMMARY_LENGTH = 40;

    private final NoteRepository repository;

    public NoteService(NoteRepository repository) {
        this.repository = repository;
    }

    public Note create(String title, String content) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Title must be at most " + MAX_TITLE_LENGTH + " characters");
        }
        String safeContent = content == null ? "" : content;
        return repository.save(new Note(title.trim(), safeContent));
    }

    public Note get(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoteNotFoundException(id));
    }

    public List<Note> list() {
        return repository.findAll();
    }

    public List<Note> search(String title) {
        if (title == null || title.isBlank()) {
            return list();
        }
        return repository.findByTitleContainingIgnoreCase(title.trim());
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoteNotFoundException(id);
        }
        repository.deleteById(id);
    }

    public String summary(Note note) {
        String content = note.getContent();
        if (content.length() <= SUMMARY_LENGTH) {
            return content;
        }
        return content.substring(0, SUMMARY_LENGTH) + "...";
    }
}

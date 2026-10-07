package com.example.notes;

public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(Long id) {
        super("Note " + id + " not found");
    }
}

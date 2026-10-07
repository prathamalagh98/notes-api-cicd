package com.example.notes;

import java.time.Instant;

public class NoteResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final Instant createdAt;

    public NoteResponse(Long id, String title, String content, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static NoteResponse from(Note note) {
        return new NoteResponse(note.getId(), note.getTitle(), note.getContent(), note.getCreatedAt());
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

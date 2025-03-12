package com.example.demo.model;

import java.time.LocalDateTime;

/**
 * Model class representing a patient note.
 * Notes can be created by healthcare providers to document patient information
 * that doesn't fit into other structured data types.
 */
public class Note {
    
    private int noteId;
    private int patientId;
    private int authorId;
    private String authorName;
    private String title;
    private String content;
    private String noteType; // General, Progress, Medication, Lab, Other
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private boolean isPrivate;
    
    /**
     * Default constructor
     */
    public Note() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
    
    /**
     * Constructor with basic fields
     */
    public Note(int patientId, int authorId, String title, String content, String noteType) {
        this.patientId = patientId;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.noteType = noteType;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.isPrivate = false;
    }
    
    /**
     * Full constructor
     */
    public Note(int noteId, int patientId, int authorId, String authorName, String title, 
                String content, String noteType, LocalDateTime createdDate, 
                LocalDateTime updatedDate, boolean isPrivate) {
        this.noteId = noteId;
        this.patientId = patientId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.title = title;
        this.content = content;
        this.noteType = noteType;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.isPrivate = isPrivate;
    }
    
    // Getters and Setters
    
    public int getNoteId() {
        return noteId;
    }
    
    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }
    
    public int getPatientId() {
        return patientId;
    }
    
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }
    
    public int getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getNoteType() {
        return noteType;
    }
    
    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
    
    @Override
    public String toString() {
        return "Note{" +
                "noteId=" + noteId +
                ", patientId=" + patientId +
                ", title='" + title + '\'' +
                ", noteType='" + noteType + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
} 
package com.example.demo.database;

import com.example.demo.model.Note;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for handling database operations related to the Note entity.
 */
public class NoteDAO {
    
    private static final Logger LOGGER = Logger.getLogger(NoteDAO.class.getName());
    private final DatabaseConnection databaseConnection;
    
    /**
     * Constructor that initializes the database connection.
     */
    public NoteDAO() {
        this.databaseConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Creates a new note in the database.
     * 
     * @param note The note to create
     * @return true if successful, false otherwise
     */
    public boolean createNote(Note note) {
        String sql = "INSERT INTO notes (patient_id, author_id, author_name, title, content, note_type, created_date, updated_date, is_private) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, note.getPatientId());
            pstmt.setInt(2, note.getAuthorId());
            pstmt.setString(3, note.getAuthorName());
            pstmt.setString(4, note.getTitle());
            pstmt.setString(5, note.getContent());
            pstmt.setString(6, note.getNoteType());
            pstmt.setTimestamp(7, Timestamp.valueOf(note.getCreatedDate()));
            pstmt.setTimestamp(8, Timestamp.valueOf(note.getUpdatedDate()));
            pstmt.setBoolean(9, note.isPrivate());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, "Creating note failed, no rows affected.");
                return false;
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    note.setNoteId(generatedKeys.getInt(1));
                    return true;
                } else {
                    LOGGER.log(Level.WARNING, "Creating note failed, no ID obtained.");
                    return false;
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating note", e);
            return false;
        }
    }
    
    /**
     * Retrieves all notes for a specific patient.
     * 
     * @param patientId The ID of the patient
     * @return List of notes for the patient
     */
    public List<Note> getNotesByPatientId(int patientId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes WHERE patient_id = ? ORDER BY created_date DESC";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapResultSetToNote(rs));
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving notes for patient ID: " + patientId, e);
        }
        
        return notes;
    }
    
    /**
     * Retrieves a note by its ID.
     * 
     * @param noteId The ID of the note
     * @return The note, or null if not found
     */
    public Note getNoteById(int noteId) {
        String sql = "SELECT * FROM notes WHERE note_id = ?";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, noteId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNote(rs);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving note ID: " + noteId, e);
        }
        
        return null;
    }
    
    /**
     * Updates an existing note in the database.
     * 
     * @param note The note to update
     * @return true if successful, false otherwise
     */
    public boolean updateNote(Note note) {
        String sql = "UPDATE notes SET patient_id = ?, author_id = ?, author_name = ?, title = ?, " +
                     "content = ?, note_type = ?, updated_date = ?, is_private = ? " +
                     "WHERE note_id = ?";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, note.getPatientId());
            pstmt.setInt(2, note.getAuthorId());
            pstmt.setString(3, note.getAuthorName());
            pstmt.setString(4, note.getTitle());
            pstmt.setString(5, note.getContent());
            pstmt.setString(6, note.getNoteType());
            pstmt.setTimestamp(7, Timestamp.valueOf(note.getUpdatedDate()));
            pstmt.setBoolean(8, note.isPrivate());
            pstmt.setInt(9, note.getNoteId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating note ID: " + note.getNoteId(), e);
            return false;
        }
    }
    
    /**
     * Deletes a note from the database.
     * 
     * @param noteId The ID of the note to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteNote(int noteId) {
        String sql = "DELETE FROM notes WHERE note_id = ?";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, noteId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting note ID: " + noteId, e);
            return false;
        }
    }
    
    /**
     * Maps a ResultSet row to a Note object.
     * 
     * @param rs The ResultSet to map
     * @return The mapped Note
     * @throws SQLException If a database access error occurs
     */
    private Note mapResultSetToNote(ResultSet rs) throws SQLException {
        int noteId = rs.getInt("note_id");
        int patientId = rs.getInt("patient_id");
        int authorId = rs.getInt("author_id");
        String authorName = rs.getString("author_name");
        String title = rs.getString("title");
        String content = rs.getString("content");
        String noteType = rs.getString("note_type");
        LocalDateTime createdDate = rs.getTimestamp("created_date").toLocalDateTime();
        LocalDateTime updatedDate = rs.getTimestamp("updated_date").toLocalDateTime();
        boolean isPrivate = rs.getBoolean("is_private");
        
        return new Note(noteId, patientId, authorId, authorName, title, content, noteType, 
                        createdDate, updatedDate, isPrivate);
    }
    
    /**
     * Retrieves notes by author ID.
     * 
     * @param authorId The ID of the author
     * @return List of notes created by the author
     */
    public List<Note> getNotesByAuthorId(int authorId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes WHERE author_id = ? ORDER BY created_date DESC";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, authorId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapResultSetToNote(rs));
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving notes for author ID: " + authorId, e);
        }
        
        return notes;
    }
    
    /**
     * Searches for notes containing the given text in title or content.
     * 
     * @param searchTerm The term to search for
     * @param patientId Optional patient ID to limit the search (can be null)
     * @return List of matching notes
     */
    public List<Note> searchNotes(String searchTerm, Integer patientId) {
        List<Note> notes = new ArrayList<>();
        String sql;
        
        if (patientId != null) {
            sql = "SELECT * FROM notes WHERE patient_id = ? AND (title LIKE ? OR content LIKE ?) ORDER BY created_date DESC";
        } else {
            sql = "SELECT * FROM notes WHERE title LIKE ? OR content LIKE ? ORDER BY created_date DESC";
        }
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            
            if (patientId != null) {
                pstmt.setInt(1, patientId);
                pstmt.setString(2, searchPattern);
                pstmt.setString(3, searchPattern);
            } else {
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapResultSetToNote(rs));
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching notes with term: " + searchTerm, e);
        }
        
        return notes;
    }
} 
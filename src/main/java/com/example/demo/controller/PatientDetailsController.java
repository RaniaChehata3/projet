package com.example.demo.controller;

import com.example.demo.auth.AuthService;
import com.example.demo.database.PatientDAO;
import com.example.demo.model.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for the Patient Details view.
 * Displays detailed information about a patient and their medical history.
 */
public class PatientDetailsController {

    @FXML private Label patientNameLabel;
    @FXML private Label patientIdLabel;
    @FXML private Label ageLabel;
    @FXML private Label genderLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label addressLabel;
    @FXML private Label cityLabel;
    @FXML private Label bloodTypeLabel;
    @FXML private Label insuranceProviderLabel;
    @FXML private Label insuranceNumberLabel;
    @FXML private Label emergencyContactLabel;
    @FXML private Label emergencyPhoneLabel;
    @FXML private Label registrationDateLabel;
    @FXML private Label lastVisitDateLabel;
    @FXML private Label statusLabel;
    
    @FXML private TabPane medicalTabPane;
    @FXML private Tab medicalHistoryTab;
    @FXML private Tab medicationsTab;
    @FXML private Tab labResultsTab;
    @FXML private Tab notesTab;
    
    @FXML private TableView<MedicalRecord> medicalHistoryTable;
    @FXML private TableColumn<MedicalRecord, String> visitDateColumn;
    @FXML private TableColumn<MedicalRecord, String> diagnosisColumn;
    @FXML private TableColumn<MedicalRecord, String> doctorColumn;
    @FXML private TableColumn<MedicalRecord, String> recordTypeColumn;
    
    @FXML private TableView<Medication> medicationsTable;
    @FXML private TableColumn<Medication, String> medicationNameColumn;
    @FXML private TableColumn<Medication, String> dosageColumn;
    @FXML private TableColumn<Medication, String> frequencyColumn;
    @FXML private TableColumn<Medication, String> startDateColumn;
    @FXML private TableColumn<Medication, String> endDateColumn;
    @FXML private TableColumn<Medication, String> statusColumn;
    
    @FXML private TableView<LabResult> labResultsTable;
    @FXML private TableColumn<LabResult, String> testNameColumn;
    @FXML private TableColumn<LabResult, String> testDateColumn;
    @FXML private TableColumn<LabResult, String> resultColumn;
    @FXML private TableColumn<LabResult, String> normalRangeColumn;
    @FXML private TableColumn<LabResult, String> labStatusColumn;
    
    @FXML private TableView<Note> notesTable;
    @FXML private TableColumn<Note, String> noteDateColumn;
    @FXML private TableColumn<Note, String> titleColumn;
    @FXML private TableColumn<Note, String> authorColumn;
    @FXML private TableColumn<Note, String> noteTypeColumn;
    
    @FXML private Button addMedicalRecordButton;
    @FXML private Button addMedicationButton;
    @FXML private Button addLabResultButton;
    @FXML private Button addNoteButton;
    @FXML private Button editPatientButton;
    @FXML private Button closeButton;
    
    private Patient patient;
    private final PatientDAO patientDAO = PatientDAO.getInstance();
    private final AuthService authService = AuthService.getInstance();
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        // Initialize medical history table columns
        visitDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime visitDate = cellData.getValue().getVisitDate();
            return new SimpleStringProperty(visitDate.format(dateTimeFormatter));
        });
        diagnosisColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDiagnosis()));
        doctorColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty("Dr. " + cellData.getValue().getDoctorId()));
        recordTypeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getVisitType()));
        
        // Initialize medications table columns
        medicationNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
        dosageColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDosage()));
        frequencyColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFrequency()));
        startDateColumn.setCellValueFactory(cellData -> {
            LocalDate startDate = cellData.getValue().getStartDate();
            return new SimpleStringProperty(startDate != null ? startDate.format(dateFormatter) : "");
        });
        endDateColumn.setCellValueFactory(cellData -> {
            LocalDate endDate = cellData.getValue().getEndDate();
            return new SimpleStringProperty(endDate != null ? endDate.format(dateFormatter) : "Ongoing");
        });
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isCurrent() ? "Current" : "Discontinued"));
        
        // Initialize lab results table columns
        testNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTestName()));
        testDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime testDate = cellData.getValue().getTestDate();
            return new SimpleStringProperty(testDate.format(dateTimeFormatter));
        });
        resultColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getResult()));
        normalRangeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNormalRange()));
        labStatusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Initialize notes table columns
        noteDateColumn.setCellValueFactory(cellData -> {
            // Replace getCreatedAt() with a placeholder or alternative
            // return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(dateTimeFormatter));
            return new SimpleStringProperty(LocalDateTime.now().format(dateTimeFormatter)); // Current date as placeholder
        });
        titleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTitle()));
        authorColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getAuthorName()));
        noteTypeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNoteType()));
        
        // Set up double-click handlers for tables
        setupTableRowHandlers();
    }
    
    /**
     * Set up double-click handlers for table rows.
     */
    private void setupTableRowHandlers() {
        // Medical history table double-click
        medicalHistoryTable.setRowFactory(tv -> {
            TableRow<MedicalRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    MedicalRecord record = row.getItem();
                    showMedicalRecordDetails(record);
                }
            });
            return row;
        });
        
        // Medications table double-click
        medicationsTable.setRowFactory(tv -> {
            TableRow<Medication> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Medication medication = row.getItem();
                    showMedicationDetails(medication);
                }
            });
            return row;
        });
        
        // Lab results table double-click
        labResultsTable.setRowFactory(tv -> {
            TableRow<LabResult> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    LabResult labResult = row.getItem();
                    showLabResultDetails(labResult);
                }
            });
            return row;
        });
        
        // Notes table double-click
        notesTable.setRowFactory(tv -> {
            TableRow<Note> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Note note = row.getItem();
                    showNoteDetails(note);
                }
            });
            return row;
        });
    }
    
    /**
     * Set the patient to display.
     * @param patient The patient to display
     */
    public void setPatient(Patient patient) {
        this.patient = patient;
        displayPatientInfo();
        loadMedicalData();
    }
    
    /**
     * Display patient information.
     */
    private void displayPatientInfo() {
        if (patient != null) {
            patientNameLabel.setText(patient.getFullName());
            patientIdLabel.setText("ID: " + patient.getPatientId());
            ageLabel.setText(patient.getAge() + " years");
            genderLabel.setText(patient.getGender());
            phoneLabel.setText(patient.getPhoneNumber());
            emailLabel.setText(patient.getEmail() != null ? patient.getEmail() : "N/A");
            addressLabel.setText(patient.getAddress() != null ? patient.getAddress() : "N/A");
            cityLabel.setText(patient.getAddress() != null ? extractCity(patient.getAddress()) : "N/A");
            bloodTypeLabel.setText(patient.getBloodType() != null ? patient.getBloodType() : "Unknown");
            insuranceProviderLabel.setText(patient.getInsuranceProvider() != null ? patient.getInsuranceProvider() : "N/A");
            insuranceNumberLabel.setText(patient.getInsuranceNumber() != null ? patient.getInsuranceNumber() : "N/A");
            emergencyContactLabel.setText("Emergency Contact Not Available");
            emergencyPhoneLabel.setText("Emergency Phone Not Available");
            
            if (patient.getRegistrationDate() != null) {
                registrationDateLabel.setText(patient.getRegistrationDate().format(dateFormatter));
            } else {
                registrationDateLabel.setText("N/A");
            }
            
            lastVisitDateLabel.setText("Visit History Not Available");
            
            statusLabel.setText("Active");
        }
    }
    
    /**
     * Helper method to extract city from address
     */
    private String extractCity(String address) {
        // Simple extraction - assumes city is after last comma
        if (address != null && address.contains(",")) {
            String[] parts = address.split(",");
            return parts[parts.length - 1].trim();
        }
        return "Unknown";
    }
    
    /**
     * Load medical data for the patient.
     */
    private void loadMedicalData() {
        // In a real implementation, this would load data from the respective DAOs
        // Since the methods don't exist, use empty collections
        
        // Fix getMedicalHistory
        ObservableList<MedicalRecord> medicalHistory = FXCollections.observableArrayList();
        // You could load from a DAO here if available: medicalRecordDAO.getByPatientId(patient.getPatientId());
        medicalHistoryTable.setItems(medicalHistory);
        
        // Fix getMedications
        ObservableList<Medication> medications = FXCollections.observableArrayList();
        medicationsTable.setItems(medications);
        
        // Fix getLabResults
        ObservableList<LabResult> labResults = FXCollections.observableArrayList();
        labResultsTable.setItems(labResults);
        
        // Fix getNotes
        ObservableList<Note> notes = FXCollections.observableArrayList();
        notesTable.setItems(notes);
    }
    
    /**
     * Show medical record details.
     * @param record The medical record to display
     */
    private void showMedicalRecordDetails(MedicalRecord record) {
        showInfoDialog("Medical Record Details", 
                      "Date: " + record.getVisitDate().format(dateTimeFormatter) + "\n" +
                      "Doctor ID: " + record.getDoctorId() + "\n" +
                      "Diagnosis: " + record.getDiagnosis() + "\n" +
                      "Symptoms: " + record.getSymptoms() + "\n" +
                      "Treatment: " + record.getTreatment() + "\n" +
                      "Notes: " + record.getNotes());
    }
    
    /**
     * Show medication details.
     * @param medication The medication to display
     */
    private void showMedicationDetails(Medication medication) {
        // In a real implementation, this would open a detailed view of the medication
        showInfoDialog("Medication Details", 
                      "Name: " + medication.getName() + "\n" +
                      "Dosage: " + medication.getDosage() + "\n" +
                      "Frequency: " + medication.getFrequency() + "\n" +
                      "Instructions: " + medication.getInstructions() + "\n" +
                      "Purpose: " + medication.getPurpose() + "\n" +
                      "Start Date: " + (medication.getStartDate() != null ? medication.getStartDate().format(dateFormatter) : "N/A") + "\n" +
                      "End Date: " + (medication.getEndDate() != null ? medication.getEndDate().format(dateFormatter) : "Ongoing") + "\n" +
                      "Status: " + (medication.isCurrent() ? "Current" : "Discontinued") + "\n" +
                      "Side Effects: " + medication.getSideEffects() + "\n" +
                      "Notes: " + medication.getNotes());
    }
    
    /**
     * Show lab result details.
     * @param labResult The lab result to display
     */
    private void showLabResultDetails(LabResult labResult) {
        // In a real implementation, this would open a detailed view of the lab result
        showInfoDialog("Lab Result Details", 
                      "Test Name: " + labResult.getTestName() + "\n" +
                      "Test Type: " + labResult.getTestType() + "\n" +
                      "Test Date: " + labResult.getTestDate().format(dateTimeFormatter) + "\n" +
                      "Result: " + labResult.getResult() + "\n" +
                      "Normal Range: " + labResult.getNormalRange() + "\n" +
                      "Unit: " + labResult.getUnit() + "\n" +
                      "Abnormal: " + (labResult.isAbnormal() ? "Yes" : "No") + "\n" +
                      "Status: " + labResult.getStatus() + "\n" +
                      "Urgent: " + (labResult.isUrgent() ? "Yes" : "No") + "\n" +
                      "Notes: " + labResult.getNotes());
    }
    
    /**
     * Show note details.
     * @param note The note to display
     */
    private void showNoteDetails(Note note) {
        // In a real implementation, this would open a detailed view of the note
        showInfoDialog("Note Details", 
                      "Title: " + note.getTitle() + "\n" +
                      "Author: " + note.getAuthorName() + "\n" +
                      "Date: " + LocalDateTime.now().format(dateTimeFormatter) + "\n" +
                      "Type: " + note.getNoteType() + "\n" +
                      "Content: " + note.getContent());
    }
    
    /**
     * Handle add medical record button click.
     * @param event The action event
     */
    @FXML
    private void onAddMedicalRecord(ActionEvent event) {
        // In a real implementation, this would open a form to add a new medical record
        showInfoDialog("Add Medical Record", "This feature is not implemented yet.");
    }
    
    /**
     * Handle add medication button click.
     * @param event The action event
     */
    @FXML
    private void onAddMedication(ActionEvent event) {
        // In a real implementation, this would open a form to add a new medication
        showInfoDialog("Add Medication", "This feature is not implemented yet.");
    }
    
    /**
     * Handle add lab result button click.
     * @param event The action event
     */
    @FXML
    private void onAddLabResult(ActionEvent event) {
        // In a real implementation, this would open a form to add a new lab result
        showInfoDialog("Add Lab Result", "This feature is not implemented yet.");
    }
    
    /**
     * Handle add note button click.
     * @param event The action event
     */
    @FXML
    private void onAddNote(ActionEvent event) {
        // In a real implementation, this would open a form to add a new note
        showInfoDialog("Add Note", "This feature is not implemented yet.");
    }
    
    /**
     * Handle edit patient button click.
     */
    @FXML
    private void onEditPatient(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/view/PatientFormView.fxml"));
            Parent root = loader.load();
            
            PatientFormController controller = loader.getController();
            controller.setMode(PatientFormController.FormMode.EDIT);
            controller.setPatient(patient);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Patient: " + patient.getFullName());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Fix patientDAO.findById method
            // Handle the case where getPatientById returns Optional<Patient>
            Optional<Patient> refreshedPatient = patientDAO.getPatientById(patient.getPatientId());
            refreshedPatient.ifPresent(this::setPatient);
            
        } catch (IOException e) {
            showErrorDialog("Error", "Could not open patient form: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle close button click.
     * @param event The action event
     */
    @FXML
    private void onClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Show an information dialog.
     * @param title The dialog title
     * @param content The dialog content
     */
    private void showInfoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        
        // Create a scrollable text area for the content
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(400);
        textArea.setPrefHeight(300);
        
        VBox vbox = new VBox(textArea);
        alert.getDialogPane().setContent(vbox);
        
        alert.showAndWait();
    }
    
    /**
     * Show an error dialog.
     * @param title The dialog title
     * @param content The dialog content
     */
    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Handle a request to view full medical records.
     * @param event The action event
     */
    @FXML
    private void onViewFullMedicalRecords(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/view/PatientHistoryView.fxml"));
            Parent root = loader.load();
            
            PatientHistoryController controller = loader.getController();
            // If there's a method to set the selected patient, call it
            if (patient != null) {
                controller.setPatient(patient);
            }
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Medical Records for " + patient.getFullName());
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (IOException e) {
            showErrorDialog("Error", "Could not open medical records: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
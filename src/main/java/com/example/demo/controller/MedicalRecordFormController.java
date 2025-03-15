package com.example.demo.controller;

import com.example.demo.auth.AuthService;
import com.example.demo.database.MedicalRecordDAO;
import com.example.demo.database.PatientDAO;
import com.example.demo.database.UserDAO;
import com.example.demo.model.MedicalRecord;
import com.example.demo.model.Patient;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Controller for the Medical Record form to add or edit medical records
 */
public class MedicalRecordFormController {

    @FXML private Label formTitleLabel;
    @FXML private ComboBox<Patient> patientComboBox;
    @FXML private TextField patientIdField;
    @FXML private DatePicker visitDatePicker;
    @FXML private ComboBox<String> visitTypeComboBox;
    @FXML private ComboBox<User> doctorComboBox;
    @FXML private ComboBox<String> recordTypeComboBox;
    @FXML private TextArea symptomsTextArea;
    @FXML private TextArea diagnosisTextArea;
    @FXML private TextField diagnosisCodesField;
    @FXML private TextArea treatmentTextArea;
    @FXML private TextArea notesTextArea;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private DatePicker followUpDatePicker;
    @FXML private TextField attachmentsField;
    @FXML private Button browseButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private MedicalRecord medicalRecord;
    private FormMode mode = FormMode.ADD;
    private final MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO();
    private final PatientDAO patientDAO = PatientDAO.getInstance();
    private final UserDAO userDAO = new UserDAO();
    private final AuthService authService = AuthService.getInstance();
    
    private Consumer<MedicalRecord> onSaveCallback;
    private Patient preSelectedPatient;

    /**
     * Mode for the form (ADD or EDIT)
     */
    public enum FormMode {
        ADD("Add Medical Record"),
        EDIT("Edit Medical Record");
        
        private final String title;
        
        FormMode(String title) {
            this.title = title;
        }
        
        public String getTitle() {
            return title;
        }
    }
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize form components
        initializeComboBoxes();
        setupPatientSelection();
        
        // Set default values
        visitDatePicker.setValue(LocalDate.now());
        statusComboBox.setValue("Pending");
        
        // Pre-select the current doctor if user is a doctor
        preselectCurrentDoctor();
    }
    
    /**
     * Initialize combo boxes with values
     */
    private void initializeComboBoxes() {
        // Visit Types
        visitTypeComboBox.setItems(FXCollections.observableArrayList(
            "Regular Check-up", "Follow-up", "Emergency", "Specialist Consultation", "Lab Test", "Vaccination"
        ));
        
        // Record Types
        recordTypeComboBox.setItems(FXCollections.observableArrayList(
            "Consultation", "Lab Result", "Prescription", "Vaccination", "Procedure", "Progress Note"
        ));
        
        // Status options
        statusComboBox.setItems(FXCollections.observableArrayList(
            "Pending", "Completed", "Cancelled", "Scheduled", "In Progress"
        ));
        
        // Load patients for patient combo box
        List<Patient> patients = patientDAO.getAllPatients();
        patientComboBox.setItems(FXCollections.observableArrayList(patients));
        patientComboBox.setConverter(new StringConverter<Patient>() {
            @Override
            public String toString(Patient patient) {
                return patient != null ? patient.getFullName() + " (ID: " + patient.getPatientId() + ")" : "";
            }
            
            @Override
            public Patient fromString(String string) {
                return null; // Not needed for combo box
            }
        });
        
        // Load doctors for doctor combo box
        List<User> doctors = userDAO.getUsersByRole(UserRole.DOCTOR);
        doctorComboBox.setItems(FXCollections.observableArrayList(doctors));
        doctorComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User doctor) {
                return doctor != null ? "Dr. " + doctor.getFullName() + " (ID: " + doctor.getUserId() + ")" : "";
            }
            
            @Override
            public User fromString(String string) {
                return null; // Not needed for combo box
            }
        });
    }
    
    /**
     * Setup patient selection handling
     */
    private void setupPatientSelection() {
        // Update patient ID field when patient is selected
        patientComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                patientIdField.setText(String.valueOf(newValue.getPatientId()));
            } else {
                patientIdField.clear();
            }
        });
    }
    
    /**
     * Pre-select the current doctor if the user is a doctor
     */
    private void preselectCurrentDoctor() {
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isPresent() && currentUser.get().getRole() == UserRole.DOCTOR) {
            User doctor = currentUser.get();
            
            // Find and select the doctor in the combo box
            for (User doc : doctorComboBox.getItems()) {
                if (doc.getUserId() == doctor.getUserId()) {
                    doctorComboBox.setValue(doc);
                    break;
                }
            }
        }
    }
    
    /**
     * Set a pre-selected patient (used when opening from patient details)
     * @param patient The patient to pre-select
     */
    public void setPatient(Patient patient) {
        this.preSelectedPatient = patient;
        
        if (patient != null) {
            // Set the patient in the combo box
            patientComboBox.setValue(patient);
            patientIdField.setText(String.valueOf(patient.getPatientId()));
            
            // Disable patient selection since we're coming from patient context
            patientComboBox.setDisable(true);
        }
    }
    
    /**
     * Set the form mode (ADD or EDIT)
     * @param mode The form mode
     */
    public void setMode(FormMode mode) {
        this.mode = mode;
        formTitleLabel.setText(mode.getTitle());
    }
    
    /**
     * Set the medical record to edit (for edit mode)
     * @param record The medical record to edit
     */
    public void setMedicalRecord(MedicalRecord record) {
        this.medicalRecord = record;
        
        if (record != null) {
            // Fill form fields with record data
            
            // Patient
            int patientId = record.getPatientId();
            for (Patient p : patientComboBox.getItems()) {
                if (p.getPatientId() == patientId) {
                    patientComboBox.setValue(p);
                    break;
                }
            }
            
            // Visit date
            if (record.getVisitDate() != null) {
                visitDatePicker.setValue(record.getVisitDate().toLocalDate());
            }
            
            // Visit type
            visitTypeComboBox.setValue(record.getVisitType());
            
            // Doctor
            int doctorId = record.getDoctorId();
            for (User doc : doctorComboBox.getItems()) {
                if (doc.getUserId() == doctorId) {
                    doctorComboBox.setValue(doc);
                    break;
                }
            }
            
            // Record type
            recordTypeComboBox.setValue(record.getRecordType());
            
            // Other fields
            symptomsTextArea.setText(record.getSymptoms());
            diagnosisTextArea.setText(record.getDiagnosis());
            diagnosisCodesField.setText(record.getDiagnosisCodes());
            treatmentTextArea.setText(record.getTreatment());
            notesTextArea.setText(record.getNotes());
            statusComboBox.setValue(record.getStatus());
            
            // Follow-up date
            if (record.getFollowUpDate() != null) {
                followUpDatePicker.setValue(record.getFollowUpDate().toLocalDate());
            }
            
            // Attachments
            attachmentsField.setText(record.getAttachments());
        }
    }
    
    /**
     * Set callback for when a record is saved
     * @param callback The callback to invoke
     */
    public void setOnSaveCallback(Consumer<MedicalRecord> callback) {
        this.onSaveCallback = callback;
    }
    
    /**
     * Handle Save button click
     * @param event The action event
     */
    @FXML
    private void onSave(ActionEvent event) {
        if (!validateForm()) {
            return;
        }
        
        // Create or update medical record with form data
        if (mode == FormMode.ADD) {
            medicalRecord = new MedicalRecord();
        }
        
        // Get patient from combo box
        Patient selectedPatient = patientComboBox.getValue();
        if (selectedPatient != null) {
            medicalRecord.setPatientId(selectedPatient.getPatientId());
            medicalRecord.setPatientName(selectedPatient.getFullName());
        }
        
        // Get doctor from combo box
        User selectedDoctor = doctorComboBox.getValue();
        if (selectedDoctor != null) {
            medicalRecord.setDoctorId(selectedDoctor.getUserId());
            medicalRecord.setDoctorName(selectedDoctor.getFullName());
        }
        
        // Set visit date and time (combine date from picker with current time)
        LocalDate visitDate = visitDatePicker.getValue();
        if (visitDate != null) {
            medicalRecord.setVisitDate(LocalDateTime.of(visitDate, LocalTime.now()));
        }
        
        // Set other fields
        medicalRecord.setVisitType(visitTypeComboBox.getValue());
        medicalRecord.setRecordType(recordTypeComboBox.getValue());
        medicalRecord.setSymptoms(symptomsTextArea.getText());
        medicalRecord.setDiagnosis(diagnosisTextArea.getText());
        medicalRecord.setDiagnosisCodes(diagnosisCodesField.getText());
        medicalRecord.setTreatment(treatmentTextArea.getText());
        medicalRecord.setNotes(notesTextArea.getText());
        medicalRecord.setStatus(statusComboBox.getValue());
        
        // Set follow-up date if provided
        if (followUpDatePicker.getValue() != null) {
            medicalRecord.setFollowUpDate(LocalDateTime.of(followUpDatePicker.getValue(), LocalTime.NOON));
        }
        
        // Set attachments
        medicalRecord.setAttachments(attachmentsField.getText());
        
        // Save to database
        boolean success;
        if (mode == FormMode.ADD) {
            success = medicalRecordDAO.createMedicalRecord(medicalRecord);
        } else {
            success = medicalRecordDAO.updateMedicalRecord(medicalRecord);
        }
        
        if (success) {
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Medical record " + (mode == FormMode.ADD ? "added" : "updated") + " successfully.");
            alert.showAndWait();
            
            // Call callback if provided
            if (onSaveCallback != null) {
                onSaveCallback.accept(medicalRecord);
            }
            
            // Close the form
            closeForm();
        } else {
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to " + (mode == FormMode.ADD ? "add" : "update") + " medical record.");
            alert.showAndWait();
        }
    }
    
    /**
     * Handle Cancel button click
     * @param event The action event
     */
    @FXML
    private void onCancel(ActionEvent event) {
        // Confirm if there are unsaved changes
        if (hasUnsavedChanges()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Cancel");
            alert.setHeaderText(null);
            alert.setContentText("There are unsaved changes. Are you sure you want to cancel?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return;
            }
        }
        
        closeForm();
    }
    
    /**
     * Handle Browse button click for attachments
     * @param event The action event
     */
    @FXML
    private void onBrowse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachments");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("Document Files", "*.pdf", "*.doc", "*.docx", "*.txt")
        );
        
        // Show open file dialog
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(browseButton.getScene().getWindow());
        
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            // Format file names as comma-separated list
            String fileNames = selectedFiles.stream()
                .map(File::getName)
                .collect(Collectors.joining(", "));
            
            attachmentsField.setText(fileNames);
        }
    }
    
    /**
     * Validate the form fields
     * @return true if valid, false otherwise
     */
    private boolean validateForm() {
        StringBuilder errorMessages = new StringBuilder();
        
        // Check required fields
        if (patientComboBox.getValue() == null) {
            errorMessages.append("- Please select a patient\n");
        }
        
        if (visitDatePicker.getValue() == null) {
            errorMessages.append("- Please select a visit date\n");
        }
        
        if (visitTypeComboBox.getValue() == null || visitTypeComboBox.getValue().isEmpty()) {
            errorMessages.append("- Please select a visit type\n");
        }
        
        if (doctorComboBox.getValue() == null) {
            errorMessages.append("- Please select a doctor\n");
        }
        
        if (recordTypeComboBox.getValue() == null || recordTypeComboBox.getValue().isEmpty()) {
            errorMessages.append("- Please select a record type\n");
        }
        
        if (diagnosisTextArea.getText() == null || diagnosisTextArea.getText().trim().isEmpty()) {
            errorMessages.append("- Please enter a diagnosis\n");
        }
        
        if (statusComboBox.getValue() == null || statusComboBox.getValue().isEmpty()) {
            errorMessages.append("- Please select a status\n");
        }
        
        // Display error message if any validation failed
        if (errorMessages.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errorMessages.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if the form has unsaved changes
     * @return true if there are unsaved changes, false otherwise
     */
    private boolean hasUnsavedChanges() {
        // Always return true for now
        // In a more sophisticated implementation, this would compare current form values with original values
        return true;
    }
    
    /**
     * Close the form
     */
    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
} 
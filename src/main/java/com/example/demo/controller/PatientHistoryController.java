package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.database.*;
import com.example.demo.auth.AuthService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Line;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;

/**
 * Controller for the Patient History view
 * Allows doctors to manage medical records, medications, lab results and notes for patients
 */
public class PatientHistoryController implements Initializable {

    // Services and Data Access Objects
    private final PatientDAO patientDAO = PatientDAO.getInstance();
    private final MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO();
    private final MedicationDAO medicationDAO = new MedicationDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final NoteDAO noteDAO = new NoteDAO();
    private final AuthService authService = AuthService.getInstance();
    
    // Currently selected patient and tab
    private Patient selectedPatient;
    private int currentTab = 0; // 0 = Medical Records, 1 = Medications, 2 = Lab Results, 3 = Notes
    
    // Flag to indicate if we're in reports mode
    private boolean reportsMode = false;
    
    // FXML components for UI
    @FXML private ComboBox<Patient> patientSelector;
    @FXML private TabPane historyTabPane;
    
    // Medical Records Tab
    @FXML private TableView<MedicalRecord> medicalRecordsTable;
    @FXML private TableColumn<MedicalRecord, String> mrDateColumn;
    @FXML private TableColumn<MedicalRecord, String> mrDiagnosisColumn;
    @FXML private TableColumn<MedicalRecord, String> mrTreatmentColumn;
    @FXML private TableColumn<MedicalRecord, String> mrFollowUpColumn;
    @FXML private TableColumn<MedicalRecord, String> mrActionColumn;
    
    // Medical Record Form
    @FXML private VBox medicalRecordForm;
    @FXML private DatePicker mrVisitDatePicker;
    @FXML private TextField mrDiagnosisField;
    @FXML private TextArea mrSymptomsArea;
    @FXML private TextArea mrTreatmentArea;
    @FXML private TextArea mrNotesArea;
    @FXML private DatePicker mrFollowUpDatePicker;
    @FXML private TextField mrRecordTypeField;
    @FXML private Button mrSaveButton;
    @FXML private Button mrCancelButton;
    
    // Medications Tab
    @FXML private TableView<Medication> medicationsTable;
    @FXML private TableColumn<Medication, String> medNameColumn;
    @FXML private TableColumn<Medication, String> medDosageColumn;
    @FXML private TableColumn<Medication, String> medFrequencyColumn;
    @FXML private TableColumn<Medication, String> medStartDateColumn;
    @FXML private TableColumn<Medication, String> medActionColumn;
    
    // Medication Form
    @FXML private VBox medicationForm;
    @FXML private TextField medNameField;
    @FXML private TextField medDosageField;
    @FXML private TextField medFrequencyField;
    @FXML private TextArea medInstructionsArea;
    @FXML private TextField medPurposeField;
    @FXML private DatePicker medStartDatePicker;
    @FXML private DatePicker medEndDatePicker;
    @FXML private CheckBox medIsCurrentCheckbox;
    @FXML private TextArea medSideEffectsArea;
    @FXML private TextArea medNotesArea;
    @FXML private Button medSaveButton;
    @FXML private Button medCancelButton;
    
    // Lab Results Tab
    @FXML private TableView<LabResult> labResultsTable;
    @FXML private TableColumn<LabResult, String> labTestNameColumn;
    @FXML private TableColumn<LabResult, String> labResultColumn;
    @FXML private TableColumn<LabResult, String> labTestDateColumn;
    @FXML private TableColumn<LabResult, String> labStatusColumn;
    @FXML private TableColumn<LabResult, String> labActionColumn;
    
    // Lab Result Form
    @FXML private VBox labResultForm;
    @FXML private TextField labTestNameField;
    @FXML private TextField labTestTypeField;
    @FXML private TextArea labResultArea;
    @FXML private TextField labNormalRangeField;
    @FXML private TextField labUnitField;
    @FXML private CheckBox labIsAbnormalCheckbox;
    @FXML private DatePicker labTestDatePicker;
    @FXML private DatePicker labResultDatePicker;
    @FXML private TextArea labNotesArea;
    @FXML private ComboBox<String> labStatusComboBox;
    @FXML private CheckBox labIsUrgentCheckbox;
    @FXML private Button labSaveButton;
    @FXML private Button labCancelButton;
    
    // Notes Tab
    @FXML private TableView<Note> notesTable;
    @FXML private TableColumn<Note, String> noteTitleColumn;
    @FXML private TableColumn<Note, String> noteTypeColumn;
    @FXML private TableColumn<Note, String> noteDateColumn;
    @FXML private TableColumn<Note, String> noteAuthorColumn;
    @FXML private TableColumn<Note, String> noteActionColumn;
    
    // Note Form
    @FXML private VBox noteForm;
    @FXML private TextField noteTitleField;
    @FXML private TextArea noteContentArea;
    @FXML private ComboBox<String> noteTypeComboBox;
    @FXML private CheckBox noteIsPrivateCheckbox;
    @FXML private Button noteSaveButton;
    @FXML private Button noteCancelButton;
    
    // State variables for editing
    private MedicalRecord editingMedicalRecord;
    private Medication editingMedication;
    private LabResult editingLabResult;
    private Note editingNote;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check if user is a doctor - if not, this view shouldn't be accessible
        if (!authService.hasRole(UserRole.DOCTOR)) {
            System.err.println("Unauthorized access attempt to Patient History by non-doctor role");
            // You should handle this by redirecting to an appropriate view
            return;
        }
        
        // Setup the patient selector
        setupPatientSelector();
        
        // Setup the tab change listener
        historyTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            currentTab = newVal.intValue();
            refreshCurrentTab();
        });
        
        // Initialize tabs
        setupMedicalRecordsTab();
        setupMedicationsTab();
        setupLabResultsTab();
        setupNotesTab();
        
        // Hide forms initially
        hideAllForms();
        
        // Add reports functionality if in reports mode
        if (reportsMode) {
            addReportingControls();
        }
    }
    
    /**
     * Set the reports mode for this view
     * @param isReportsMode true if this is a reports view, false for normal medical records view
     */
    public void setReportsMode(boolean isReportsMode) {
        this.reportsMode = isReportsMode;
        
        // If this is called after initialization and we're in the FX thread, update UI
        if (reportsMode && javafx.application.Platform.isFxApplicationThread()) {
            // Change window title in stage if possible
            try {
                if (patientSelector != null && patientSelector.getScene() != null && 
                    patientSelector.getScene().getWindow() instanceof Stage) {
                    Stage stage = (Stage) patientSelector.getScene().getWindow();
                    stage.setTitle("Patient Records Reports");
                }
            } catch (Exception e) {
                System.err.println("Error updating title for reports mode: " + e.getMessage());
            }
        }
    }
    
    /**
     * Setup the patient selector with all patients
     */
    private void setupPatientSelector() {
        // Get all patients
        List<Patient> patients = patientDAO.getAllPatients();
        
        // Configure the patient selector
        patientSelector.setItems(FXCollections.observableArrayList(patients));
        
        // Set the cell factory to display patient name
        patientSelector.setCellFactory(p -> new ListCell<Patient>() {
            @Override
            protected void updateItem(Patient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullName() + " (ID: " + item.getPatientId() + ")");
                }
            }
        });
        
        // Set the converter for the selected item display
        patientSelector.setConverter(new StringConverter<Patient>() {
            @Override
            public String toString(Patient patient) {
                if (patient == null) {
                    return "Select Patient";
                }
                return patient.getFullName() + " (ID: " + patient.getPatientId() + ")";
            }
            
            @Override
            public Patient fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
        
        // Add change listener
        patientSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedPatient = newVal;
                refreshAllTabs();
            }
        });
        
        // Select first patient if available
        if (!patients.isEmpty()) {
            patientSelector.getSelectionModel().selectFirst();
        }
    }
    
    /**
     * Setup the medical records tab
     */
    private void setupMedicalRecordsTab() {
        // Configure table columns
        mrDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getVisitDate().toString()));
        
        mrDiagnosisColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDiagnosis()));
        
        mrTreatmentColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTreatment()));
        
        mrFollowUpColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty("N/A"));
        
        // Setup action column with edit and delete buttons
        mrActionColumn.setCellFactory(createActionCellFactory(this::editMedicalRecord, this::deleteMedicalRecord));
    }
    
    /**
     * Setup the medications tab
     */
    private void setupMedicationsTab() {
        // Configure table columns
        medNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
        
        medDosageColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDosage()));
        
        medFrequencyColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFrequency()));
        
        medStartDateColumn.setCellValueFactory(cellData -> {
            LocalDate startDate = cellData.getValue().getStartDate();
            return new SimpleStringProperty(startDate != null ? startDate.toString() : "N/A");
        });
        
        // Setup action column with edit and delete buttons
        medActionColumn.setCellFactory(createActionCellFactory(this::editMedication, this::deleteMedication));
    }
    
    /**
     * Setup the lab results tab
     */
    private void setupLabResultsTab() {
        // Configure table columns
        labTestNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTestName()));
        
        labResultColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getResult()));
        
        labTestDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTestDate().toString()));
        
        labStatusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Setup action column with edit and delete buttons
        labActionColumn.setCellFactory(createActionCellFactory(this::editLabResult, this::deleteLabResult));
        
        // Setup lab status options
        labStatusComboBox.setItems(FXCollections.observableArrayList(
            "Pending", "In Progress", "Completed", "Cancelled"
        ));
    }
    
    /**
     * Setup the notes tab
     */
    private void setupNotesTab() {
        // Configure table columns
        noteTitleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTitle()));
        
        noteTypeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNoteType()));
        
        noteDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty("N/A"));
        
        noteAuthorColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getAuthorName()));
        
        // Setup action column with edit and delete buttons
        noteActionColumn.setCellFactory(createActionCellFactory(this::editNote, this::deleteNote));
        
        // Setup note type options
        noteTypeComboBox.setItems(FXCollections.observableArrayList(
            "General", "Progress", "Treatment", "Lab", "Medication", "Emergency"
        ));
    }
    
    /**
     * Refresh all tabs with data for the selected patient
     */
    private void refreshAllTabs() {
        refreshMedicalRecordsTab();
        refreshMedicationsTab();
        refreshLabResultsTab();
        refreshNotesTab();
    }
    
    /**
     * Refresh only the current active tab
     */
    private void refreshCurrentTab() {
        switch (currentTab) {
            case 0:
                refreshMedicalRecordsTab();
                break;
            case 1:
                refreshMedicationsTab();
                break;
            case 2:
                refreshLabResultsTab();
                break;
            case 3:
                refreshNotesTab();
                break;
        }
    }
    
    /**
     * Refresh the medical records tab
     */
    private void refreshMedicalRecordsTab() {
        if (selectedPatient != null) {
            List<MedicalRecord> records = medicalRecordDAO.getMedicalRecordsByPatientId(selectedPatient.getPatientId());
            medicalRecordsTable.setItems(FXCollections.observableArrayList(records));
        } else {
            medicalRecordsTable.getItems().clear();
        }
    }
    
    /**
     * Refresh the medications tab
     */
    private void refreshMedicationsTab() {
        if (selectedPatient != null) {
            List<Medication> medications = medicationDAO.getMedicationsByPatientId(selectedPatient.getPatientId());
            medicationsTable.setItems(FXCollections.observableArrayList(medications));
        } else {
            medicationsTable.getItems().clear();
        }
    }
    
    /**
     * Refresh the lab results tab
     */
    private void refreshLabResultsTab() {
        if (selectedPatient != null) {
            List<LabResult> results = labResultDAO.getLabResultsByPatientId(selectedPatient.getPatientId());
            labResultsTable.setItems(FXCollections.observableArrayList(results));
        } else {
            labResultsTable.getItems().clear();
        }
    }
    
    /**
     * Refresh the notes tab
     */
    private void refreshNotesTab() {
        if (selectedPatient != null) {
            List<Note> notes = noteDAO.getNotesByPatientId(selectedPatient.getPatientId());
            notesTable.setItems(FXCollections.observableArrayList(notes));
        } else {
            notesTable.getItems().clear();
        }
    }
    
    /**
     * Hide all forms
     */
    private void hideAllForms() {
        medicalRecordForm.setVisible(false);
        medicationForm.setVisible(false);
        labResultForm.setVisible(false);
        noteForm.setVisible(false);
    }
    
    /**
     * Create a new medical record
     */
    @FXML
    private void createNewMedicalRecord() {
        if (selectedPatient == null) {
            showAlert("Error", "No patient selected", "Please select a patient first.");
            return;
        }
        
        editingMedicalRecord = null;
        clearMedicalRecordForm();
        medicalRecordForm.setVisible(true);
        mrVisitDatePicker.setValue(LocalDate.now());
    }
    
    /**
     * Edit a medical record
     */
    private void editMedicalRecord(MedicalRecord record) {
        editingMedicalRecord = record;
        
        mrVisitDatePicker.setValue(record.getVisitDate() != null ? 
                                 record.getVisitDate().toLocalDate() : LocalDate.now());
        mrDiagnosisField.setText(record.getDiagnosis());
        mrSymptomsArea.setText(record.getSymptoms());
        mrTreatmentArea.setText(record.getTreatment());
        mrNotesArea.setText(record.getNotes());
        
        mrRecordTypeField.setText(record.getVisitType());
        
        medicalRecordForm.setVisible(true);
    }
    
    /**
     * Save a medical record (create or update)
     */
    @FXML
    private void saveMedicalRecord() {
        if (selectedPatient == null) {
            showAlert("Error", "No patient selected", "Please select a patient first.");
            return;
        }
        
        try {
            // Get the current doctor from the auth service
            Optional<User> doctorOpt = authService.getCurrentUser();
            if (!doctorOpt.isPresent()) {
                showAlert("Error", "Authentication Error", "Could not determine the current doctor.");
                return;
            }
            
            User doctor = doctorOpt.get();
            
            // Create or update the medical record
            MedicalRecord record = (editingMedicalRecord != null) 
                ? editingMedicalRecord 
                : new MedicalRecord();
            
            record.setPatientId(selectedPatient.getPatientId());
            record.setDoctorId(doctor.getUserId());
            
            // Convert LocalDate to LocalDateTime for the model
            LocalDate visitDate = mrVisitDatePicker.getValue();
            record.setVisitDate(visitDate != null ? 
                              LocalDateTime.of(visitDate, LocalTime.now()) : 
                              LocalDateTime.now());
            
            record.setDiagnosis(mrDiagnosisField.getText());
            record.setSymptoms(mrSymptomsArea.getText());
            record.setTreatment(mrTreatmentArea.getText());
            record.setNotes(mrNotesArea.getText());
            
            record.setVisitType(mrRecordTypeField.getText());
            
            boolean success;
            if (editingMedicalRecord != null) {
                success = medicalRecordDAO.updateMedicalRecord(record);
            } else {
                success = medicalRecordDAO.createMedicalRecord(record);
            }
            
            if (success) {
                medicalRecordForm.setVisible(false);
                refreshMedicalRecordsTab();
            } else {
                showAlert("Error", "Database Error", "Could not save the medical record.");
            }
        } catch (Exception e) {
            showAlert("Error", "Input Error", "Please check your input values: " + e.getMessage());
        }
    }
    
    /**
     * Delete a medical record
     */
    private void deleteMedicalRecord(MedicalRecord record) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Medical Record");
        alert.setContentText("Are you sure you want to delete this medical record? This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = medicalRecordDAO.deleteMedicalRecord(record.getRecordId());
            if (success) {
                refreshMedicalRecordsTab();
            } else {
                showAlert("Error", "Database Error", "Could not delete the medical record.");
            }
        }
    }
    
    /**
     * Cancel medical record edit or creation
     */
    @FXML
    private void cancelMedicalRecord() {
        medicalRecordForm.setVisible(false);
        editingMedicalRecord = null;
    }
    
    /**
     * Clear the medical record form
     */
    private void clearMedicalRecordForm() {
        mrVisitDatePicker.setValue(null);
        mrDiagnosisField.clear();
        mrSymptomsArea.clear();
        mrTreatmentArea.clear();
        mrNotesArea.clear();
        mrRecordTypeField.clear();
    }
    
    /**
     * Create a new medication
     */
    @FXML
    private void createNewMedication() {
        if (selectedPatient == null) {
            showAlert("Error", "No patient selected", "Please select a patient first.");
            return;
        }
        
        editingMedication = null;
        clearMedicationForm();
        medicationForm.setVisible(true);
        medStartDatePicker.setValue(LocalDate.now());
        medIsCurrentCheckbox.setSelected(true);
    }
    
    /**
     * Edit a medication
     */
    private void editMedication(Medication medication) {
        editingMedication = medication;
        
        medNameField.setText(medication.getName());
        medDosageField.setText(medication.getDosage());
        medFrequencyField.setText(medication.getFrequency());
        medInstructionsArea.setText(medication.getInstructions());
        medPurposeField.setText(medication.getPurpose());
        medStartDatePicker.setValue(medication.getStartDate());
        medEndDatePicker.setValue(medication.getEndDate());
        medIsCurrentCheckbox.setSelected(medication.isCurrent());
        medSideEffectsArea.setText(medication.getSideEffects());
        medNotesArea.setText(medication.getNotes());
        
        medicationForm.setVisible(true);
    }
    
    /**
     * Save a medication (create or update)
     */
    @FXML
    private void saveMedication() {
        if (selectedPatient == null) {
            showAlert("Error", "No patient selected", "Please select a patient first.");
            return;
        }
        
        try {
            // Get the current doctor from the auth service
            Optional<User> doctorOpt = authService.getCurrentUser();
            if (!doctorOpt.isPresent()) {
                showAlert("Error", "Authentication Error", "Could not determine the current doctor.");
                return;
            }
            
            User doctor = doctorOpt.get();
            
            // Create or update the medication
            Medication medication = (editingMedication != null) 
                ? editingMedication 
                : new Medication();
            
            medication.setPatientId(selectedPatient.getPatientId());
            medication.setDoctorId(doctor.getUserId());
            medication.setName(medNameField.getText());
            medication.setDosage(medDosageField.getText());
            medication.setFrequency(medFrequencyField.getText());
            medication.setInstructions(medInstructionsArea.getText());
            medication.setPurpose(medPurposeField.getText());
            medication.setStartDate(medStartDatePicker.getValue());
            medication.setEndDate(medEndDatePicker.getValue());
            medication.setCurrent(medIsCurrentCheckbox.isSelected());
            medication.setSideEffects(medSideEffectsArea.getText());
            medication.setNotes(medNotesArea.getText());
            
            boolean success;
            if (editingMedication != null) {
                success = medicationDAO.updateMedication(medication);
            } else {
                success = medicationDAO.createMedication(medication);
            }
            
            if (success) {
                medicationForm.setVisible(false);
                refreshMedicationsTab();
            } else {
                showAlert("Error", "Database Error", "Could not save the medication.");
            }
        } catch (Exception e) {
            showAlert("Error", "Input Error", "Please check your input values: " + e.getMessage());
        }
    }
    
    /**
     * Delete a medication
     */
    private void deleteMedication(Medication medication) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Medication");
        alert.setContentText("Are you sure you want to delete this medication? This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = medicationDAO.deleteMedication(medication.getMedicationId());
            if (success) {
                refreshMedicationsTab();
            } else {
                showAlert("Error", "Database Error", "Could not delete the medication.");
            }
        }
    }
    
    /**
     * Cancel medication edit or creation
     */
    @FXML
    private void cancelMedication() {
        medicationForm.setVisible(false);
        editingMedication = null;
    }
    
    /**
     * Clear the medication form
     */
    private void clearMedicationForm() {
        medNameField.clear();
        medDosageField.clear();
        medFrequencyField.clear();
        medInstructionsArea.clear();
        medPurposeField.clear();
        medStartDatePicker.setValue(null);
        medEndDatePicker.setValue(null);
        medIsCurrentCheckbox.setSelected(true);
        medSideEffectsArea.clear();
        medNotesArea.clear();
    }
    
    /**
     * Create a new lab result
     */
    @FXML
    private void createNewLabResult() {
        if (selectedPatient == null) {
            showAlert("Error", "No patient selected", "Please select a patient first.");
            return;
        }
        
        editingLabResult = null;
        clearLabResultForm();
        labResultForm.setVisible(true);
        labTestDatePicker.setValue(LocalDate.now());
        labStatusComboBox.setValue("Pending");
    }
    
    /**
     * Edit a lab result
     */
    private void editLabResult(LabResult labResult) {
        editingLabResult = labResult;
        
        labTestNameField.setText(labResult.getTestName());
        labTestTypeField.setText(labResult.getTestType());
        labResultArea.setText(labResult.getResult());
        labNormalRangeField.setText(labResult.getNormalRange());
        labUnitField.setText(labResult.getUnit());
        labIsAbnormalCheckbox.setSelected(labResult.isAbnormal());
        
        labTestDatePicker.setValue(labResult.getTestDate() != null ? 
                                 labResult.getTestDate().toLocalDate() : LocalDate.now());
        labResultDatePicker.setValue(labResult.getResultDate() != null ? 
                                    labResult.getResultDate().toLocalDate() : LocalDate.now());
                                    
        labNotesArea.setText(labResult.getNotes());
        labStatusComboBox.setValue(labResult.getStatus());
        labIsUrgentCheckbox.setSelected(labResult.isUrgent());
        
        labResultForm.setVisible(true);
    }
    
    /**
     * Save a lab result (create or update)
     */
    @FXML
    private void saveLabResult() {
        if (selectedPatient == null) {
            showAlert("Error", "No patient selected", "Please select a patient first.");
            return;
        }
        
        try {
            // Get the current doctor from the auth service
            Optional<User> doctorOpt = authService.getCurrentUser();
            if (!doctorOpt.isPresent()) {
                showAlert("Error", "Authentication Error", "Could not determine the current doctor.");
                return;
            }
            
            User doctor = doctorOpt.get();
            
            // Create or update the lab result
            LabResult labResult = (editingLabResult != null) 
                ? editingLabResult 
                : new LabResult();
            
            labResult.setPatientId(selectedPatient.getPatientId());
            labResult.setDoctorId(doctor.getUserId());
            labResult.setTestName(labTestNameField.getText());
            labResult.setTestType(labTestTypeField.getText());
            labResult.setResult(labResultArea.getText());
            labResult.setNormalRange(labNormalRangeField.getText());
            labResult.setUnit(labUnitField.getText());
            labResult.setAbnormal(labIsAbnormalCheckbox.isSelected());
            
            // Convert LocalDate to LocalDateTime
            LocalDate testDate = labTestDatePicker.getValue();
            labResult.setTestDate(testDate != null ? 
                                    LocalDateTime.of(testDate, LocalTime.now()) : 
                                    LocalDateTime.now());
                                    
            LocalDate resultDate = labResultDatePicker.getValue();
            labResult.setResultDate(resultDate != null ? 
                                    LocalDateTime.of(resultDate, LocalTime.now()) : 
                                    LocalDateTime.now());
                                    
            labResult.setNotes(labNotesArea.getText());
            labResult.setStatus(labStatusComboBox.getValue());
            labResult.setUrgent(labIsUrgentCheckbox.isSelected());
            
            boolean success;
            if (editingLabResult != null) {
                success = labResultDAO.updateLabResult(labResult);
            } else {
                success = labResultDAO.createLabResult(labResult);
            }
            
            if (success) {
                labResultForm.setVisible(false);
                refreshLabResultsTab();
            } else {
                showAlert("Error", "Database Error", "Could not save the lab result.");
            }
        } catch (Exception e) {
            showAlert("Error", "Input Error", "Please check your input values: " + e.getMessage());
        }
    }
    
    /**
     * Delete a lab result
     */
    private void deleteLabResult(LabResult labResult) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Lab Result");
        alert.setContentText("Are you sure you want to delete this lab result? This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = labResultDAO.deleteLabResult(labResult.getResultId());
            if (success) {
                refreshLabResultsTab();
            } else {
                showAlert("Error", "Database Error", "Could not delete the lab result.");
            }
        }
    }
    
    /**
     * Cancel lab result edit or creation
     */
    @FXML
    private void cancelLabResult() {
        labResultForm.setVisible(false);
        editingLabResult = null;
    }
    
    /**
     * Clear the lab result form
     */
    private void clearLabResultForm() {
        labTestNameField.clear();
        labTestTypeField.clear();
        labResultArea.clear();
        labNormalRangeField.clear();
        labUnitField.clear();
        labIsAbnormalCheckbox.setSelected(false);
        labTestDatePicker.setValue(null);
        labResultDatePicker.setValue(null);
        labNotesArea.clear();
        labStatusComboBox.setValue("Pending");
        labIsUrgentCheckbox.setSelected(false);
    }
    
    /**
     * Create a new note
     */
    @FXML
    private void createNewNote() {
        if (selectedPatient == null) {
            showAlert("Error", "No patient selected", "Please select a patient first.");
            return;
        }
        
        editingNote = null;
        clearNoteForm();
        noteForm.setVisible(true);
        noteTypeComboBox.setValue("General");
    }
    
    /**
     * Edit a note
     */
    private void editNote(Note note) {
        editingNote = note;
        
        noteTitleField.setText(note.getTitle());
        noteContentArea.setText(note.getContent());
        noteTypeComboBox.setValue(note.getNoteType());
        noteIsPrivateCheckbox.setSelected(note.isPrivate());
        
        noteForm.setVisible(true);
    }
    
    /**
     * Save a note (create or update)
     */
    @FXML
    private void saveNote() {
        if (selectedPatient == null) {
            showAlert("Error", "No patient selected", "Please select a patient first.");
            return;
        }
        
        try {
            // Get the current doctor from the auth service
            Optional<User> doctorOpt = authService.getCurrentUser();
            if (!doctorOpt.isPresent()) {
                showAlert("Error", "Authentication Error", "Could not determine the current doctor.");
                return;
            }
            
            User doctor = doctorOpt.get();
            
            // Create or update the note
            Note note = (editingNote != null) 
                ? editingNote 
                : new Note();
            
            note.setPatientId(selectedPatient.getPatientId());
            note.setAuthorId(doctor.getUserId());
            note.setAuthorName(doctor.getFullName());
            note.setTitle(noteTitleField.getText());
            note.setContent(noteContentArea.getText());
            note.setNoteType(noteTypeComboBox.getValue());
            note.setPrivate(noteIsPrivateCheckbox.isSelected());
            
            boolean success;
            if (editingNote != null) {
                success = noteDAO.updateNote(note);
            } else {
                success = noteDAO.createNote(note);
            }
            
            if (success) {
                noteForm.setVisible(false);
                refreshNotesTab();
            } else {
                showAlert("Error", "Database Error", "Could not save the note.");
            }
        } catch (Exception e) {
            showAlert("Error", "Input Error", "Please check your input values: " + e.getMessage());
        }
    }
    
    /**
     * Delete a note
     */
    private void deleteNote(Note note) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Note");
        alert.setContentText("Are you sure you want to delete this note? This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = noteDAO.deleteNote(note.getNoteId());
            if (success) {
                refreshNotesTab();
            } else {
                showAlert("Error", "Database Error", "Could not delete the note.");
            }
        }
    }
    
    /**
     * Cancel note edit or creation
     */
    @FXML
    private void cancelNote() {
        noteForm.setVisible(false);
        editingNote = null;
    }
    
    /**
     * Clear the note form
     */
    private void clearNoteForm() {
        noteTitleField.clear();
        noteContentArea.clear();
        noteTypeComboBox.setValue("General");
        noteIsPrivateCheckbox.setSelected(false);
    }
    
    /**
     * Create a cell factory for action columns (edit/delete buttons)
     */
    private <T> Callback<TableColumn<T, String>, TableCell<T, String>> createActionCellFactory(
            java.util.function.Consumer<T> editAction, 
            java.util.function.Consumer<T> deleteAction) {
        
        return new Callback<>() {
            @Override
            public TableCell<T, String> call(TableColumn<T, String> param) {
                return new TableCell<>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final Pane pane = new Pane();
                    
                    {
                        // Configure buttons
                        editButton.setOnAction(event -> {
                            T item = getTableView().getItems().get(getIndex());
                            editAction.accept(item);
                        });
                        
                        deleteButton.setOnAction(event -> {
                            T item = getTableView().getItems().get(getIndex());
                            deleteAction.accept(item);
                        });
                        
                        // Add buttons to a container
                        pane.getChildren().addAll(editButton, deleteButton);
                        
                        // Position buttons
                        editButton.setLayoutX(0);
                        deleteButton.setLayoutX(50); // Adjust as needed
                    }
                    
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
    }
    
    /**
     * Show an alert dialog
     */
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Set a specific patient to be selected
     * @param patient The patient to select
     */
    public void setPatient(Patient patient) {
        // Save the given patient
        this.selectedPatient = patient;
        
        // Find and select the patient in the combo box
        if (patientSelector != null && patient != null) {
            // Look through the items to find the matching patient by ID
            for (Patient p : patientSelector.getItems()) {
                if (p.getPatientId() == patient.getPatientId()) {
                    patientSelector.getSelectionModel().select(p);
                    break;
                }
            }
            
            // If no matching patient was found in the combo box, reload the data
            if (patientSelector.getSelectionModel().getSelectedItem() == null) {
                // Add this patient to the combo box if it's not there
                patientSelector.getItems().add(patient);
                patientSelector.getSelectionModel().select(patient);
            }
            
            // Refresh all tabs with the selected patient's data
            refreshAllTabs();
        }
    }

    /**
     * Handle back button click
     */
    @FXML
    private void onBack() {
        // Close this window using patientSelector as reference instead of backButton
        if (patientSelector != null && patientSelector.getScene() != null) {
            Stage stage = (Stage) patientSelector.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Add reporting controls to the UI for Reports mode
     */
    private void addReportingControls() {
        // Add this to execute after the scene is fully loaded
        javafx.application.Platform.runLater(() -> {
            try {
                // Get the scene and stage
                if (patientSelector != null && patientSelector.getScene() != null) {
                    // Set window title
                    Stage stage = (Stage) patientSelector.getScene().getWindow();
                    stage.setTitle("Patient Records Reports");
                    
                    // Access TabPane for reports functionality
                    if (historyTabPane != null) {
                        // Add a Reports tab as the first tab
                        Tab reportsTab = new Tab("Reports Dashboard");
                        
                        // Create reports layout content
                        VBox reportsContent = new VBox(10);
                        reportsContent.setPadding(new Insets(15));
                        
                        // Add heading
                        Label reportsHeading = new Label("Patient Records Reports");
                        reportsHeading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                        
                        // Add report type selector
                        ComboBox<String> reportTypeSelector = new ComboBox<>();
                        reportTypeSelector.getItems().addAll(
                            "Patient Summary Report",
                            "Medical Records Report",
                            "Medications Report",
                            "Lab Results Report"
                        );
                        reportTypeSelector.setValue("Patient Summary Report");
                        reportTypeSelector.setPrefWidth(300);
                        
                        // Add date range selector
                        HBox dateRangeBox = new HBox(10);
                        dateRangeBox.setAlignment(Pos.CENTER_LEFT);
                        
                        Label fromLabel = new Label("From:");
                        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusMonths(3));
                        
                        Label toLabel = new Label("To:");
                        DatePicker toDatePicker = new DatePicker(LocalDate.now());
                        
                        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker);
                        
                        // Add generate report button
                        Button generateReportBtn = new Button("Generate Report");
                        generateReportBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                        generateReportBtn.setOnAction(e -> generateReport(reportTypeSelector.getValue(), 
                                                                         fromDatePicker.getValue(), 
                                                                         toDatePicker.getValue()));
                        
                        // Add a report preview area
                        TextArea reportPreview = new TextArea();
                        reportPreview.setWrapText(true);
                        reportPreview.setEditable(false);
                        reportPreview.setPrefHeight(400);
                        reportPreview.setPromptText("Report preview will appear here...");
                        
                        // Action buttons
                        HBox actionButtons = new HBox(10);
                        Button printReportBtn = new Button("Print Report");
                        Button exportPdfBtn = new Button("Export as PDF");
                        Button exportCsvBtn = new Button("Export as CSV");
                        
                        actionButtons.getChildren().addAll(printReportBtn, exportPdfBtn, exportCsvBtn);
                        actionButtons.setAlignment(Pos.CENTER_RIGHT);
                        
                        // Create a line instead of a separator
                        Line separator = new Line();
                        separator.setStartX(0);
                        separator.setEndX(Double.MAX_VALUE); // Make it span the full width
                        separator.setStrokeWidth(1);
                        separator.setStyle("-fx-stroke: #CCCCCC;");
                        
                        // Add all components to the reports content
                        reportsContent.getChildren().addAll(
                            reportsHeading, 
                            separator,
                            new Label("Select Report Type:"),
                            reportTypeSelector,
                            new Label("Select Date Range:"),
                            dateRangeBox,
                            generateReportBtn,
                            new Label("Report Preview:"),
                            reportPreview,
                            actionButtons
                        );
                        
                        // Set the content for the reports tab
                        reportsTab.setContent(reportsContent);
                        
                        // Add the reports tab to the beginning of the tab pane
                        historyTabPane.getTabs().add(0, reportsTab);
                        
                        // Select the reports tab by default
                        historyTabPane.getSelectionModel().select(0);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error adding reporting controls: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Generate a report based on the selected type and date range
     */
    private void generateReport(String reportType, LocalDate fromDate, LocalDate toDate) {
        // Create a simple report text
        StringBuilder reportText = new StringBuilder();
        
        reportText.append("REPORT TYPE: ").append(reportType).append("\n");
        reportText.append("DATE RANGE: ").append(fromDate).append(" to ").append(toDate).append("\n");
        reportText.append("GENERATED ON: ").append(LocalDateTime.now()).append("\n\n");
        
        if (selectedPatient != null) {
            reportText.append("PATIENT: ").append(selectedPatient.getFullName()).append("\n");
            reportText.append("PATIENT ID: ").append(selectedPatient.getPatientId()).append("\n");
            reportText.append("AGE: ").append(selectedPatient.getAge()).append(" years\n");
            reportText.append("GENDER: ").append(selectedPatient.getGender()).append("\n\n");
        } else {
            reportText.append("ALL PATIENTS\n\n");
        }
        
        // Add report-specific data based on type
        switch (reportType) {
            case "Patient Summary Report":
                reportText.append("=== PATIENT SUMMARY ===\n\n");
                
                if (selectedPatient != null) {
                    // Add patient details
                    reportText.append("Contact: ").append(selectedPatient.getPhoneNumber() != null ? 
                                                         selectedPatient.getPhoneNumber() : "N/A").append("\n");
                    reportText.append("Email: ").append(selectedPatient.getEmail() != null ? 
                                                       selectedPatient.getEmail() : "N/A").append("\n");
                    reportText.append("Address: ").append(selectedPatient.getAddress() != null ? 
                                                         selectedPatient.getAddress() : "N/A").append("\n");
                    
                    // Count records by type
                    int medRecordCount = 0;
                    int medicationCount = 0;
                    int labResultCount = 0;
                    
                    if (medicalRecordsTable != null && medicalRecordsTable.getItems() != null) {
                        medRecordCount = medicalRecordsTable.getItems().size();
                    }
                    
                    if (medicationsTable != null && medicationsTable.getItems() != null) {
                        medicationCount = medicationsTable.getItems().size();
                    }
                    
                    if (labResultsTable != null && labResultsTable.getItems() != null) {
                        labResultCount = labResultsTable.getItems().size();
                    }
                    
                    reportText.append("\nRECORD SUMMARY:\n");
                    reportText.append("- Medical Records: ").append(medRecordCount).append("\n");
                    reportText.append("- Current Medications: ").append(medicationCount).append("\n");
                    reportText.append("- Lab Results: ").append(labResultCount).append("\n");
                } else {
                    reportText.append("Please select a patient to generate a summary report.");
                }
                break;
                
            case "Medical Records Report":
                reportText.append("=== MEDICAL RECORDS REPORT ===\n\n");
                
                if (medicalRecordsTable != null && medicalRecordsTable.getItems() != null) {
                    ObservableList<MedicalRecord> records = medicalRecordsTable.getItems();
                    
                    if (records.isEmpty()) {
                        reportText.append("No medical records found for the selected period.\n");
                    } else {
                        reportText.append("Total Records: ").append(records.size()).append("\n\n");
                        
                        int count = 1;
                        for (MedicalRecord record : records) {
                            reportText.append("Record #").append(count++).append(":\n");
                            reportText.append("- Date: ").append(record.getVisitDate()).append("\n");
                            reportText.append("- Diagnosis: ").append(record.getDiagnosis()).append("\n");
                            reportText.append("- Treatment: ").append(record.getTreatment()).append("\n");
                            reportText.append("- Notes: ").append(record.getNotes()).append("\n");
                            reportText.append("\n");
                        }
                    }
                }
                break;
                
            case "Medications Report":
                reportText.append("=== MEDICATIONS REPORT ===\n\n");
                
                if (medicationsTable != null && medicationsTable.getItems() != null) {
                    ObservableList<Medication> medications = medicationsTable.getItems();
                    
                    if (medications.isEmpty()) {
                        reportText.append("No medications found for the selected period.\n");
                    } else {
                        reportText.append("Total Medications: ").append(medications.size()).append("\n\n");
                        
                        int count = 1;
                        for (Medication med : medications) {
                            reportText.append("Medication #").append(count++).append(":\n");
                            reportText.append("- Name: ").append(med.getName()).append("\n");
                            reportText.append("- Dosage: ").append(med.getDosage()).append("\n");
                            reportText.append("- Frequency: ").append(med.getFrequency()).append("\n");
                            reportText.append("- Start Date: ").append(med.getStartDate()).append("\n");
                            reportText.append("- End Date: ").append(med.getEndDate()).append("\n");
                            reportText.append("\n");
                        }
                    }
                }
                break;
                
            case "Lab Results Report":
                reportText.append("=== LAB RESULTS REPORT ===\n\n");
                
                if (labResultsTable != null && labResultsTable.getItems() != null) {
                    ObservableList<LabResult> labResults = labResultsTable.getItems();
                    
                    if (labResults.isEmpty()) {
                        reportText.append("No lab results found for the selected period.\n");
                    } else {
                        reportText.append("Total Lab Results: ").append(labResults.size()).append("\n\n");
                        
                        int count = 1;
                        for (LabResult result : labResults) {
                            reportText.append("Lab Result #").append(count++).append(":\n");
                            reportText.append("- Test: ").append(result.getTestName()).append("\n");
                            reportText.append("- Result: ").append(result.getResult()).append("\n");
                            reportText.append("- Date: ").append(result.getTestDate()).append("\n");
                            reportText.append("- Notes: ").append(result.getNotes()).append("\n");
                            reportText.append("\n");
                        }
                    }
                }
                break;
        }
        
        // Find the report preview TextArea and update it
        findReportPreviewTextArea(reportText.toString());
    }

    /**
     * Find and update the report preview TextArea
     * @param reportText The report text to display
     */
    private void findReportPreviewTextArea(String reportText) {
        javafx.application.Platform.runLater(() -> {
            try {
                if (historyTabPane != null) {
                    Tab reportsTab = historyTabPane.getTabs().get(0);
                    if (reportsTab != null && reportsTab.getContent() instanceof VBox) {
                        VBox reportsContent = (VBox) reportsTab.getContent();
                        
                        // Find the TextArea in the reports content
                        for (javafx.scene.Node node : reportsContent.getChildren()) {
                            if (node instanceof TextArea) {
                                TextArea reportPreview = (TextArea) node;
                                reportPreview.setText(reportText);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error updating report preview: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
} 
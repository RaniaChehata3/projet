package com.example.demo.controller;

import com.example.demo.database.MedicalRecordDAO;
import com.example.demo.database.PatientDAO;
import com.example.demo.model.MedicalRecord;
import com.example.demo.model.Patient;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Simplified controller for the Patient Details view.
 */
public class SimplePatientDetailsController {

    @FXML private Label patientNameLabel;
    @FXML private Label patientBasicInfoLabel;
    @FXML private Label fullNameValue;
    @FXML private Label ageValue;
    @FXML private Label genderValue;
    @FXML private Label phoneValue;
    @FXML private Label emailValue;
    @FXML private Label addressValue;
    @FXML private Label bloodTypeValue;
    @FXML private Label statusValue;
    @FXML private Label insuranceValue;
    @FXML private Label lastVisitValue;
    @FXML private Label recordsCountValue;
    
    @FXML private TableView<MedicalRecord> medicalRecordsTable;
    @FXML private TableColumn<MedicalRecord, String> recordDateColumn;
    @FXML private TableColumn<MedicalRecord, String> recordTypeColumn;
    @FXML private TableColumn<MedicalRecord, String> recordDoctorColumn;
    @FXML private TableColumn<MedicalRecord, String> recordDiagnosisColumn;
    @FXML private TableColumn<MedicalRecord, Void> recordActionsColumn;
    
    @FXML private Button addRecordButton;
    @FXML private Button closeButton;
    
    private Patient patient;
    private final PatientDAO patientDAO = PatientDAO.getInstance();
    private final MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO();
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        // Initialize table columns
        setupTableColumns();
    }
    
    /**
     * Set up the table columns for medical records
     */
    private void setupTableColumns() {
        // Configure medical records table columns
        recordDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getVisitDate().format(dateTimeFormatter)));
        
        recordTypeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getVisitType()));
        
        recordDoctorColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDoctorName() != null ? 
                cellData.getValue().getDoctorName() : "Dr. " + cellData.getValue().getDoctorId()));
        
        recordDiagnosisColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDiagnosis()));
        
        // Configure the actions column with a view button
        recordActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            
            {
                viewButton.getStyleClass().addAll("action-button", "small-button");
                viewButton.setOnAction(event -> {
                    MedicalRecord record = getTableView().getItems().get(getIndex());
                    viewMedicalRecord(record);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
    }
    
    /**
     * Set the patient to display details for
     * 
     * @param patient The patient to display
     */
    public void setPatient(Patient patient) {
        this.patient = patient;
        displayPatientInfo();
        loadMedicalRecords();
    }
    
    /**
     * Display patient information in the UI
     */
    private void displayPatientInfo() {
        if (patient == null) return;
        
        // Basic info at the top
        patientNameLabel.setText(patient.getFullName());
        patientBasicInfoLabel.setText(
            String.format("%d years | %s | ID: %d", 
                patient.getAge(), 
                patient.getGender(), 
                patient.getPatientId())
        );
        
        // Personal information section
        fullNameValue.setText(patient.getFullName());
        ageValue.setText(patient.getAge() + " years");
        genderValue.setText(patient.getGender());
        phoneValue.setText(patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "N/A");
        emailValue.setText(patient.getEmail() != null ? patient.getEmail() : "N/A");
        addressValue.setText(patient.getAddress() != null ? patient.getAddress() : "N/A");
        
        // Medical information section
        bloodTypeValue.setText(patient.getBloodType() != null ? patient.getBloodType() : "Unknown");
        
        // Set default status as "Active" since Patient model doesn't have status field
        String status = "Active";
        statusValue.setText(status);
        statusValue.getStyleClass().setAll("status-badge", "status-badge-active");
        
        insuranceValue.setText(patient.getInsuranceProvider() != null ? 
            patient.getInsuranceProvider() : "N/A");
    }
    
    /**
     * Load medical records for the patient
     */
    private void loadMedicalRecords() {
        if (patient == null) return;
        
        // Load records from DAO
        List<MedicalRecord> records = medicalRecordDAO.getMedicalRecordsByPatientId(patient.getPatientId());
        ObservableList<MedicalRecord> observableRecords = FXCollections.observableArrayList(records);
        
        // Update the table
        medicalRecordsTable.setItems(observableRecords);
        
        // Update record count
        recordsCountValue.setText(String.valueOf(records.size()));
        
        // Update last visit date if there are records
        if (!records.isEmpty()) {
            // Sort by date descending to get the most recent visit
            records.sort((a, b) -> b.getVisitDate().compareTo(a.getVisitDate()));
            lastVisitValue.setText(records.get(0).getVisitDate().format(dateFormatter));
        } else {
            lastVisitValue.setText("No visits");
        }
    }
    
    /**
     * View a medical record's details
     * 
     * @param record The medical record to view
     */
    private void viewMedicalRecord(MedicalRecord record) {
        if (record == null) return;
        
        try {
            // Load the medical record form for viewing
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/view/MedicalRecordFormView.fxml"));
            Parent root = loader.load();
            
            // Get the controller and setup
            MedicalRecordFormController controller = loader.getController();
            controller.setMode(MedicalRecordFormController.FormMode.EDIT);
            controller.setPatient(patient);
            controller.setMedicalRecord(record);
            
            // Set callback for when a record is saved
            controller.setOnSaveCallback(savedRecord -> {
                // Reload medical records after saving
                loadMedicalRecords();
            });
            
            // Show the form in a new window
            Stage stage = new Stage();
            stage.setTitle("Medical Record Details");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open medical record details.\nError: " + e.getMessage());
        }
    }
    
    /**
     * Handle Add Record button click
     */
    @FXML
    private void onAddRecord() {
        if (patient == null) return;
        
        try {
            // Load the medical record form for adding
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/view/MedicalRecordFormView.fxml"));
            Parent root = loader.load();
            
            // Get the controller and setup
            MedicalRecordFormController controller = loader.getController();
            controller.setMode(MedicalRecordFormController.FormMode.ADD);
            controller.setPatient(patient);
            
            // Set callback for when a record is saved
            controller.setOnSaveCallback(savedRecord -> {
                // Reload medical records after saving
                loadMedicalRecords();
            });
            
            // Show the form in a new window
            Stage stage = new Stage();
            stage.setTitle("Add Medical Record");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open medical record form.\nError: " + e.getMessage());
        }
    }
    
    /**
     * Handle Close button click
     */
    @FXML
    private void onClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Show an alert dialog
     * 
     * @param alertType The type of alert
     * @param title The alert title
     * @param message The alert message
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 
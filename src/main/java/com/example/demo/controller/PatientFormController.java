package com.example.demo.controller;

import com.example.demo.database.PatientDAO;
import com.example.demo.model.Patient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Controller for the Patient Form view.
 * Handles adding and editing patient information.
 */
public class PatientFormController {
    
    // Form mode enum
    public enum FormMode {
        ADD, EDIT
    }
    
    @FXML private Label titleLabel;
    @FXML private TextField fullNameField;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private TextField bloodTypeField;
    @FXML private TextField insuranceProviderField;
    @FXML private TextField insuranceNumberField;
    @FXML private TextField emergencyContactField;
    @FXML private TextField emergencyPhoneField;
    @FXML private ComboBox<String> statusComboBox;
    
    // New fields for medical data
    @FXML private TextArea medicalHistoryArea;
    @FXML private TextArea allergiesArea;
    @FXML private VBox medicalRecordButtonsBox;
    @FXML private Button viewMedicalRecordsButton;
    
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private final PatientDAO patientDAO = PatientDAO.getInstance();
    private FormMode mode = FormMode.ADD;
    private Patient patient;
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        // Initialize gender dropdown
        genderComboBox.getItems().addAll("Male", "Female", "Other", "Prefer not to say");
        genderComboBox.setValue("Male");
        
        // Initialize status dropdown
        statusComboBox.getItems().addAll("Active", "Inactive", "Transferred", "Deceased");
        statusComboBox.setValue("Active");
        
        // Set default date to today
        dateOfBirthPicker.setValue(LocalDate.now().minusYears(30));
        
        // Medical record buttons are only shown in EDIT mode
        if (medicalRecordButtonsBox != null) {
            medicalRecordButtonsBox.setVisible(false);
        }
        
        if (viewMedicalRecordsButton != null) {
            viewMedicalRecordsButton.setVisible(false);
        }
    }
    
    /**
     * Set the form mode (add or edit).
     * @param mode The form mode
     */
    public void setMode(FormMode mode) {
        this.mode = mode;
        updateFormTitle();
        
        // Show/hide medical record buttons based on mode
        if (medicalRecordButtonsBox != null) {
            medicalRecordButtonsBox.setVisible(mode == FormMode.EDIT);
        }
        
        if (viewMedicalRecordsButton != null) {
            viewMedicalRecordsButton.setVisible(mode == FormMode.EDIT);
        }
    }
    
    /**
     * Set the patient for editing.
     * @param patient The patient to edit
     */
    public void setPatient(Patient patient) {
        this.patient = patient;
        populateForm();
    }
    
    /**
     * Update the form title based on the mode.
     */
    private void updateFormTitle() {
        if (mode == FormMode.ADD) {
            titleLabel.setText("Add New Patient");
            saveButton.setText("Add Patient");
        } else {
            titleLabel.setText("Edit Patient");
            saveButton.setText("Save Changes");
        }
    }
    
    /**
     * Populate the form with patient data for editing.
     */
    private void populateForm() {
        if (patient != null) {
            fullNameField.setText(patient.getFullName());
            dateOfBirthPicker.setValue(patient.getDateOfBirth());
            genderComboBox.setValue(patient.getGender());
            phoneField.setText(patient.getPhoneNumber());
            emailField.setText(patient.getEmail());
            addressField.setText(patient.getAddress());
            cityField.setText(patient.getAddress() != null ? extractCity(patient.getAddress()) : "");
            bloodTypeField.setText(patient.getBloodType());
            insuranceProviderField.setText(patient.getInsuranceProvider());
            insuranceNumberField.setText(patient.getInsuranceNumber());
            emergencyContactField.setText(patient.getEmergencyContactName() != null ? patient.getEmergencyContactName() : "");
            emergencyPhoneField.setText(patient.getEmergencyContactPhone() != null ? patient.getEmergencyContactPhone() : "");
            statusComboBox.setValue(patient.isActive() ? "Active" : "Inactive");
            
            // Set medical history and allergies text areas if they exist
            if (medicalHistoryArea != null) {
                medicalHistoryArea.setText(patient.getMedicalHistory() != null ? patient.getMedicalHistory() : "");
            }
            
            if (allergiesArea != null) {
                allergiesArea.setText(patient.getAllergies() != null ? patient.getAllergies() : "");
            }
        }
    }
    
    /**
     * Extract city from address string
     * @param address The full address
     * @return The extracted city
     */
    private String extractCity(String address) {
        if (address != null && address.contains(",")) {
            String[] parts = address.split(",");
            return parts[parts.length - 1].trim();
        }
        return "";
    }
    
    /**
     * Handle save button click.
     * @param event The action event
     */
    @FXML
    private void onSave(ActionEvent event) {
        if (validateForm()) {
            if (mode == FormMode.ADD) {
                addPatient();
            } else {
                updatePatient();
            }
            closeForm();
        }
    }
    
    /**
     * Handle cancel button click.
     * @param event The action event
     */
    @FXML
    private void onCancel(ActionEvent event) {
        closeForm();
    }
    
    /**
     * Close the form.
     */
    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Handle view medical records button click.
     * @param event The action event
     */
    @FXML
    private void onViewMedicalRecords(ActionEvent event) {
        if (patient == null || patient.getPatientId() <= 0) {
            showErrorAlert("Error", "Please save the patient first before viewing medical records.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/view/PatientHistoryView.fxml"));
            Parent root = loader.load();
            
            PatientHistoryController controller = loader.getController();
            controller.setPatient(patient);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Medical Records for " + patient.getFullName());
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (IOException e) {
            showErrorAlert("Error", "Could not open medical records: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle button click to add a medical record.
     * @param event The action event
     */
    @FXML
    private void onAddMedicalRecord(ActionEvent event) {
        if (patient == null || patient.getPatientId() <= 0) {
            showErrorAlert("Error", "Please save the patient first before adding a medical record.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/view/PatientHistoryView.fxml"));
            Parent root = loader.load();
            
            PatientHistoryController controller = loader.getController();
            controller.setPatient(patient);
            
            // Call the method to create a new medical record if it exists
            try {
                java.lang.reflect.Method method = controller.getClass().getDeclaredMethod("createNewMedicalRecord");
                method.setAccessible(true);
                method.invoke(controller);
            } catch (Exception e) {
                // Method might not exist or be accessible
                System.out.println("Could not call createNewMedicalRecord: " + e.getMessage());
            }
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Medical Record for " + patient.getFullName());
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (IOException e) {
            showErrorAlert("Error", "Could not open medical records: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Validate the form inputs.
     * @return true if valid, false otherwise
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        if (fullNameField.getText().trim().isEmpty()) {
            errors.append("Full name is required.\n");
        }
        
        if (dateOfBirthPicker.getValue() == null) {
            errors.append("Date of birth is required.\n");
        } else if (dateOfBirthPicker.getValue().isAfter(LocalDate.now())) {
            errors.append("Date of birth cannot be in the future.\n");
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            errors.append("Phone number is required.\n");
        }
        
        if (!errors.toString().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    /**
     * Add a new patient.
     */
    private void addPatient() {
        Patient newPatient = createPatientFromForm();
        boolean success = patientDAO.createPatient(newPatient);
        
        if (success) {
            showSuccessAlert("Patient Added", "Patient has been successfully added.");
        } else {
            showErrorAlert("Add Failed", "Failed to add patient. Please try again.");
        }
    }
    
    /**
     * Update an existing patient.
     */
    private void updatePatient() {
        Patient updatedPatient = createPatientFromForm();
        updatedPatient.setPatientId(patient.getPatientId());
        
        boolean success = patientDAO.updatePatient(updatedPatient);
        
        if (success) {
            showSuccessAlert("Patient Updated", "Patient information has been successfully updated.");
        } else {
            showErrorAlert("Update Failed", "Failed to update patient. Please try again.");
        }
    }
    
    /**
     * Create a Patient object from form inputs.
     * @return The Patient object
     */
    private Patient createPatientFromForm() {
        Patient formPatient = new Patient();
        
        // Split full name into first and last name
        String fullName = fullNameField.getText().trim();
        String[] nameParts = fullName.split("\\s+", 2);
        formPatient.setFirstName(nameParts[0]);
        if (nameParts.length > 1) {
            formPatient.setLastName(nameParts[1]);
        } else {
            formPatient.setLastName("");
        }
        
        formPatient.setDateOfBirth(dateOfBirthPicker.getValue());
        formPatient.setGender(genderComboBox.getValue());
        formPatient.setPhoneNumber(phoneField.getText().trim());
        formPatient.setEmail(emailField.getText().trim());
        formPatient.setAddress(addressField.getText().trim());
        formPatient.setBloodType(bloodTypeField.getText().trim());
        formPatient.setInsuranceProvider(insuranceProviderField.getText().trim());
        formPatient.setInsuranceNumber(insuranceNumberField.getText().trim());
        formPatient.setEmergencyContactName(emergencyContactField.getText().trim());
        formPatient.setEmergencyContactPhone(emergencyPhoneField.getText().trim());
        formPatient.setActive(statusComboBox.getValue().equals("Active"));
        
        // Set medical history and allergies if text areas exist
        if (medicalHistoryArea != null) {
            formPatient.setMedicalHistory(medicalHistoryArea.getText().trim());
        }
        
        if (allergiesArea != null) {
            formPatient.setAllergies(allergiesArea.getText().trim());
        }
        
        if (mode == FormMode.ADD) {
            formPatient.setRegistrationDate(LocalDate.now());
        } else {
            formPatient.setRegistrationDate(patient.getRegistrationDate());
        }
        
        return formPatient;
    }
    
    /**
     * Show a success alert.
     * @param header The alert header
     * @param content The alert content
     */
    private void showSuccessAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Show an error alert.
     * @param header The alert header
     * @param content The alert content
     */
    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
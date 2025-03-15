package com.example.demo.controller;

import com.example.demo.database.PatientDAO;
import com.example.demo.model.Patient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Controller for the Doctor's Patient List view
 */
public class PatientListController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> idColumn;
    @FXML private TableColumn<Patient, String> nameColumn;
    @FXML private TableColumn<Patient, String> ageColumn;
    @FXML private TableColumn<Patient, String> genderColumn;
    @FXML private TableColumn<Patient, String> contactColumn;
    @FXML private TableColumn<Patient, String> emailColumn;
    @FXML private TableColumn<Patient, String> lastVisitColumn;
    @FXML private TableColumn<Patient, String> statusColumn;
    @FXML private TableColumn<Patient, Void> actionsColumn;
    @FXML private Label totalPatientsLabel;
    @FXML private Pagination patientsPagination;
    
    private final PatientDAO patientDAO = PatientDAO.getInstance();
    private ObservableList<Patient> patientsList = FXCollections.observableArrayList();
    private static final int ITEMS_PER_PAGE = 10;
    private String currentSearchTerm = "";
    private String currentFilterValue = "All";
    private String currentSortValue = "Name (A-Z)";
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize filter combo box
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All", "Active", "Inactive"
        ));
        filterComboBox.setValue("All");
        filterComboBox.setOnAction(e -> refreshPatientList());
        
        // Initialize sort combo box
        sortComboBox.setItems(FXCollections.observableArrayList(
            "Name (A-Z)", "Name (Z-A)", "ID (Asc)", "ID (Desc)", "Most Recent", "Oldest"
        ));
        sortComboBox.setValue("Name (A-Z)");
        sortComboBox.setOnAction(e -> refreshPatientList());
        
        // Initialize columns
        idColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getPatientId())));
            
        nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));
            
        ageColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getAge())));
            
        genderColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getGender()));
            
        contactColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPhoneNumber()));
            
        emailColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEmail()));
            
        lastVisitColumn.setCellValueFactory(cellData -> {
            // This would normally be fetched from the medical records
            // For now, displaying registration date instead
            LocalDate date = cellData.getValue().getRegistrationDate();
            if (date != null) {
                return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new SimpleStringProperty("N/A");
        });
            
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));
        
        // Set up the actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonBox = new HBox(5);
            
            {
                // Style buttons
                viewButton.getStyleClass().addAll("action-button", "view-button", "small-button");
                editButton.getStyleClass().addAll("action-button", "edit-button", "small-button");
                deleteButton.getStyleClass().addAll("action-button", "delete-button", "small-button");
                
                // Set button icons
                Text viewIcon = new Text("👁");
                viewIcon.getStyleClass().add("menu-icon");
                viewButton.setGraphic(viewIcon);
                
                Text editIcon = new Text("✏");
                editIcon.getStyleClass().add("menu-icon");
                editButton.setGraphic(editIcon);
                
                Text deleteIcon = new Text("🗑");
                deleteIcon.getStyleClass().add("menu-icon");
                deleteButton.setGraphic(deleteIcon);
                
                // Set up button actions
                viewButton.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    handleViewPatient(patient);
                });
                
                editButton.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    handleEditPatient(patient);
                });
                
                deleteButton.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    handleDeletePatient(patient);
                });
                
                buttonBox.getChildren().addAll(viewButton, editButton, deleteButton);
                buttonBox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
        
        // Set up pagination
        patientsPagination.setPageFactory(this::createPage);
        
        // Load initial data
        refreshPatientList();
    }
    
    /**
     * Creates a page for the pagination control
     */
    private TableView<Patient> createPage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, patientsList.size());
        
        if (fromIndex >= patientsList.size()) {
            return new TableView<>();
        }
        
        patientsTable.setItems(FXCollections.observableArrayList(
            patientsList.subList(fromIndex, toIndex)
        ));
        
        return patientsTable;
    }
    
    /**
     * Refreshes the patient list based on current search, filter, and sort settings
     */
    private void refreshPatientList() {
        // Get current values
        currentSearchTerm = searchField.getText().trim();
        currentFilterValue = filterComboBox.getValue();
        currentSortValue = sortComboBox.getValue();
        
        // Load patients
        List<Patient> patients;
        
        if (!currentSearchTerm.isEmpty()) {
            // Search based on the search term
            patients = patientDAO.searchPatients(currentSearchTerm);
        } else {
            // Get all patients
            patients = patientDAO.getAllPatients();
        }
        
        // Filter patients
        if (!"All".equals(currentFilterValue)) {
            boolean activeFilter = "Active".equals(currentFilterValue);
            patients.removeIf(patient -> patient.isActive() != activeFilter);
        }
        
        // Sort patients based on selected option
        patients.sort((p1, p2) -> {
            switch (currentSortValue) {
                case "Name (A-Z)":
                    return p1.getLastName().compareToIgnoreCase(p2.getLastName());
                case "Name (Z-A)":
                    return p2.getLastName().compareToIgnoreCase(p1.getLastName());
                case "ID (Asc)":
                    return Integer.compare(p1.getPatientId(), p2.getPatientId());
                case "ID (Desc)":
                    return Integer.compare(p2.getPatientId(), p1.getPatientId());
                case "Most Recent":
                    return p2.getRegistrationDate().compareTo(p1.getRegistrationDate());
                case "Oldest":
                    return p1.getRegistrationDate().compareTo(p2.getRegistrationDate());
                default:
                    return 0;
            }
        });
        
        // Update observable list
        patientsList.setAll(patients);
        
        // Update pagination
        int pageCount = (int) Math.ceil((double) patientsList.size() / ITEMS_PER_PAGE);
        patientsPagination.setPageCount(Math.max(1, pageCount));
        patientsPagination.setCurrentPageIndex(0);
        
        // Update total patients label
        totalPatientsLabel.setText(String.valueOf(patientsList.size()));
        
        // Set first page
        createPage(0);
    }
    
    /**
     * Handle search button action
     */
    @FXML
    private void handleSearch() {
        refreshPatientList();
    }
    
    /**
     * Handle reset search button action
     */
    @FXML
    private void handleResetSearch() {
        searchField.clear();
        filterComboBox.setValue("All");
        sortComboBox.setValue("Name (A-Z)");
        refreshPatientList();
    }
    
    /**
     * Handle add patient button action
     */
    @FXML
    private void handleAddPatient() {
        openPatientForm(null, PatientFormController.FormMode.ADD, patient -> refreshPatientList());
    }
    
    /**
     * Handle edit patient button action
     */
    private void handleEditPatient(Patient patient) {
        openPatientForm(patient, PatientFormController.FormMode.EDIT, p -> refreshPatientList());
    }
    
    /**
     * Handle view patient button action
     */
    private void handleViewPatient(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/view/PatientDetailsView.fxml"));
            Parent root = loader.load();
            
            PatientDetailsController controller = loader.getController();
            controller.setPatient(patient);
            
            Stage stage = new Stage();
            stage.setTitle("Patient Details - " + patient.getFirstName() + " " + patient.getLastName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open patient details view.\nError: " + e.getMessage());
        }
    }
    
    /**
     * Handle delete patient button action
     */
    private void handleDeletePatient(Patient patient) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Patient");
        confirmation.setHeaderText("Delete Patient Record");
        confirmation.setContentText("Are you sure you want to delete " + patient.getFirstName() + " " + patient.getLastName() + "?\nThis action cannot be undone.");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = patientDAO.deletePatient(patient.getPatientId());
            if (success) {
                refreshPatientList();
                showSuccessAlert("Success", "Patient deleted successfully");
            } else {
                showErrorAlert("Error", "Failed to delete patient");
            }
        }
    }
    
    /**
     * Opens the patient form for adding or editing a patient
     */
    private void openPatientForm(Patient patient, PatientFormController.FormMode mode, Consumer<Patient> onSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/view/PatientFormView.fxml"));
            Parent root = loader.load();
            
            PatientFormController controller = loader.getController();
            controller.setMode(mode);
            
            if (patient != null) {
                controller.setPatient(patient);
            }
            
            // Set callback for when patient is saved
            controller.setOnSaveCallback(onSuccess);
            
            Stage stage = new Stage();
            stage.setTitle(mode == PatientFormController.FormMode.ADD ? "Add New Patient" : "Edit Patient");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open patient form.\nError: " + e.getMessage());
        }
    }
    
    /**
     * Shows a success alert
     */
    private void showSuccessAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows an error alert
     */
    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
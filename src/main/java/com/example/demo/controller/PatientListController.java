package com.example.demo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the Doctor's Patient List view
 */
public class PatientListController {

    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> filterComboBox;
    
    @FXML
    private TableView<Patient> patientsTable;
    
    @FXML
    private TableColumn<Patient, String> idColumn;
    
    @FXML
    private TableColumn<Patient, String> nameColumn;
    
    @FXML
    private TableColumn<Patient, Integer> ageColumn;
    
    @FXML
    private TableColumn<Patient, String> genderColumn;
    
    @FXML
    private TableColumn<Patient, String> contactColumn;
    
    @FXML
    private TableColumn<Patient, String> lastVisitColumn;
    
    @FXML
    private TableColumn<Patient, String> statusColumn;
    
    @FXML
    private TableColumn<Patient, Button> actionsColumn;
    
    @FXML
    private Label totalPatientsLabel;
    
    @FXML
    private Pagination patientsPagination;
    
    private ObservableList<Patient> patientsList = FXCollections.observableArrayList();
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize the filter dropdown
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All Patients", "Recent Visits", "Upcoming Appointments", "Active", "Inactive"
        ));
        filterComboBox.getSelectionModel().selectFirst();
        
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        lastVisitColumn.setCellValueFactory(new PropertyValueFactory<>("lastVisit"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Configure actions column with buttons
        actionsColumn.setCellFactory(col -> {
            TableCell<Patient, Button> cell = new TableCell<>() {
                private final Button viewButton = new Button("View");
                
                {
                    viewButton.getStyleClass().add("action-button");
                    viewButton.setOnAction(event -> {
                        Patient patient = getTableView().getItems().get(getIndex());
                        handleViewPatient(patient);
                    });
                }
                
                @Override
                protected void updateItem(Button item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(viewButton);
                    }
                }
            };
            return cell;
        });
        
        // Load sample data
        loadSamplePatients();
        
        // Set up table with data
        patientsTable.setItems(patientsList);
        
        // Update total patients count
        totalPatientsLabel.setText(String.valueOf(patientsList.size()));
        
        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            // This would typically filter the patients based on the search text
            // For now, just show an info alert
            if (!oldValue.equals(newValue) && !newValue.isEmpty()) {
                System.out.println("Searching for: " + newValue);
            }
        });
    }
    
    /**
     * Load sample patient data
     */
    private void loadSamplePatients() {
        patientsList.add(new Patient("P-10001", "John Smith", 42, "Male", "555-123-4567", "2023-05-15", "Active"));
        patientsList.add(new Patient("P-10002", "Sarah Johnson", 35, "Female", "555-987-6543", "2023-05-10", "Active"));
        patientsList.add(new Patient("P-10003", "Robert Garcia", 67, "Male", "555-456-7890", "2023-04-28", "Active"));
        patientsList.add(new Patient("P-10004", "Lisa Chen", 29, "Female", "555-789-0123", "2023-04-15", "Active"));
        patientsList.add(new Patient("P-10005", "David Wilson", 51, "Male", "555-234-5678", "2023-03-22", "Inactive"));
        patientsList.add(new Patient("P-10006", "Maria Rodriguez", 48, "Female", "555-345-6789", "2023-03-10", "Active"));
        patientsList.add(new Patient("P-10007", "Thomas Lee", 73, "Male", "555-456-7891", "2023-02-18", "Inactive"));
        patientsList.add(new Patient("P-10008", "Jennifer Taylor", 31, "Female", "555-567-8901", "2023-02-05", "Active"));
    }
    
    /**
     * Handle the "Add Patient" button click
     */
    @FXML
    private void handleAddPatient() {
        showNotImplementedAlert("Add New Patient");
    }
    
    /**
     * Handle viewing a specific patient
     */
    private void handleViewPatient(Patient patient) {
        showNotImplementedAlert("View Patient Details");
    }
    
    /**
     * Show an alert for features that are not yet implemented
     */
    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Not Implemented");
        alert.setHeaderText(feature);
        alert.setContentText("This feature is not implemented in the current version.");
        alert.showAndWait();
    }
    
    /**
     * Class representing a Patient
     */
    public static class Patient {
        private final String id;
        private final String name;
        private final int age;
        private final String gender;
        private final String contact;
        private final String lastVisit;
        private final String status;
        
        public Patient(String id, String name, int age, String gender, String contact, String lastVisit, String status) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.contact = contact;
            this.lastVisit = lastVisit;
            this.status = status;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getGender() { return gender; }
        public String getContact() { return contact; }
        public String getLastVisit() { return lastVisit; }
        public String getStatus() { return status; }
    }
} 
package com.example.demo.controller;

import com.example.demo.database.PatientDAO;
import com.example.demo.database.DatabaseConnection;
import com.example.demo.model.Patient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.application.Platform;

/**
 * Controller for the Patient Management view.
 * Handles comprehensive patient management functionality including search, filter,
 * add, edit, delete operations, and statistics.
 */
public class PatientManagementController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private ComboBox<String> chartTypeComboBox;
    
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> idColumn;
    @FXML private TableColumn<Patient, String> nameColumn;
    @FXML private TableColumn<Patient, String> ageColumn;
    @FXML private TableColumn<Patient, String> genderColumn;
    @FXML private TableColumn<Patient, String> phoneColumn;
    @FXML private TableColumn<Patient, String> emailColumn;
    @FXML private TableColumn<Patient, String> lastVisitColumn;
    @FXML private TableColumn<Patient, String> statusColumn;
    @FXML private TableColumn<Patient, Void> actionsColumn;
    
    @FXML private Pagination patientsPagination;
    @FXML private Label totalPatientsCountLabel;
    @FXML private Label activePatientsCountLabel;
    @FXML private Label newPatientsCountLabel;
    @FXML private Label maleCountLabel;
    @FXML private Label femaleCountLabel;
    @FXML private Label resultsCountLabel;
    
    @FXML private PieChart demographicsChart;
    @FXML private StackPane chartContainer;

    private final PatientDAO patientDAO = PatientDAO.getInstance();
    private ObservableList<Patient> patientsList = FXCollections.observableArrayList();
    private ObservableList<Patient> filteredPatientsList = FXCollections.observableArrayList();
    private static final int ITEMS_PER_PAGE = 10;
    private String currentSearchTerm = "";
    private String currentFilterValue = "All";
    private String currentSortValue = "Name (A-Z)";
    private String currentChartType = "Age Groups";

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Configure combo boxes
        initializeComboBoxes();
        
        // Configure table columns
        initializeTableColumns();
        
        // Load initial data and statistics
        refreshData();
        
        // Initialize charts
        initializeCharts();
    }
    
    /**
     * Initialize combo boxes with values and event handlers
     */
    private void initializeComboBoxes() {
        // Status filter combo box
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All", "Active", "Inactive"
        ));
        filterComboBox.setValue("All");
        filterComboBox.setOnAction(e -> refreshData());
        
        // Sort combo box
        sortComboBox.setItems(FXCollections.observableArrayList(
            "Name (A-Z)", "Name (Z-A)", "ID (Asc)", "ID (Desc)", 
            "Age (Oldest)", "Age (Youngest)", "Recent Patients"
        ));
        sortComboBox.setValue("Name (A-Z)");
        sortComboBox.setOnAction(e -> refreshData());
        
        // Chart type combo box
        chartTypeComboBox.setItems(FXCollections.observableArrayList(
            "Age Groups", "Gender Distribution", "Blood Types", "Registration by Month"
        ));
        chartTypeComboBox.setValue("Age Groups");
        chartTypeComboBox.setOnAction(e -> {
            currentChartType = chartTypeComboBox.getValue();
            updateChart();
        });
    }
    
    /**
     * Initialize table columns with cell factories and value factories
     */
    private void initializeTableColumns() {
        // ID column
        idColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getPatientId())));
        
        // Name column
        nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));
        
        // Age column
        ageColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getAge())));
        
        // Gender column
        genderColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getGender()));
        
        // Phone column
        phoneColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPhoneNumber()));
        
        // Email column
        emailColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEmail()));
        
        // Last visit column - Would be from medical records but using registration date for now
        lastVisitColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getRegistrationDate();
            if (date != null) {
                return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new SimpleStringProperty("N/A");
        });
        
        // Status column
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));
        
        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button medicalRecordsButton = new Button("Records");
            private final HBox buttonBox = new HBox(5);
            
            {
                // Style buttons
                viewButton.getStyleClass().addAll("action-button", "view-button", "small-button");
                editButton.getStyleClass().addAll("action-button", "edit-button", "small-button");
                deleteButton.getStyleClass().addAll("action-button", "delete-button", "small-button");
                medicalRecordsButton.getStyleClass().addAll("action-button", "primary-button", "small-button");
                
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
                
                Text medicalRecordsIcon = new Text("📋");
                medicalRecordsIcon.getStyleClass().add("menu-icon");
                medicalRecordsButton.setGraphic(medicalRecordsIcon);
                
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
                
                medicalRecordsButton.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    viewMedicalRecords(patient);
                });
                
                buttonBox.getChildren().addAll(viewButton, editButton, medicalRecordsButton, deleteButton);
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
    }
    
    /**
     * Initialize charts
     */
    private void initializeCharts() {
        demographicsChart.setAnimated(true);
        updateChart();
    }
    
    /**
     * Update the chart based on the selected chart type
     */
    private void updateChart() {
        demographicsChart.getData().clear();
        
        switch (currentChartType) {
            case "Age Groups":
                createAgeGroupChart();
                break;
            case "Gender Distribution":
                createGenderDistributionChart();
                break;
            case "Blood Types":
                createBloodTypeChart();
                break;
            case "Registration by Month":
                createRegistrationByMonthChart();
                break;
            default:
                createAgeGroupChart();
                break;
        }
    }
    
    /**
     * Create age group distribution chart
     */
    private void createAgeGroupChart() {
        Map<String, Long> ageGroups = patientsList.stream()
            .collect(Collectors.groupingBy(patient -> {
                int age = patient.getAge();
                if (age < 18) return "Under 18";
                else if (age < 30) return "18-29";
                else if (age < 45) return "30-44";
                else if (age < 60) return "45-59";
                else return "60+";
            }, Collectors.counting()));
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        ageGroups.forEach((group, count) -> 
            pieChartData.add(new PieChart.Data(group + " (" + count + ")", count)));
        
        demographicsChart.setData(pieChartData);
        demographicsChart.setTitle("Patient Age Distribution");
    }
    
    /**
     * Create gender distribution chart
     */
    private void createGenderDistributionChart() {
        Map<String, Long> genderCounts = patientsList.stream()
            .collect(Collectors.groupingBy(
                patient -> patient.getGender() != null ? patient.getGender() : "Unknown", 
                Collectors.counting()
            ));
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        genderCounts.forEach((gender, count) -> 
            pieChartData.add(new PieChart.Data(gender + " (" + count + ")", count)));
        
        demographicsChart.setData(pieChartData);
        demographicsChart.setTitle("Patient Gender Distribution");
    }
    
    /**
     * Create blood type distribution chart
     */
    private void createBloodTypeChart() {
        Map<String, Long> bloodTypes = patientsList.stream()
            .collect(Collectors.groupingBy(
                patient -> patient.getBloodType() != null ? patient.getBloodType() : "Unknown", 
                Collectors.counting()
            ));
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        bloodTypes.forEach((type, count) -> 
            pieChartData.add(new PieChart.Data(type + " (" + count + ")", count)));
        
        demographicsChart.setData(pieChartData);
        demographicsChart.setTitle("Patient Blood Type Distribution");
    }
    
    /**
     * Create registration by month chart
     */
    private void createRegistrationByMonthChart() {
        Map<Month, Long> registrationByMonth = patientsList.stream()
            .filter(patient -> patient.getRegistrationDate() != null)
            .collect(Collectors.groupingBy(
                patient -> patient.getRegistrationDate().getMonth(), 
                Collectors.counting()
            ));
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        registrationByMonth.forEach((month, count) -> 
            pieChartData.add(new PieChart.Data(month.toString() + " (" + count + ")", count)));
        
        demographicsChart.setData(pieChartData);
        demographicsChart.setTitle("Patient Registrations by Month");
    }
    
    /**
     * Creates a page for the pagination control
     */
    private TableView<Patient> createPage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredPatientsList.size());
        
        if (fromIndex >= filteredPatientsList.size()) {
            return new TableView<>();
        }
        
        patientsTable.setItems(FXCollections.observableArrayList(
            filteredPatientsList.subList(fromIndex, toIndex)
        ));
        
        return patientsTable;
    }
    
    /**
     * Refresh data based on search, filter, and sort criteria
     */
    private void refreshData() {
        // Get current values
        currentSearchTerm = searchField.getText().trim();
        currentFilterValue = filterComboBox.getValue();
        currentSortValue = sortComboBox.getValue();
        
        // Load all patients
        loadPatientsData();
        
        // Apply filters
        applyFilters();
        
        // Update pagination and stats
        updatePaginationAndStats();
        
        // Update chart
        updateChart();
    }
    
    /**
     * Load patients data from database
     */
    private void loadPatientsData() {
        List<Patient> patients;
        
        if (!currentSearchTerm.isEmpty()) {
            // Search based on the search term
            patients = patientDAO.searchPatients(currentSearchTerm);
        } else {
            // Get all patients
            patients = patientDAO.getAllPatients();
        }
        
        patientsList.setAll(patients);
    }
    
    /**
     * Apply filters and sorting to the patient list
     */
    private void applyFilters() {
        // Start with all patients
        List<Patient> filtered = patientsList;
        
        // Apply status filter
        if (!"All".equals(currentFilterValue)) {
            boolean activeFilter = "Active".equals(currentFilterValue);
            filtered = filtered.stream()
                .filter(patient -> patient.isActive() == activeFilter)
                .collect(Collectors.toList());
        }
        
        // Apply sorting
        filtered.sort((p1, p2) -> {
            switch (currentSortValue) {
                case "Name (A-Z)":
                    return (p1.getLastName() + p1.getFirstName())
                        .compareToIgnoreCase(p2.getLastName() + p2.getFirstName());
                case "Name (Z-A)":
                    return (p2.getLastName() + p2.getFirstName())
                        .compareToIgnoreCase(p1.getLastName() + p1.getFirstName());
                case "ID (Asc)":
                    return Integer.compare(p1.getPatientId(), p2.getPatientId());
                case "ID (Desc)":
                    return Integer.compare(p2.getPatientId(), p1.getPatientId());
                case "Age (Oldest)":
                    return Integer.compare(p2.getAge(), p1.getAge());
                case "Age (Youngest)":
                    return Integer.compare(p1.getAge(), p2.getAge());
                case "Recent Patients":
                    if (p1.getRegistrationDate() == null) return 1;
                    if (p2.getRegistrationDate() == null) return -1;
                    return p2.getRegistrationDate().compareTo(p1.getRegistrationDate());
                default:
                    return 0;
            }
        });
        
        // Update filtered list
        filteredPatientsList.setAll(filtered);
    }
    
    /**
     * Update pagination and stats after data refresh
     */
    private void updatePaginationAndStats() {
        // Update pagination
        int pageCount = (int) Math.ceil((double) filteredPatientsList.size() / ITEMS_PER_PAGE);
        patientsPagination.setPageCount(Math.max(1, pageCount));
        patientsPagination.setCurrentPageIndex(0);
        
        // Show first page
        createPage(0);
        
        // Update result count label
        resultsCountLabel.setText("(" + filteredPatientsList.size() + " results)");
        
        // Update stats
        updateStatistics();
    }
    
    /**
     * Update statistics labels
     */
    private void updateStatistics() {
        // Total patients
        totalPatientsCountLabel.setText(String.valueOf(patientsList.size()));
        
        // Active patients
        long activeCount = patientsList.stream().filter(Patient::isActive).count();
        activePatientsCountLabel.setText(String.valueOf(activeCount));
        
        // New patients this month
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        long newPatientsThisMonth = patientsList.stream()
            .filter(p -> p.getRegistrationDate() != null && p.getRegistrationDate().isAfter(firstDayOfMonth))
            .count();
        newPatientsCountLabel.setText(String.valueOf(newPatientsThisMonth));
        
        // Gender counts
        long maleCount = patientsList.stream()
            .filter(p -> p.getGender() != null && p.getGender().equalsIgnoreCase("Male"))
            .count();
        long femaleCount = patientsList.stream()
            .filter(p -> p.getGender() != null && p.getGender().equalsIgnoreCase("Female"))
            .count();
        
        maleCountLabel.setText("M: " + maleCount);
        femaleCountLabel.setText("F: " + femaleCount);
    }
    
    /**
     * Handle search button click
     */
    @FXML
    private void handleSearch() {
        refreshData();
    }
    
    /**
     * Handle reset search button click
     */
    @FXML
    private void handleResetSearch() {
        searchField.clear();
        filterComboBox.setValue("All");
        sortComboBox.setValue("Name (A-Z)");
        refreshData();
    }
    
    /**
     * Handle add patient button click
     */
    @FXML
    private void handleAddPatient() {
        openPatientForm(null, PatientFormController.FormMode.ADD, patient -> refreshData());
    }
    
    /**
     * Handle edit patient
     */
    private void handleEditPatient(Patient patient) {
        openPatientForm(patient, PatientFormController.FormMode.EDIT, p -> refreshData());
    }
    
    /**
     * Handle view patient details
     */
    private void handleViewPatient(Patient patient) {
        try {
            // Load the new simplified patient details view instead of the problematic one
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/view/SimplePatientDetailsView.fxml"));
            Parent root = loader.load();
            
            SimplePatientDetailsController controller = loader.getController();
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
     * View medical records for a specific patient
     * 
     * @param patient The patient to view medical records for
     */
    private void viewMedicalRecords(Patient patient) {
        try {
            // Navigate to medical records and filter by patient
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/view/MedicalRecordsView.fxml"));
            Parent root = loader.load();
            
            MedicalRecordsController controller = loader.getController();
            // Filter by the selected patient
            controller.filterByPatient(patient);
            
            Stage stage = new Stage();
            stage.setTitle("Medical Records - " + patient.getFirstName() + " " + patient.getLastName());
            stage.setScene(new Scene(root));
            stage.setMinWidth(900);
            stage.setMinHeight(700);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open medical records view.\nError: " + e.getMessage());
        }
    }
    
    /**
     * Handle delete patient
     */
    private void handleDeletePatient(Patient patient) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Patient");
        confirmation.setHeaderText("Delete Patient Record");
        confirmation.setContentText("Are you sure you want to delete " + patient.getFirstName() + " " + 
            patient.getLastName() + "?\nThis action cannot be undone.");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = patientDAO.deletePatient(patient.getPatientId());
            if (success) {
                refreshData();
                showSuccessAlert("Success", "Patient deleted successfully");
            } else {
                showErrorAlert("Error", "Failed to delete patient");
            }
        }
    }
    
    /**
     * Handle import patients
     */
    @FXML
    private void handleImportPatients() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Patients");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        
        File selectedFile = fileChooser.showOpenDialog(searchField.getScene().getWindow());
        if (selectedFile != null) {
            // This would normally call a service to import the file
            // For now, just show a success message
            showSuccessAlert("Import Started", 
                "Import of patient data from " + selectedFile.getName() + " has been initiated.");
            
            // Refresh after a short delay to simulate completion
            // In a real implementation, this would be called after the import completes
            refreshData();
        }
    }
    
    /**
     * Handle export patients
     */
    @FXML
    private void handleExportPatients() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Patients");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"),
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        
        File selectedFile = fileChooser.showSaveDialog(searchField.getScene().getWindow());
        if (selectedFile != null) {
            // This would normally call a service to export the data
            // For now, just show a success message
            showSuccessAlert("Export Started", 
                "Export of " + filteredPatientsList.size() + " patient records to " + 
                selectedFile.getName() + " has been initiated.");
        }
    }
    
    /**
     * Open patient form for adding or editing
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
     * Show success alert
     */
    private void showSuccessAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Show error alert
     */
    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
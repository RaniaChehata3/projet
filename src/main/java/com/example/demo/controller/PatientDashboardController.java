package com.example.demo.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the Patient Dashboard View
 */
public class PatientDashboardController extends ModernDashboardController {

    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label nextAppointmentLabel;
    
    @FXML
    private Label nextAppointmentTimeLabel;
    
    @FXML
    private Label prescriptionRefillsLabel;
    
    @FXML
    private Label newTestResultsLabel;
    
    @FXML
    private TableView<Appointment> appointmentsTable;
    
    @FXML
    private TableColumn<Appointment, String> appointmentDateColumn;
    
    @FXML
    private TableColumn<Appointment, String> appointmentTimeColumn;
    
    @FXML
    private TableColumn<Appointment, String> appointmentDoctorColumn;
    
    @FXML
    private TableColumn<Appointment, String> appointmentPurposeColumn;
    
    @FXML
    private TableColumn<Appointment, Button> appointmentActionsColumn;
    
    @FXML
    private LineChart<String, Number> weightChart;
    
    @FXML
    private LineChart<String, Number> bpChart;
    
    @FXML
    private TableView<Medication> medicationsTable;
    
    @FXML
    private TableColumn<Medication, String> medicationNameColumn;
    
    @FXML
    private TableColumn<Medication, String> medicationDosageColumn;
    
    @FXML
    private TableColumn<Medication, String> medicationFrequencyColumn;
    
    @FXML
    private TableColumn<Medication, String> medicationStartColumn;
    
    @FXML
    private TableColumn<Medication, String> medicationRefillColumn;

    private ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();
    private ObservableList<Medication> medicationsList = FXCollections.observableArrayList();
    
    @Override
    public void initialize() {
        super.initialize();
        
        // Initialize tables
        initializeTables();
        
        // Load charts data
        initializeCharts();
        
        // Update welcome message with patient's name
        if (currentUser != null) {
            welcomeLabel.setText("Welcome back, " + currentUser.getFullName().split(" ")[0]);
        }
    }
    
    @Override
    protected void setupRoleSpecificUI() {
        // Set up navigation menu items specific to Patient role
        ObservableList<String> menuItems = FXCollections.observableArrayList(
            "Dashboard",
            "Appointments",
            "Medical Records",
            "Medications",
            "Test Results",
            "Messages",
            "Billing"
        );
        
        navigationList.setItems(menuItems);
        navigationList.getSelectionModel().select(0);
        
        // Set up navigation map for patient-specific views
        navigationMap.put("Dashboard", "/com/example/demo/view/PatientDashboardView.fxml");
        navigationMap.put("Appointments", "/com/example/demo/view/PatientAppointmentsView.fxml");
        navigationMap.put("Medical Records", "/com/example/demo/view/PatientRecordsView.fxml");
        navigationMap.put("Medications", "/com/example/demo/view/PatientMedicationsView.fxml");
        navigationMap.put("Test Results", "/com/example/demo/view/PatientTestResultsView.fxml");
        navigationMap.put("Messages", "/com/example/demo/view/PatientMessagesView.fxml");
        navigationMap.put("Billing", "/com/example/demo/view/PatientBillingView.fxml");
    }
    
    /**
     * Initialize the tables in the patient dashboard
     */
    private void initializeTables() {
        // Initialize appointments table
        appointmentDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate()));
        appointmentTimeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTime()));
        appointmentDoctorColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDoctor()));
        appointmentPurposeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPurpose()));
        appointmentActionsColumn.setCellFactory(col -> {
            TableCell<Appointment, Button> cell = new TableCell<>() {
                private final Button viewButton = new Button("View");
                
                {
                    viewButton.getStyleClass().add("action-button");
                    viewButton.setOnAction(event -> {
                        Appointment appointment = getTableView().getItems().get(getIndex());
                        handleViewAppointmentDetails(appointment);
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
        
        // Load sample appointment data
        loadSampleAppointments();
        appointmentsTable.setItems(appointmentsList);
        
        // Initialize medications table
        medicationNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
        medicationDosageColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDosage()));
        medicationFrequencyColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFrequency()));
        medicationStartColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStartDate()));
        medicationRefillColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRefillDate()));
        
        // Load sample medications data
        loadSampleMedications();
        medicationsTable.setItems(medicationsList);
    }
    
    /**
     * Initialize charts with sample data
     */
    private void initializeCharts() {
        // Weight chart
        XYChart.Series<String, Number> weightSeries = new XYChart.Series<>();
        weightSeries.setName("Weight (kg)");
        weightSeries.getData().add(new XYChart.Data<>("Jan", 75));
        weightSeries.getData().add(new XYChart.Data<>("Feb", 74.5));
        weightSeries.getData().add(new XYChart.Data<>("Mar", 73.8));
        weightSeries.getData().add(new XYChart.Data<>("Apr", 73.2));
        weightSeries.getData().add(new XYChart.Data<>("May", 72.9));
        weightSeries.getData().add(new XYChart.Data<>("Jun", 72.5));
        
        weightChart.getData().add(weightSeries);
        
        // Blood pressure chart
        XYChart.Series<String, Number> systolicSeries = new XYChart.Series<>();
        systolicSeries.setName("Systolic");
        systolicSeries.getData().add(new XYChart.Data<>("Jan", 135));
        systolicSeries.getData().add(new XYChart.Data<>("Feb", 132));
        systolicSeries.getData().add(new XYChart.Data<>("Mar", 128));
        systolicSeries.getData().add(new XYChart.Data<>("Apr", 129));
        systolicSeries.getData().add(new XYChart.Data<>("May", 126));
        systolicSeries.getData().add(new XYChart.Data<>("Jun", 122));
        
        XYChart.Series<String, Number> diastolicSeries = new XYChart.Series<>();
        diastolicSeries.setName("Diastolic");
        diastolicSeries.getData().add(new XYChart.Data<>("Jan", 87));
        diastolicSeries.getData().add(new XYChart.Data<>("Feb", 85));
        diastolicSeries.getData().add(new XYChart.Data<>("Mar", 83));
        diastolicSeries.getData().add(new XYChart.Data<>("Apr", 82));
        diastolicSeries.getData().add(new XYChart.Data<>("May", 80));
        diastolicSeries.getData().add(new XYChart.Data<>("Jun", 78));
        
        bpChart.getData().addAll(systolicSeries, diastolicSeries);
    }
    
    /**
     * Load sample appointment data
     */
    private void loadSampleAppointments() {
        appointmentsList.add(new Appointment("May 25, 2023", "10:30 AM", "Dr. John Smith", "Follow-up"));
        appointmentsList.add(new Appointment("June 10, 2023", "2:15 PM", "Dr. Sarah Johnson", "Annual Physical"));
        appointmentsList.add(new Appointment("July 5, 2023", "11:00 AM", "Dr. Michael Brown", "Blood Work"));
    }
    
    /**
     * Load sample medications data
     */
    private void loadSampleMedications() {
        medicationsList.add(new Medication("Atorvastatin", "20mg", "Once daily", "Jan 15, 2023", "Jun 15, 2023"));
        medicationsList.add(new Medication("Lisinopril", "10mg", "Once daily", "Feb 10, 2023", "Aug 10, 2023"));
        medicationsList.add(new Medication("Metformin", "500mg", "Twice daily", "Mar 5, 2023", "Jun 5, 2023"));
        medicationsList.add(new Medication("Levothyroxine", "50mcg", "Once daily", "Jan 20, 2023", "Jul 20, 2023"));
    }
    
    /**
     * Handle view appointment details action
     */
    private void handleViewAppointmentDetails(Appointment appointment) {
        showNotImplementedAlert("View Appointment Details");
    }
    
    /**
     * Handle schedule appointment button click
     */
    @FXML
    private void handleScheduleAppointment() {
        showNotImplementedAlert("Schedule Appointment");
    }
    
    /**
     * Handle message provider button click
     */
    @FXML
    private void handleMessageProvider() {
        showNotImplementedAlert("Message Provider");
    }
    
    /**
     * Handle request refill button click
     */
    @FXML
    private void handleRequestRefill() {
        showNotImplementedAlert("Request Medication Refill");
    }
    
    /**
     * Handle view medical records button click
     */
    @FXML
    private void handleViewMedicalRecords() {
        navigateTo("/com/example/demo/view/PatientRecordsView.fxml");
    }
    
    /**
     * Show an alert for not implemented features
     */
    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Not Implemented");
        alert.setHeaderText(feature + " Feature");
        alert.setContentText("The " + feature + " feature is not implemented in this preview.");
        alert.showAndWait();
    }
    
    /**
     * Inner class to represent an appointment
     */
    public static class Appointment {
        private final String date;
        private final String time;
        private final String doctor;
        private final String purpose;
        
        public Appointment(String date, String time, String doctor, String purpose) {
            this.date = date;
            this.time = time;
            this.doctor = doctor;
            this.purpose = purpose;
        }
        
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getDoctor() { return doctor; }
        public String getPurpose() { return purpose; }
    }
    
    /**
     * Inner class to represent a medication
     */
    public static class Medication {
        private final String name;
        private final String dosage;
        private final String frequency;
        private final String startDate;
        private final String refillDate;
        
        public Medication(String name, String dosage, String frequency, String startDate, String refillDate) {
            this.name = name;
            this.dosage = dosage;
            this.frequency = frequency;
            this.startDate = startDate;
            this.refillDate = refillDate;
        }
        
        public String getName() { return name; }
        public String getDosage() { return dosage; }
        public String getFrequency() { return frequency; }
        public String getStartDate() { return startDate; }
        public String getRefillDate() { return refillDate; }
    }
} 
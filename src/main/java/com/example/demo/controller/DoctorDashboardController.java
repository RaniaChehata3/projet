package com.example.demo.controller;

import com.example.demo.HelloApplication;
import com.example.demo.auth.AuthService;
import com.example.demo.model.User;
import com.example.demo.utils.NavigationUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * Controller for the Doctor Dashboard view.
 * Simplified to show a blank page with only authentication.
 */
public class DoctorDashboardController extends ModernDashboardController {

    @FXML private BorderPane rootPane;
    @FXML private Label welcomeLabel;
    @FXML private Label todayAppointmentsLabel;
    @FXML private Label patientsTodayLabel;
    @FXML private Label pendingResultsLabel;
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> appointmentTimeColumn;
    @FXML private TableColumn<Appointment, String> appointmentPatientColumn;
    @FXML private TableColumn<Appointment, String> appointmentPurposeColumn;
    @FXML private TableColumn<Appointment, String> appointmentStatusColumn;
    @FXML private TableColumn<Appointment, Button> appointmentActionsColumn;
    @FXML private PieChart demographicsChart;
    @FXML private LineChart<String, Number> visitsChart;
    @FXML private TableView<PatientNote> recentNotesTable;
    @FXML private TableColumn<PatientNote, String> noteTimeColumn;
    @FXML private TableColumn<PatientNote, String> notePatientColumn;
    @FXML private TableColumn<PatientNote, String> noteSummaryColumn;

    private Optional<User> currentUser;
    private ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();
    private ObservableList<PatientNote> recentNotesList = FXCollections.observableArrayList();

    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        super.initialize();
        
        // Initialize tables
        initializeTables();
        
        // Load charts data
        initializeCharts();
        
        // Get the currently authenticated user
        currentUser = AuthService.getInstance().getCurrentUser();

        // Set welcome message with doctor's name
        if (currentUser.isPresent()) {
            welcomeLabel.setText("Welcome back, " + currentUser.get().getFullName());
        } else {
            welcomeLabel.setText("Welcome");
        }
    }

    @Override
    protected void setupRoleSpecificUI() {
        // Set up navigation menu items specific to Doctor role
        ObservableList<String> menuItems = FXCollections.observableArrayList(
            "Dashboard",
            "My Patients",
            "Appointments",
            "Medical Records",
            "Prescriptions",
            "Lab Results",
            "Messages"
        );
        
        navigationList.setItems(menuItems);
        navigationList.getSelectionModel().select(0);
        
        // Set up navigation map for doctor-specific views
        navigationMap.put("Dashboard", "/com/example/demo/view/DoctorDashboardView.fxml");
        navigationMap.put("My Patients", "/com/example/demo/view/PatientListView.fxml");
        navigationMap.put("Appointments", "/com/example/demo/view/DoctorAppointmentsView.fxml");
        navigationMap.put("Medical Records", "/com/example/demo/view/MedicalRecordsView.fxml");
        navigationMap.put("Prescriptions", "/com/example/demo/view/PrescriptionsView.fxml");
        navigationMap.put("Lab Results", "/com/example/demo/view/LabResultsView.fxml");
        navigationMap.put("Messages", "/com/example/demo/view/MessagesView.fxml");
    }

    /**
     * Initialize the tables in the doctor dashboard
     */
    private void initializeTables() {
        // Initialize appointments table
        appointmentTimeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTime()));
        appointmentPatientColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPatient()));
        appointmentPurposeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPurpose()));
        appointmentStatusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));
        appointmentActionsColumn.setCellFactory(col -> {
            TableCell<Appointment, Button> cell = new TableCell<>() {
                private final Button viewButton = new Button("View");
                
                {
                    viewButton.getStyleClass().add("action-button");
                    viewButton.setOnAction(event -> {
                        Appointment appointment = getTableView().getItems().get(getIndex());
                        handleViewAppointment(appointment);
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
        
        // Initialize recent notes table
        noteTimeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate()));
        notePatientColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPatient()));
        noteSummaryColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSummary()));
        
        // Load sample notes data
        loadSampleNotes();
        recentNotesTable.setItems(recentNotesList);
    }
    
    /**
     * Initialize charts with sample data
     */
    private void initializeCharts() {
        // Initialize demographics chart
        ObservableList<PieChart.Data> demographicsData = FXCollections.observableArrayList(
            new PieChart.Data("Male", 45),
            new PieChart.Data("Female", 55)
        );
        demographicsChart.setData(demographicsData);
        
        // Initialize visits chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Patient Visits");
        series.getData().add(new XYChart.Data<>("Jan", 25));
        series.getData().add(new XYChart.Data<>("Feb", 30));
        series.getData().add(new XYChart.Data<>("Mar", 45));
        series.getData().add(new XYChart.Data<>("Apr", 42));
        series.getData().add(new XYChart.Data<>("May", 38));
        series.getData().add(new XYChart.Data<>("Jun", 50));
        
        visitsChart.getData().add(series);
    }
    
    /**
     * Load sample appointment data
     */
    private void loadSampleAppointments() {
        // Sample data - in a real application, this would come from a database
        appointmentsList.add(new Appointment("09:00 AM", "John Smith", "Check-up", "Confirmed"));
        appointmentsList.add(new Appointment("10:30 AM", "Emily Johnson", "Follow-up", "Confirmed"));
        appointmentsList.add(new Appointment("11:45 AM", "Michael Brown", "Consultation", "Confirmed"));
        appointmentsList.add(new Appointment("01:15 PM", "Sarah Davis", "Test Results", "Pending"));
        appointmentsList.add(new Appointment("02:30 PM", "David Wilson", "New Patient", "Confirmed"));
        appointmentsList.add(new Appointment("03:45 PM", "Lisa Thompson", "Follow-up", "Confirmed"));
        appointmentsList.add(new Appointment("04:30 PM", "Robert Martinez", "Check-up", "Pending"));
        appointmentsList.add(new Appointment("05:15 PM", "Jennifer Taylor", "Consultation", "Confirmed"));
    }
    
    /**
     * Load sample patient notes
     */
    private void loadSampleNotes() {
        // Sample data - in a real application, this would come from a database
        recentNotesList.add(new PatientNote("2023-05-15", "John Smith", "Patient reported persistent cough for 2 weeks. Prescribed antibiotics."));
        recentNotesList.add(new PatientNote("2023-05-14", "Sarah Davis", "Follow-up on previous treatment. Patient showing improvement."));
        recentNotesList.add(new PatientNote("2023-05-14", "Michael Brown", "Reviewed lab results. All values within normal range."));
        recentNotesList.add(new PatientNote("2023-05-13", "Emily Johnson", "Patient experiencing mild allergic reactions. Adjusted medication."));
        recentNotesList.add(new PatientNote("2023-05-12", "David Wilson", "Initial consultation. Patient has history of hypertension."));
    }
    
    /**
     * Handle view appointment action
     */
    private void handleViewAppointment(Appointment appointment) {
        // This would typically open a detailed view of the appointment
        showNotImplementedAlert("View Appointment Details");
    }
    
    /**
     * Handle add appointment button click
     */
    @FXML
    private void handleAddAppointment() {
        // This would open the add appointment screen
        showNotImplementedAlert("Add Appointment");
    }
    
    /**
     * Handle view full schedule button click
     */
    @FXML
    private void handleViewSchedule() {
        // Navigate to appointments view
        navigateTo("/com/example/demo/view/DoctorAppointmentsView.fxml");
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
        private final String time;
        private final String patient;
        private final String purpose;
        private final String status;
        
        public Appointment(String time, String patient, String purpose, String status) {
            this.time = time;
            this.patient = patient;
            this.purpose = purpose;
            this.status = status;
        }
        
        public String getTime() { return time; }
        public String getPatient() { return patient; }
        public String getPurpose() { return purpose; }
        public String getStatus() { return status; }
    }
    
    /**
     * Inner class to represent a patient note
     */
    public static class PatientNote {
        private final String date;
        private final String patient;
        private final String summary;
        
        public PatientNote(String date, String patient, String summary) {
            this.date = date;
            this.patient = patient;
            this.summary = summary;
        }
        
        public String getDate() { return date; }
        public String getPatient() { return patient; }
        public String getSummary() { return summary; }
    }
} 
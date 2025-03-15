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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.geometry.Pos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

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
    @FXML private Label currentDateLabel;
    @FXML private Label currentTimeLabel;
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
    @FXML private TableColumn<PatientNote, HBox> noteActionsColumn;

    private Optional<User> currentUser;
    private ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();
    private ObservableList<PatientNote> recentNotesList = FXCollections.observableArrayList();
    private Timeline clockTimeline;

    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        super.initialize();
        
        // Initialize date and time display
        initializeDateTimeDisplay();
        
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
    
    /**
     * Initialize the date and time display with auto-update
     */
    private void initializeDateTimeDisplay() {
        // Set current date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        currentDateLabel.setText(LocalDate.now().format(dateFormatter));
        
        // Set up a timeline to update the time every second
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
            currentTimeLabel.setText(LocalTime.now().format(timeFormatter));
        }));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
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
        
        // Style the status column
        appointmentStatusColumn.setCellFactory(column -> {
            return new TableCell<Appointment, String>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    
                    if (empty || status == null) {
                        setText(null);
                        setGraphic(null);
                        getStyleClass().removeAll("status-badge-completed", "status-badge-pending", "status-badge-cancelled");
                    } else {
                        setText(status);
                        getStyleClass().removeAll("status-badge-completed", "status-badge-pending", "status-badge-cancelled");
                        
                        if (status.equalsIgnoreCase("Confirmed")) {
                            getStyleClass().add("status-badge-completed");
                        } else if (status.equalsIgnoreCase("Pending")) {
                            getStyleClass().add("status-badge-pending");
                        } else if (status.equalsIgnoreCase("Cancelled")) {
                            getStyleClass().add("status-badge-cancelled");
                        }
                    }
                }
            };
        });
        
        appointmentActionsColumn.setCellFactory(col -> {
            TableCell<Appointment, Button> cell = new TableCell<>() {
                private final HBox actionsContainer = new HBox(5);
                private final Button viewButton = new Button("View");
                private final Button rescheduleButton = new Button("Reschedule");
                
                {
                    viewButton.getStyleClass().addAll("record-action-button", "view");
                    rescheduleButton.getStyleClass().addAll("record-action-button");
                    
                    viewButton.setOnAction(event -> {
                        Appointment appointment = getTableView().getItems().get(getIndex());
                        handleViewAppointment(appointment);
                    });
                    
                    rescheduleButton.setOnAction(event -> {
                        Appointment appointment = getTableView().getItems().get(getIndex());
                        handleRescheduleAppointment(appointment);
                    });
                    
                    actionsContainer.getChildren().addAll(viewButton, rescheduleButton);
                    actionsContainer.setAlignment(Pos.CENTER_LEFT);
                }
                
                @Override
                protected void updateItem(Button item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(actionsContainer);
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
        
        // Set up action column for notes
        noteActionsColumn.setCellFactory(col -> {
            TableCell<PatientNote, HBox> cell = new TableCell<>() {
                private final HBox actionsContainer = new HBox(5);
                private final Button viewButton = new Button("View");
                private final Button editButton = new Button("Edit");
                
                {
                    viewButton.getStyleClass().addAll("record-action-button", "view");
                    editButton.getStyleClass().addAll("record-action-button");
                    
                    viewButton.setOnAction(event -> {
                        PatientNote note = getTableView().getItems().get(getIndex());
                        handleViewNote(note);
                    });
                    
                    editButton.setOnAction(event -> {
                        PatientNote note = getTableView().getItems().get(getIndex());
                        handleEditNote(note);
                    });
                    
                    actionsContainer.getChildren().addAll(viewButton, editButton);
                    actionsContainer.setAlignment(Pos.CENTER_LEFT);
                }
                
                @Override
                protected void updateItem(HBox item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(actionsContainer);
                    }
                }
            };
            return cell;
        });
        
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
            new PieChart.Data("Female", 55),
            new PieChart.Data("Other", 5)
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
     * Handle reschedule appointment action
     */
    private void handleRescheduleAppointment(Appointment appointment) {
        // This would open a dialog to reschedule the appointment
        showNotImplementedAlert("Reschedule Appointment");
    }
    
    /**
     * Handle view note action
     */
    private void handleViewNote(PatientNote note) {
        // This would open a detailed view of the note
        showNotImplementedAlert("View Patient Note");
    }
    
    /**
     * Handle edit note action
     */
    private void handleEditNote(PatientNote note) {
        // This would open a dialog to edit the note
        showNotImplementedAlert("Edit Patient Note");
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
     * Handle patient lookup button click
     */
    @FXML
    private void handlePatientLookup() {
        // This would open the patient search screen
        showNotImplementedAlert("Patient Lookup");
    }
    
    /**
     * Handle add note button click
     */
    @FXML
    private void handleAddNote() {
        // This would open a dialog to add a new patient note
        showNotImplementedAlert("Add Patient Note");
    }
    
    /**
     * Handle add task button click
     */
    @FXML
    private void handleAddTask() {
        // This would open a dialog to add a new task
        showNotImplementedAlert("Add Task");
    }
    
    /**
     * Show an alert for not implemented features
     */
    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Not Implemented");
        alert.setHeaderText(feature + " Feature");
        alert.setContentText("This feature is not yet implemented in the demo.");
        alert.showAndWait();
    }
    
    /**
     * Stop any running timelines when the controller is no longer needed
     */
    public void shutdown() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
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
package com.example.demo.controller;

import com.example.demo.HelloApplication;
import com.example.demo.auth.AuthService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import com.example.demo.utils.NavigationUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML
    private ListView<String> sidebarMenu;
    
    @FXML
    private LineChart<String, Number> lineChart;
    
    @FXML
    private PieChart pieChart;
    
    @FXML
    private TableView<Appointment> appointmentsTable;
    
    @FXML
    private TableColumn<Appointment, String> patientColumn;
    
    @FXML
    private TableColumn<Appointment, LocalDate> dateColumn;
    
    @FXML
    private TableColumn<Appointment, LocalTime> timeColumn;
    
    @FXML
    private TableColumn<Appointment, String> purposeColumn;
    
    @FXML
    private TableColumn<Appointment, String> statusColumn;
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Label userRoleLabel;

    @FXML
    private StackPane loadingOverlay;

    private AuthService authService;

    @FXML
    public void initialize() {
        // Show loading overlay
        showLoading(true);
        
        // Initialize auth service
        authService = AuthService.getInstance();
        
        // Simulate loading delay
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(e -> {
            // Initialize sidebar menu items
            initializeSidebarMenu();
            
            // Initialize charts with sample data
            initializeCharts();
            
            // Initialize appointments table with sample data
            initializeAppointmentsTable();
            
            // Set user profile info if available
            authService.getCurrentUser().ifPresent(user -> {
                // userNameLabel.setText(user.getFullName());
                // userRoleLabel.setText(user.getRole().toString());
            });
            
            // Hide loading overlay
            showLoading(false);
        });
        delay.play();
    }
    
    private void initializeSidebarMenu() {
        ObservableList<String> menuItems = FXCollections.observableArrayList(
            "Dashboard",
            "Patients",
            "Appointments",
            "Doctors",
            "Laboratory",
            "Messages",
            "Reports",
            "Settings"
        );
        
        sidebarMenu.setItems(menuItems);
        
        // Select Dashboard by default
        sidebarMenu.getSelectionModel().select(0);
        
        // Add selection listener for menu items
        sidebarMenu.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Selected menu item: " + newValue);
            // In a real app, this would load different views based on selection
            // loadContent(newValue);
        });
    }
    
    private void initializeCharts() {
        // Initialize LineChart if available
        if (lineChart != null) {
            // Create series for patient appointments
            XYChart.Series<String, Number> appointmentsSeries = new XYChart.Series<>();
            appointmentsSeries.setName("Appointments");
            appointmentsSeries.getData().add(new XYChart.Data<>("Mon", 5));
            appointmentsSeries.getData().add(new XYChart.Data<>("Tue", 8));
            appointmentsSeries.getData().add(new XYChart.Data<>("Wed", 12));
            appointmentsSeries.getData().add(new XYChart.Data<>("Thu", 7));
            appointmentsSeries.getData().add(new XYChart.Data<>("Fri", 10));
            appointmentsSeries.getData().add(new XYChart.Data<>("Sat", 4));
            appointmentsSeries.getData().add(new XYChart.Data<>("Sun", 2));
            
            // Create series for lab tests
            XYChart.Series<String, Number> testsSeries = new XYChart.Series<>();
            testsSeries.setName("Lab Tests");
            testsSeries.getData().add(new XYChart.Data<>("Mon", 3));
            testsSeries.getData().add(new XYChart.Data<>("Tue", 5));
            testsSeries.getData().add(new XYChart.Data<>("Wed", 8));
            testsSeries.getData().add(new XYChart.Data<>("Thu", 4));
            testsSeries.getData().add(new XYChart.Data<>("Fri", 6));
            testsSeries.getData().add(new XYChart.Data<>("Sat", 2));
            testsSeries.getData().add(new XYChart.Data<>("Sun", 1));
            
            lineChart.getData().addAll(appointmentsSeries, testsSeries);
        }
        
        // Initialize PieChart if available
        if (pieChart != null) {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Male", 45),
                new PieChart.Data("Female", 55),
                new PieChart.Data("Other", 5)
            );
            pieChart.setData(pieChartData);
            pieChart.setTitle("Patient Demographics");
            pieChart.setLabelsVisible(true);
        }
    }
    
    private void initializeAppointmentsTable() {
        if (appointmentsTable == null) {
            return; // Table not found in FXML
        }
        
        // Set up table columns
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patient"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        purposeColumn.setCellValueFactory(new PropertyValueFactory<>("purpose"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Format date and time columns
        dateColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Appointment, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                }
            }
        });
        
        timeColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Appointment, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("h:mm a")));
                }
            }
        });
        
        // Set up status column with color indicators
        statusColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Appointment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label statusLabel = new Label(item);
                    statusLabel.getStyleClass().add("status-indicator");
                    
                    // Apply appropriate style based on status
                    switch (item.toLowerCase()) {
                        case "confirmed":
                            statusLabel.getStyleClass().add("success");
                            break;
                        case "pending":
                            statusLabel.getStyleClass().add("warning");
                            break;
                        case "rescheduled":
                        case "cancelled":
                            statusLabel.getStyleClass().add("error");
                            break;
                        default:
                            // Default styling
                            break;
                    }
                    
                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        });
        
        // Add sample data
        ObservableList<Appointment> appointments = FXCollections.observableArrayList(
            new Appointment("John Smith", LocalDate.now(), LocalTime.of(9, 0), "Annual Checkup", "Confirmed"),
            new Appointment("Sarah Johnson", LocalDate.now(), LocalTime.of(10, 30), "Blood Test Results", "Pending"),
            new Appointment("Robert Brown", LocalDate.now().plusDays(1), LocalTime.of(14, 15), "Cardiology Consultation", "Confirmed"),
            new Appointment("Maria Rodriguez", LocalDate.now().plusDays(1), LocalTime.of(16, 0), "Follow-up Visit", "Rescheduled"),
            new Appointment("David Lee", LocalDate.now().plusDays(2), LocalTime.of(11, 45), "Medication Review", "Confirmed")
        );
        
        appointmentsTable.setItems(appointments);
    }
    
    /**
     * Handle logout button click
     */
    @FXML
    public void handleLogout() {
        // Get the AuthService instance
        AuthService authService = AuthService.getInstance();
        
        // Log out the user
        authService.logout();
        
        try {
            // Navigate back to the login screen
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/com/example/demo/view/LoginView.fxml"));
            Parent loginView = loader.load();
            
            // Get current stage
            Stage stage = (Stage) appointmentsTable.getScene().getWindow();
            
            // Set up the scene
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            
            // Adjust stage size for login view
            NavigationUtil.adjustStageToScreen(stage);
            
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to navigate to login screen: " + e.getMessage());
        }
    }
    
    /**
     * Show error alert with the given title and message
     * 
     * @param title The alert title
     * @param message The alert message
     */
    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show or hide the loading overlay
     * 
     * @param show True to show loading overlay, false to hide
     */
    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(show);
            loadingOverlay.setManaged(show);
        }
    }
    
    /**
     * Model class for Appointment data
     */
    public static class Appointment {
        private final String patient;
        private final LocalDate date;
        private final LocalTime time;
        private final String purpose;
        private final String status;
        
        public Appointment(String patient, LocalDate date, LocalTime time, String purpose, String status) {
            this.patient = patient;
            this.date = date;
            this.time = time;
            this.purpose = purpose;
            this.status = status;
        }
        
        public String getPatient() { return patient; }
        public LocalDate getDate() { return date; }
        public LocalTime getTime() { return time; }
        public String getPurpose() { return purpose; }
        public String getStatus() { return status; }
    }
} 
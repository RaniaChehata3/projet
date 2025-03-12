package com.example.demo.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the Laboratory Dashboard View
 */
public class LabDashboardController extends ModernDashboardController {

    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label pendingTestsLabel;
    
    @FXML
    private Label completedTodayLabel;
    
    @FXML
    private Label urgentTestsLabel;
    
    @FXML
    private TableView<LabTest> pendingTestsTable;
    
    @FXML
    private TableColumn<LabTest, String> testIdColumn;
    
    @FXML
    private TableColumn<LabTest, String> testDateColumn;
    
    @FXML
    private TableColumn<LabTest, String> testPatientColumn;
    
    @FXML
    private TableColumn<LabTest, String> testTypeColumn;
    
    @FXML
    private TableColumn<LabTest, String> testOrderedByColumn;
    
    @FXML
    private TableColumn<LabTest, String> testPriorityColumn;
    
    @FXML
    private TableColumn<LabTest, Button> testActionsColumn;
    
    @FXML
    private BarChart<String, Number> testActivityChart;

    private ObservableList<LabTest> pendingTestsList = FXCollections.observableArrayList();
    
    @Override
    public void initialize() {
        super.initialize();
        
        // Initialize tables
        initializeTestsTable();
        
        // Load charts data
        initializeCharts();
        
        // Update welcome message with lab technician's name
        if (currentUser != null) {
            welcomeLabel.setText("Welcome back, " + currentUser.getFullName());
        }
    }
    
    @Override
    protected void setupRoleSpecificUI() {
        // Set up navigation menu items specific to Laboratory role
        ObservableList<String> menuItems = FXCollections.observableArrayList(
            "Dashboard",
            "Test Processing",
            "Test Results",
            "Sample Management",
            "Inventory",
            "Reports",
            "Messages"
        );
        
        navigationList.setItems(menuItems);
        navigationList.getSelectionModel().select(0);
        
        // Set up navigation map for laboratory-specific views
        navigationMap.put("Dashboard", "/com/example/demo/view/LabDashboardView.fxml");
        navigationMap.put("Test Processing", "/com/example/demo/view/TestProcessingView.fxml");
        navigationMap.put("Test Results", "/com/example/demo/view/TestResultsView.fxml");
        navigationMap.put("Sample Management", "/com/example/demo/view/SampleManagementView.fxml");
        navigationMap.put("Inventory", "/com/example/demo/view/LabInventoryView.fxml");
        navigationMap.put("Reports", "/com/example/demo/view/LabReportsView.fxml");
        navigationMap.put("Messages", "/com/example/demo/view/LabMessagesView.fxml");
    }
    
    /**
     * Initialize the tests table in the lab dashboard
     */
    private void initializeTestsTable() {
        // Initialize table columns
        testIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTestId()));
        testDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate()));
        testPatientColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPatient()));
        testTypeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTestType()));
        testOrderedByColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getOrderedBy()));
        testPriorityColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPriority()));
        
        // Configure actions column with a button
        testActionsColumn.setCellFactory(col -> {
            TableCell<LabTest, Button> cell = new TableCell<>() {
                private final Button processButton = new Button("Process");
                
                {
                    processButton.getStyleClass().add("action-button");
                    processButton.setOnAction(event -> {
                        LabTest test = getTableView().getItems().get(getIndex());
                        handleProcessTestAction(test);
                    });
                }
                
                @Override
                protected void updateItem(Button item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(processButton);
                    }
                }
            };
            return cell;
        });
        
        // Load sample test data
        loadSampleTests();
        pendingTestsTable.setItems(pendingTestsList);
    }
    
    /**
     * Initialize charts with sample data
     */
    private void initializeCharts() {
        // Create series for completed tests by day of week
        XYChart.Series<String, Number> completedSeries = new XYChart.Series<>();
        completedSeries.setName("Completed Tests");
        completedSeries.getData().add(new XYChart.Data<>("Mon", 14));
        completedSeries.getData().add(new XYChart.Data<>("Tue", 18));
        completedSeries.getData().add(new XYChart.Data<>("Wed", 21));
        completedSeries.getData().add(new XYChart.Data<>("Thu", 12));
        completedSeries.getData().add(new XYChart.Data<>("Fri", 15));
        completedSeries.getData().add(new XYChart.Data<>("Sat", 6));
        completedSeries.getData().add(new XYChart.Data<>("Sun", 3));
        
        // Create series for new tests by day of week
        XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
        newSeries.setName("New Tests");
        newSeries.getData().add(new XYChart.Data<>("Mon", 12));
        newSeries.getData().add(new XYChart.Data<>("Tue", 17));
        newSeries.getData().add(new XYChart.Data<>("Wed", 24));
        newSeries.getData().add(new XYChart.Data<>("Thu", 14));
        newSeries.getData().add(new XYChart.Data<>("Fri", 19));
        newSeries.getData().add(new XYChart.Data<>("Sat", 8));
        newSeries.getData().add(new XYChart.Data<>("Sun", 5));
        
        testActivityChart.getData().addAll(completedSeries, newSeries);
    }
    
    /**
     * Load sample test data
     */
    private void loadSampleTests() {
        // Sample data - in a real app, this would come from a database
        pendingTestsList.add(new LabTest("T-10045", "2023-05-20", "John Smith", "Complete Blood Count", "Dr. Jones", "Normal"));
        pendingTestsList.add(new LabTest("T-10046", "2023-05-20", "Sarah Davis", "Lipid Panel", "Dr. Smith", "Normal"));
        pendingTestsList.add(new LabTest("T-10047", "2023-05-20", "Michael Johnson", "Liver Function", "Dr. Wilson", "Urgent"));
        pendingTestsList.add(new LabTest("T-10048", "2023-05-20", "James Brown", "HbA1c", "Dr. Thompson", "Normal"));
        pendingTestsList.add(new LabTest("T-10049", "2023-05-20", "Lisa Garcia", "Thyroid Panel", "Dr. Moore", "Normal"));
        pendingTestsList.add(new LabTest("T-10050", "2023-05-20", "Robert Martinez", "Vitamin D", "Dr. Jones", "Normal"));
        pendingTestsList.add(new LabTest("T-10051", "2023-05-20", "Emily Taylor", "Kidney Function", "Dr. Smith", "Urgent"));
        pendingTestsList.add(new LabTest("T-10052", "2023-05-20", "David Wilson", "Electrolytes", "Dr. Wilson", "Normal"));
    }
    
    /**
     * Handle processing a specific test
     */
    private void handleProcessTestAction(LabTest test) {
        showNotImplementedAlert("Process Test");
    }
    
    /**
     * Handle process test button click
     */
    @FXML
    private void handleProcessTest() {
        // This would open the test processing screen
        navigateTo("/com/example/demo/view/TestProcessingView.fxml");
    }
    
    /**
     * Handle view all tests button click
     */
    @FXML
    private void handleViewAllTests() {
        // Navigate to test results view
        navigateTo("/com/example/demo/view/TestResultsView.fxml");
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
     * Inner class to represent a lab test
     */
    public static class LabTest {
        private final String testId;
        private final String date;
        private final String patient;
        private final String testType;
        private final String orderedBy;
        private final String priority;
        
        public LabTest(String testId, String date, String patient, String testType, String orderedBy, String priority) {
            this.testId = testId;
            this.date = date;
            this.patient = patient;
            this.testType = testType;
            this.orderedBy = orderedBy;
            this.priority = priority;
        }
        
        public String getTestId() { return testId; }
        public String getDate() { return date; }
        public String getPatient() { return patient; }
        public String getTestType() { return testType; }
        public String getOrderedBy() { return orderedBy; }
        public String getPriority() { return priority; }
    }
} 
package com.example.demo.controller;

import com.example.demo.HelloApplication;
import com.example.demo.auth.AuthService;
import com.example.demo.database.MedicalRecordDAO;
import com.example.demo.database.PatientDAO;
import com.example.demo.database.UserDAO;
import com.example.demo.model.Patient;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.utils.NavigationUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.demo.util.RecordLinkApiClient;

// Added imports
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import org.json.JSONObject;
import java.awt.Desktop;
import java.net.URI;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.util.Duration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Add these imports at the top of the file after other imports
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import java.util.Calendar;

/**
 * Enhanced controller for the Medical Records view
 */
public class MedicalRecordsController {

    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> recordTypeFilter;
    
    @FXML
    private ComboBox<String> statusFilter;
    
    @FXML
    private DatePicker dateFilter;
    
    @FXML
    private Button clearFiltersButton;
    
    @FXML
    private Button exportButton;
    
    @FXML
    private Button addRecordButton;
    
    @FXML
    private TabPane recordsTabPane;
    
    @FXML
    private TableView<MedicalRecord> recordsTable;
    
    @FXML
    private TableView<MedicalRecord> labResultsTable;
    
    @FXML
    private TableView<MedicalRecord> consultationsTable;
    
    @FXML
    private TableView<MedicalRecord> prescriptionsTable;
    
    @FXML
    private TableView<MedicalRecord> immunizationsTable;
    
    @FXML
    private TableColumn<MedicalRecord, String> idColumn;
    
    @FXML
    private TableColumn<MedicalRecord, String> patientColumn;
    
    @FXML
    private TableColumn<MedicalRecord, String> dateColumn;
    
    @FXML
    private TableColumn<MedicalRecord, String> typeColumn;
    
    @FXML
    private TableColumn<MedicalRecord, String> doctorColumn;
    
    @FXML
    private TableColumn<MedicalRecord, String> notesColumn;
    
    @FXML
    private TableColumn<MedicalRecord, String> statusColumn;
    
    @FXML
    private TableColumn<MedicalRecord, Void> actionsColumn;
    
    @FXML
    private Label totalRecordsLabel;
    
    @FXML
    private Label totalRecordsCount;
    
    @FXML
    private Label pendingRecordsCount;
    
    @FXML
    private Label recentRecordsCount;
    
    @FXML
    private Pagination recordsPagination;
    
    private ObservableList<MedicalRecord> allRecordsList = FXCollections.observableArrayList();
    private FilteredList<MedicalRecord> filteredRecords;
    
    // Maps to store type-specific records
    private Map<String, ObservableList<MedicalRecord>> recordsByType = new HashMap<>();
    
    // Date formatter for consistent date formatting
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Add DAO for database operations
    private MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO();
    private PatientDAO patientDAO = PatientDAO.getInstance();
    private UserDAO userDAO = new UserDAO();
    
    // Observable lists for patients and doctors
    private ObservableList<PatientItem> patientsList = FXCollections.observableArrayList();
    private ObservableList<DoctorItem> doctorsList = FXCollections.observableArrayList();
    
    @FXML
    private ToggleButton showMyPatientsOnlyButton;
    
    private boolean showMyPatientsOnly = false;
    private int currentDoctorId = -1;
    
    /**
     * Verify database connection and display status
     * Called during initialization
     */
    private void verifyDatabaseConnection() {
        boolean isConnected = false;
        try {
            // Try to get a connection
            java.sql.Connection conn = com.example.demo.database.DatabaseConnection.getConnection();
            if (conn != null) {
                isConnected = true;
                conn.close();
                System.out.println("Database connection successful");
            }
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Create a final copy of the isConnected variable for use in the lambda
        final boolean connectedStatus = isConnected;
        
        // Show database connection status in the UI (could be added to a status bar or label)
        Platform.runLater(() -> {
            String status = connectedStatus ? "Connected to database" : "Not connected to database - using sample data";
            
            // Display a notification toast or status message
            if (!connectedStatus) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Database Connection");
                alert.setHeaderText("Database Connection Status");
                alert.setContentText("Could not connect to the database. Using sample data only.\n\nChanges will not be saved when you refresh the application.");
                alert.getDialogPane().getStyleClass().add("custom-alert");
                alert.show();
            } else {
                System.out.println("Connected to database successfully. Changes will be saved.");
            }
        });
    }
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Verify database connection first
        verifyDatabaseConnection();
        
        setupTableColumns(recordsTable);
        setupTableColumns(labResultsTable);
        setupTableColumns(consultationsTable);
        setupTableColumns(prescriptionsTable);
        setupTableColumns(immunizationsTable);
        
        // Load patients and doctors for dropdowns
        loadPatients();
        loadDoctors();
        
        // Get current user information
        getCurrentUserInfo();
        
        // Initialize filter controls
        initializeFilterControls();
        
        // Load records from database with initial filters
        refreshRecords();
        
        // Set up filtered records
        filteredRecords = new FilteredList<>(allRecordsList, p -> true);
        recordsTable.setItems(filteredRecords);
        
        // Setup tables and UI
        setupTypeSpecificTables();
        setupSearchAndFilters();
        setupActionButtons();
        addTableRowTooltips(recordsTable);
        
        // Initialize pagination
        initializePagination();
        
        // Removed the welcome dialog popup
    }
    
    /**
     * Initialize filter controls with appropriate values
     */
    private void initializeFilterControls() {
        // Initialize record type filter
        recordTypeFilter.getItems().clear();
        recordTypeFilter.getItems().addAll(
            "All Records",
            "Recent Records",
            "Consultation",
            "Lab Results",
            "Prescription",
            "Immunization"
        );
        recordTypeFilter.getSelectionModel().selectFirst();
        
        // Initialize status filter
        statusFilter.getItems().clear();
        statusFilter.getItems().addAll(
            "All Statuses",
            "Pending",
            "Completed",
            "Cancelled"
        );
        statusFilter.getSelectionModel().selectFirst();
        
        // Initialize date picker with default formatting
        dateFilter.setValue(null);
        dateFilter.setPromptText("Select date");
        
        // Create "Show My Patients Only" toggle button if it doesn't exist
        if (showMyPatientsOnlyButton == null && currentDoctorId > 0) {
            showMyPatientsOnlyButton = new ToggleButton("Show My Patients Only");
            showMyPatientsOnlyButton.setOnAction(e -> {
                showMyPatientsOnly = showMyPatientsOnlyButton.isSelected();
                refreshRecords();
            });
            
            // Add to the filter tools container if possible
            if (clearFiltersButton != null && clearFiltersButton.getParent() instanceof HBox) {
                HBox filterBox = (HBox) clearFiltersButton.getParent();
                filterBox.getChildren().add(showMyPatientsOnlyButton);
            }
        }
    }
    
    /**
     * Get current logged-in user information
     */
    private void getCurrentUserInfo() {
        try {
            // Hard-coding doctor ID to 2 for testing purposes
            // In a real application, you would get this from your authentication system
            this.currentDoctorId = 2; // Dr. John Smith
            System.out.println("Setting current doctor ID to: " + currentDoctorId);
        } catch (Exception e) {
            System.err.println("Error getting current user info: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Set up search field and filter listeners
     */
    private void setupSearchAndFilters() {
        // Search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFiltersToUI();
        });
        
        // Filter ComboBox listeners
        recordTypeFilter.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> applyFiltersToUI());
        
        statusFilter.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> applyFiltersToUI());
        
        // Date filter listener
        dateFilter.valueProperty().addListener(
            (observable, oldValue, newValue) -> applyFiltersToUI());
        
        // Set up clear filters button
        clearFiltersButton.setOnAction(e -> handleClearFilters());
    }
    
    /**
     * Handle clear filters button action
     */
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        recordTypeFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        dateFilter.setValue(null);
        
        // Reset "Show My Patients Only" button if it exists
        if (showMyPatientsOnlyButton != null) {
            showMyPatientsOnlyButton.setSelected(false);
            showMyPatientsOnly = false;
        }
        
        // Refresh records from database with cleared filters
        refreshRecords();
    }
    
    /**
     * Refresh records from the database with current filters
     */
    private void refreshRecords() {
            // Clear existing records
            allRecordsList.clear();
            for (Map.Entry<String, ObservableList<MedicalRecord>> entry : recordsByType.entrySet()) {
                entry.getValue().clear();
            }
            
        // Set up filtering parameters
        String patientNameFilter = searchField.getText();
        String typeFilter = getSelectedRecordType();
        String statusFilter = getSelectedStatus();
        java.sql.Date dateFilterValue = getSelectedDateSql();
        Integer doctorIdFilter = showMyPatientsOnly ? currentDoctorId : null;
        
        System.out.println("Refreshing records with filters:");
        System.out.println("- Patient name: " + (patientNameFilter == null || patientNameFilter.isEmpty() ? "None" : patientNameFilter));
        System.out.println("- Type: " + (typeFilter == null ? "None" : typeFilter));
        System.out.println("- Status: " + (statusFilter == null ? "None" : statusFilter));
        System.out.println("- Date: " + (dateFilterValue == null ? "None" : dateFilterValue));
        System.out.println("- Doctor ID: " + (doctorIdFilter == null ? "None" : doctorIdFilter));
        
        try {
            // Load records from database with filtering
            java.util.List<com.example.demo.model.MedicalRecord> dbRecords = 
                medicalRecordDAO.getFilteredMedicalRecords(
                    patientNameFilter, typeFilter, statusFilter, 
                    dateFilterValue, null, doctorIdFilter, 0, 100);
            
            System.out.println("Loaded " + dbRecords.size() + " records from database");
            
            // Convert to controller's MedicalRecord format
            for (com.example.demo.model.MedicalRecord dbRecord : dbRecords) {
                MedicalRecord controllerRecord = medicalRecordDAO.convertToControllerFormat(dbRecord);
                allRecordsList.add(controllerRecord);
                
                // Also add to type-specific collections
                String type = controllerRecord.getType();
                if (recordsByType.containsKey(type)) {
                    recordsByType.get(type).add(controllerRecord);
                }
            }
            
            // Apply UI-level filtering (for any additional filtering needs)
            applyFiltersToUI();
            
            // Update stats and counts
            updateTotalRecordsLabel();
            updateStatsDashboard();
            
        } catch (Exception e) {
            System.err.println("Error refreshing records: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to sample data in case of database error
            if (allRecordsList.isEmpty()) {
            loadSampleRecords();
            }
        }
    }
    
    /**
     * Get the currently selected record type for filtering
     */
    private String getSelectedRecordType() {
        String selectedType = recordTypeFilter.getValue();
        
        // Special handling for "All Records" and "Recent Records"
        if (selectedType == null || "All Records".equals(selectedType)) {
            return null; // No type filter
        } else if ("Recent Records".equals(selectedType)) {
            return "Recent Records"; // Special case handled in DAO
        }
        
        return selectedType;
    }
    
    /**
     * Get the currently selected status for filtering
     */
    private String getSelectedStatus() {
        String selectedStatus = statusFilter.getValue();
        
        // Special handling for "All Statuses"
        if (selectedStatus == null || "All Statuses".equals(selectedStatus)) {
            return null; // No status filter
        }
        
        return selectedStatus;
    }
    
    /**
     * Get the selected date as a SQL Date object
     */
    private java.sql.Date getSelectedDateSql() {
        LocalDate selectedDate = dateFilter.getValue();
        
        if (selectedDate == null) {
            return null;
        }
        
        return java.sql.Date.valueOf(selectedDate);
    }
    
    /**
     * Apply filters to the UI-level filtered list
     * This is for additional filtering beyond what the database query provides
     */
    private void applyFiltersToUI() {
        String searchText = searchField.getText().toLowerCase();
        String selectedType = recordTypeFilter.getValue();
        String selectedStatus = statusFilter.getValue();
        LocalDate selectedDate = dateFilter.getValue();
        
        System.out.println("Applying UI filters:");
        System.out.println("- Search text: " + (searchText.isEmpty() ? "None" : searchText));
        System.out.println("- Type filter: " + selectedType);
        System.out.println("- Status filter: " + selectedStatus);
        System.out.println("- Date filter: " + selectedDate);
        System.out.println("- Show my patients only: " + showMyPatientsOnly);
        
        int beforeFilterCount = 0;
        for (MedicalRecord record : allRecordsList) {
            beforeFilterCount++;
        }
        
        filteredRecords.setPredicate(record -> {
            // Search text filter
            boolean matchesSearch = searchText == null || searchText.isEmpty() || 
                record.getPatient().toLowerCase().contains(searchText) ||
                record.getDoctor().toLowerCase().contains(searchText) ||
                record.getType().toLowerCase().contains(searchText) ||
                record.getStatus().toLowerCase().contains(searchText) ||
                (record.getNotes() != null && record.getNotes().toLowerCase().contains(searchText)) ||
                record.getId().toLowerCase().contains(searchText);
            
            // Type filter
            boolean matchesType = true;
            if (selectedType != null && !selectedType.equals("All Records")) {
                if (selectedType.equals("Recent Records")) {
                    // Recent records are within last 7 days
                    try {
                        LocalDate recordDate = LocalDate.parse(record.getDate(), DATE_FORMATTER);
                        matchesType = ChronoUnit.DAYS.between(recordDate, LocalDate.now()) <= 7;
                    } catch (Exception e) {
                        System.err.println("Error parsing date for recent records: " + record.getDate());
                        matchesType = false;
                    }
                } else {
                    matchesType = record.getType().equals(selectedType);
                }
            }
            
            // Status filter
            boolean matchesStatus = selectedStatus == null || 
                selectedStatus.equals("All Statuses") || 
                record.getStatus().equals(selectedStatus);
            
            // Date filter
            boolean matchesDate = selectedDate == null;
            if (!matchesDate && record.getDate() != null) {
                try {
                    LocalDate recordDate = LocalDate.parse(record.getDate(), DATE_FORMATTER);
                    matchesDate = recordDate.equals(selectedDate);
                } catch (Exception e) {
                    System.err.println("Error parsing date: " + record.getDate());
                    matchesDate = false;
                }
            }
            
            boolean result = matchesSearch && matchesType && matchesStatus && matchesDate;
            
            // Debug any filtering issues for specific records - useful for troubleshooting
            if (!result && record.getId().equals("MR-1")) {
                System.out.println("Debug filter for " + record.getId() + " - " + record.getPatient() + ":");
                System.out.println("  Search match: " + matchesSearch);
                System.out.println("  Type match: " + matchesType + " (Record type: " + record.getType() + ", Filter: " + selectedType + ")");
                System.out.println("  Status match: " + matchesStatus + " (Record status: " + record.getStatus() + ", Filter: " + selectedStatus + ")");
                System.out.println("  Date match: " + matchesDate + " (Record date: " + record.getDate() + ", Filter: " + selectedDate + ")");
            }
            
            return result;
        });
        
        int afterFilterCount = 0;
        for (MedicalRecord record : filteredRecords) {
            afterFilterCount++;
        }
        
        System.out.println("Filter results: " + afterFilterCount + " of " + beforeFilterCount + " records match filters");
        
        // Update labels and stats
        updateTotalRecordsLabel();
    }
    
    /**
     * Set up columns for all tables
     */
    private void setupTableColumns(TableView<MedicalRecord> table) {
        // Create new columns for each table to avoid sharing constraints
        TableColumn<MedicalRecord, String> idCol = new TableColumn<>("Record ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<MedicalRecord, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patient"));
        
        TableColumn<MedicalRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<MedicalRecord, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        TableColumn<MedicalRecord, String> doctorCol = new TableColumn<>("Doctor");
        doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctor"));
        
        // Add Notes column for more visibility
        TableColumn<MedicalRecord, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesCol.setCellFactory(column -> {
            return new TableCell<MedicalRecord, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setTooltip(null);
                    } else {
                        // Show abbreviated text and full text on tooltip
                        String displayText = item.length() > 40 ? item.substring(0, 37) + "..." : item;
                        setText(displayText);
                        
                        // Set tooltip with full text
                        Tooltip tooltip = new Tooltip(item);
                        tooltip.setWrapText(true);
                        tooltip.setMaxWidth(400);
                        setTooltip(tooltip);
                    }
                }
            };
        });
        
        TableColumn<MedicalRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> {
            return new TableCell<MedicalRecord, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        // Create label with status badge style
                        Label statusLabel = new Label(item);
                        statusLabel.getStyleClass().add("status-badge");
                        
                        // Apply style based on status
                        switch (item) {
                            case "Completed":
                                statusLabel.getStyleClass().add("status-badge-completed");
                                break;
                            case "Pending":
                                statusLabel.getStyleClass().add("status-badge-pending");
                                break;
                            case "Expired":
                            case "Cancelled":
                                statusLabel.getStyleClass().add("status-badge-expired");
                                break;
                            default:
                                // Default styling
                                break;
                        }
                        
                        setGraphic(statusLabel);
                        setText(null);
                        setStyle("");
                    }
                }
            };
        });
        
        // Actions column with View, Edit, and Export buttons
        TableColumn<MedicalRecord, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(createActionsColumnCellFactory());
        
        // Set column widths - adjusted for better visibility
        idCol.setPrefWidth(90);
        patientCol.setPrefWidth(140);
        dateCol.setPrefWidth(90);
        typeCol.setPrefWidth(110);
        doctorCol.setPrefWidth(140);
        notesCol.setPrefWidth(200);
        statusCol.setPrefWidth(100);
        actionsCol.setPrefWidth(240); // Increased width for action buttons
        
        // Make column width adjustable
        idCol.setResizable(true);
        patientCol.setResizable(true);
        dateCol.setResizable(true);
        typeCol.setResizable(true);
        doctorCol.setResizable(true);
        notesCol.setResizable(true);
        statusCol.setResizable(true);
        actionsCol.setResizable(true);
        
        // Add columns to table
        table.getColumns().setAll(idCol, patientCol, dateCol, typeCol, doctorCol, notesCol, statusCol, actionsCol);
        
        // Add tooltips to table rows
        addTableRowTooltips(table);
    }
    
    /**
     * Add tooltips to table rows to show more detailed information
     */
    private void addTableRowTooltips(TableView<MedicalRecord> table) {
        table.setRowFactory(tv -> {
            TableRow<MedicalRecord> row = new TableRow<MedicalRecord>() {
                @Override
                protected void updateItem(MedicalRecord record, boolean empty) {
                    super.updateItem(record, empty);
                    
                    // Apply highlight styling to first 5 rows
                    if (!empty && getIndex() < 5) {
                        getStyleClass().add("highlight-row");
                    } else {
                        getStyleClass().remove("highlight-row");
                    }
                }
            };
            
            // Create tooltip with comprehensive record information
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    MedicalRecord record = row.getItem();
                    StringBuilder tooltipText = new StringBuilder();
                    tooltipText.append("Patient: ").append(record.getPatient()).append("\n");
                    tooltipText.append("Date: ").append(record.getDate()).append("\n");
                    tooltipText.append("Type: ").append(record.getType()).append("\n");
                    tooltipText.append("Doctor: ").append(record.getDoctor()).append("\n");
                    tooltipText.append("Status: ").append(record.getStatus()).append("\n\n");
                    
                    if (record.getNotes() != null && !record.getNotes().isEmpty()) {
                        tooltipText.append("Notes: ").append(record.getNotes()).append("\n\n");
                    }
                    
                    if (record.getDiagnosisCodes() != null && !record.getDiagnosisCodes().isEmpty()) {
                        tooltipText.append("Diagnosis Codes: ").append(record.getDiagnosisCodes()).append("\n");
                    }
                    
                    if (record.getAttachments() != null && !record.getAttachments().isEmpty()) {
                        tooltipText.append("Attachments: ").append(record.getAttachments());
                    }
                    
                    Tooltip tooltip = new Tooltip(tooltipText.toString());
                    tooltip.setStyle("-fx-font-size: 12px; -fx-max-width: 500px;");
                    tooltip.setWrapText(true);
                    Tooltip.install(row, tooltip);
                }
            });
            
            // Double-click to view record details
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewRecord(row.getItem());
                }
            });
            
            // Visual feedback on hover for all rows
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    if (row.getIndex() >= 5) {
                        row.setStyle("-fx-background-color: rgba(74, 107, 255, 0.08);");
                    } else {
                        row.setStyle("-fx-background-color: rgba(74, 107, 255, 0.12);");
                    }
                }
            });
            
            row.setOnMouseExited(event -> {
                row.setStyle("");
            });
            
            return row;
        });
    }
    
    /**
     * Set up type-specific tables
     */
    private void setupTypeSpecificTables() {
        setupTableColumns(labResultsTable);
        setupTableColumns(consultationsTable);
        setupTableColumns(prescriptionsTable);
        setupTableColumns(immunizationsTable);
        
        // Initialize record lists by type
        recordsByType.put("Lab Results", FXCollections.observableArrayList());
        recordsByType.put("Consultation", FXCollections.observableArrayList());
        recordsByType.put("Prescription", FXCollections.observableArrayList());
        recordsByType.put("Immunization", FXCollections.observableArrayList());
    }
    
    /**
     * Update a type-specific table with its data
     */
    private void updateTypeSpecificTable(TableView<MedicalRecord> table, String type) {
        ObservableList<MedicalRecord> typeRecords = recordsByType.get(type);
        table.setItems(typeRecords);
    }
    
    /**
     * Create a cell factory for the actions column
     */
    private Callback<TableColumn<MedicalRecord, Void>, TableCell<MedicalRecord, Void>> createActionsColumnCellFactory() {
        return column -> {
            TableCell<MedicalRecord, Void> cell = new TableCell<>() {
                private final Button viewButton = new Button("View");
                private final Button editButton = new Button("Edit");
                private final Button exportButton = new Button("Export");
                private final Button deleteButton = new Button("Delete");
                
                {
                    // Set buttons to a fixed width and smaller padding to ensure fit
                    viewButton.setPrefWidth(55);
                    editButton.setPrefWidth(55);
                    exportButton.setPrefWidth(55);
                    deleteButton.setPrefWidth(55);
                    
                    viewButton.getStyleClass().addAll("record-action-button", "view");
                    viewButton.setOnAction(event -> {
                        MedicalRecord record = getTableView().getItems().get(getIndex());
                        handleViewRecord(record);
                    });
                    
                    editButton.getStyleClass().addAll("record-action-button");
                    editButton.setOnAction(event -> {
                        MedicalRecord record = getTableView().getItems().get(getIndex());
                        handleEditRecord(record);
                    });
                    
                    exportButton.getStyleClass().addAll("record-action-button", "export");
                    exportButton.setOnAction(event -> {
                        MedicalRecord record = getTableView().getItems().get(getIndex());
                        handleExportSingleRecord(record);
                    });
                    
                    deleteButton.getStyleClass().addAll("record-action-button", "delete");
                    deleteButton.setOnAction(event -> {
                        MedicalRecord record = getTableView().getItems().get(getIndex());
                        handleDeleteRecord(record);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        // Create an HBox to hold the buttons with larger spacing
                        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5);
                        hbox.setAlignment(Pos.CENTER_LEFT);
                        hbox.getChildren().addAll(viewButton, editButton, exportButton, deleteButton);
                        setGraphic(hbox);
                    }
                }
            };
            return cell;
        };
    }
    
    /**
     * Update the total records label
     */
    private void updateTotalRecordsLabel() {
        int total = filteredRecords.size();
        totalRecordsLabel.setText(String.valueOf(total));
    }
    
    /**
     * Update the stats dashboard with current counts
     */
    private void updateStatsDashboard() {
        // Total records
        totalRecordsCount.setText(String.valueOf(allRecordsList.size()));
        
        // Count pending records
        long pendingCount = allRecordsList.stream()
            .filter(record -> "Pending".equals(record.getStatus()))
            .count();
        pendingRecordsCount.setText(String.valueOf(pendingCount));
        
        // Count recent records (last 7 days)
        LocalDate oneWeekAgo = LocalDate.now().minusDays(7);
        long recentCount = allRecordsList.stream()
            .filter(record -> {
                LocalDate recordDate = LocalDate.parse(record.getDate());
                return recordDate.isAfter(oneWeekAgo) || recordDate.isEqual(oneWeekAgo);
            })
            .count();
        recentRecordsCount.setText(String.valueOf(recentCount));
    }
    
    /**
     * Load sample medical records data
     */
    private void loadSampleRecords() {
        // Clear any existing data
        allRecordsList.clear();
        for (Map.Entry<String, ObservableList<MedicalRecord>> entry : recordsByType.entrySet()) {
            entry.getValue().clear();
        }
        
        // Create sample records - using more recent dates and more detailed information
        MedicalRecord[] records = {
            new MedicalRecord(
                "MR-10001", 
                "John Smith", 
                LocalDate.now().format(DATE_FORMATTER), 
                "Consultation", 
                "Dr. Jane Wilson", 
                "Pending", 
                "Initial consultation for persistent headaches and fatigue",
                "R51 (Headache), G93.3 (Chronic fatigue)",
                "blood_test_requisition.pdf",
                "Severe headaches daily for 2 weeks with fatigue",
                "Possible chronic fatigue syndrome, further tests needed",
                "Rest, hydration, return for blood tests in 1 week"),
                
            new MedicalRecord(
                "MR-10002", 
                "Sarah Johnson", 
                LocalDate.now().minusDays(1).format(DATE_FORMATTER), 
                "Lab Results", 
                "Dr. Michael Brown", 
                "Completed", 
                "Complete blood count and metabolic panel review",
                "Z00.00 (General adult medical exam)",
                "cbc_results.pdf, metabolic_panel.pdf",
                "Annual blood work screening",
                "All values within normal range",
                "Continue current health regimen, annual follow-up"),
                
            new MedicalRecord(
                "MR-10003", 
                "Robert Garcia", 
                LocalDate.now().minusDays(2).format(DATE_FORMATTER), 
                "Prescription", 
                "Dr. Emily Davis", 
                "Completed", 
                "Prescription renewal for hypertension medication",
                "I10 (Essential hypertension)",
                "prescription_form.pdf",
                "Continuing management of hypertension",
                "Essential hypertension, well-controlled on current medication",
                "Lisinopril 10mg daily, refill for 90 days"),
                
            new MedicalRecord(
                "MR-10004", 
                "Lisa Chen", 
                LocalDate.now().minusDays(3).format(DATE_FORMATTER), 
                "Immunization", 
                "Dr. James Smith", 
                "Completed", 
                "Seasonal flu vaccination",
                "Z23 (Encounter for immunization)",
                "immunization_record.pdf",
                "Annual flu vaccine",
                "Preventive care, no contraindications",
                "Administered quadrivalent flu vaccine, lot #FL29384"),
                
            new MedicalRecord(
                "MR-10005", 
                "David Wilson", 
                LocalDate.now().minusDays(4).format(DATE_FORMATTER), 
                "Consultation", 
                "Dr. Jane Wilson", 
                "Completed", 
                "Follow-up appointment for diabetes management",
                "E11.9 (Type 2 diabetes mellitus without complications)",
                "diabetes_management_plan.pdf",
                "3-month diabetes follow-up, reports improved energy",
                "Type 2 diabetes, improved A1C from 7.8 to 6.9",
                "Continue Metformin 500mg twice daily, dietary counseling scheduled"),
                
            new MedicalRecord(
                "MR-10006", 
                "Maria Rodriguez", 
                LocalDate.now().minusDays(5).format(DATE_FORMATTER), 
                "Lab Results", 
                "Dr. Michael Brown", 
                "Pending", 
                "Lipid panel and A1C test results pending",
                "E78.5 (Hyperlipidemia, unspecified)",
                "test_order.pdf",
                "Routine monitoring of hyperlipidemia and prediabetes",
                "Awaiting lab results",
                "Continue current medications, results review in 1 week"),
                
            new MedicalRecord(
                "MR-10007", 
                "Thomas Lee", 
                LocalDate.now().minusDays(7).format(DATE_FORMATTER), 
                "Prescription", 
                "Dr. Emily Davis", 
                "Expired", 
                "Antibiotic prescription for respiratory infection",
                "J06.9 (Acute upper respiratory infection)",
                "antibiotic_prescription.pdf",
                "Cough, sore throat, mild fever for 5 days",
                "Acute bronchitis, likely viral but cannot rule out bacterial infection",
                "Amoxicillin 500mg three times daily for 7 days"),
                
            new MedicalRecord(
                "MR-10008", 
                "Jennifer Taylor", 
                LocalDate.now().minusDays(10).format(DATE_FORMATTER), 
                "Consultation", 
                "Dr. James Smith", 
                "Completed", 
                "Annual physical examination",
                "Z00.00 (General adult medical exam)",
                "exam_results.pdf",
                "Annual wellness check, no specific concerns",
                "Overall good health, mild vitamin D deficiency noted",
                "Vitamin D supplement 2000 IU daily, continue exercise program"),
                
            new MedicalRecord(
                "MR-10009", 
                "William Johnson", 
                LocalDate.now().minusDays(12).format(DATE_FORMATTER), 
                "Lab Results", 
                "Dr. Jane Wilson", 
                "Pending", 
                "Thyroid function test results awaiting analysis",
                "E03.9 (Hypothyroidism, unspecified)",
                "",
                "Follow-up for known hypothyroidism, reports fatigue despite medication",
                "Awaiting TSH and T4 results to adjust medication",
                "Continue current levothyroxine dose until results review"),
                
            new MedicalRecord(
                "MR-10010", 
                "Emily Davis", 
                LocalDate.now().minusDays(14).format(DATE_FORMATTER), 
                "Immunization", 
                "Dr. Michael Brown", 
                "Completed", 
                "COVID-19 booster vaccination",
                "Z23 (Encounter for immunization)",
                "covid_vaccination_record.pdf",
                "COVID-19 booster administration",
                "Eligible for booster per current guidelines",
                "Administered Pfizer-BioNTech booster, lot #PB478921")
        };
        
        // Add records to the main list
        allRecordsList.addAll(records);
        
        // Organize records by type
        for (MedicalRecord record : records) {
            String type = record.getType();
            if (recordsByType.containsKey(type)) {
                recordsByType.get(type).add(record);
            }
        }
        
        // Set items for type-specific tables
        labResultsTable.setItems(recordsByType.get("Lab Results"));
        consultationsTable.setItems(recordsByType.get("Consultation"));
        prescriptionsTable.setItems(recordsByType.get("Prescription"));
        immunizationsTable.setItems(recordsByType.get("Immunization"));
    }
    
    /**
     * Handle the "New Record" button click
     */
    @FXML
    private void handleAddRecord() {
        // Open a new record dialog or view
        showRecordDialog(null, "Create New Medical Record");
    }
    
    /**
     * Handle viewing a specific medical record
     */
    private void handleViewRecord(MedicalRecord record) {
        // Show a detailed view of the record
        showRecordDialog(record, "View Medical Record");
    }
    
    /**
     * Handle editing a specific medical record
     */
    private void handleEditRecord(MedicalRecord record) {
        // Show an edit dialog for the record
        showRecordDialog(record, "Edit Medical Record");
    }
    
    /**
     * Handle exporting a single medical record
     */
    private void handleExportSingleRecord(MedicalRecord record) {
        // Use the new exportRecord method that includes web link functionality
        exportRecord(record);
    }
    
    /**
     * Show a dialog for exporting a single record
     * @deprecated Use the exportRecord method instead
     */
    @Deprecated
    private void showSingleRecordExportDialog(MedicalRecord record) {
        // This method is kept for compatibility but is no longer used
        // All functionality is now in the exportRecord method
        exportRecord(record);
    }
    
    /**
     * Show a record dialog for create/view/edit
     */
    private void showRecordDialog(MedicalRecord record, String title) {
        // Create dialog
        Dialog<MedicalRecord> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        dialog.getDialogPane().setPrefWidth(700);
        dialog.getDialogPane().setPrefHeight(600);
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        // Create tabs for different sections of the form
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Create basic info tab
        Tab basicInfoTab = new Tab("Basic Information");
        VBox basicInfoContainer = new VBox(15);
        basicInfoContainer.setPadding(new Insets(20));
        
        // Create form fields with better spacing and alignment
        GridPane basicGrid = new GridPane();
        basicGrid.setHgap(15);
        basicGrid.setVgap(15);
        basicGrid.setPadding(new Insets(10, 0, 10, 0));
        
        // Add a section header for Patient Information
        Label patientInfoHeader = new Label("Patient Information");
        patientInfoHeader.getStyleClass().add("form-section-header");
        
        // Record ID field
        Label recordIdLabel = new Label("Auto-generated");
        recordIdLabel.setStyle("-fx-font-style: italic; -fx-text-fill: -fx-text-light;");
        if (record != null) {
            recordIdLabel = new Label(record.getId());
            recordIdLabel.setStyle("-fx-font-weight: bold;");
        }
        
        // Replace patient text field with ComboBox
        ComboBox<PatientItem> patientComboBox = new ComboBox<>(patientsList);
        patientComboBox.setPrefWidth(400);
        patientComboBox.setPromptText("Type to search and select patient");
        patientComboBox.setEditable(true);
        patientComboBox.getStyleClass().add("form-field");
        
        // Create filtered list for patient search
        FilteredList<PatientItem> filteredPatients = new FilteredList<>(patientsList, p -> true);
        patientComboBox.setItems(filteredPatients);
        
        // Add search functionality directly to the ComboBox
        patientComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            final TextField editor = patientComboBox.getEditor();
            final String selected = patientComboBox.getSelectionModel().getSelectedItem() != null ? 
                    patientComboBox.getSelectionModel().getSelectedItem().toString() : "";
            
            // If the user has typed something but not selected, filter the list
            if (editor.getText() != null && !editor.getText().equals(selected)) {
                filteredPatients.setPredicate(item -> {
                    // If search field is empty, show all patients
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    
                    // Compare patient name with search text (case insensitive)
                    String lowerCaseFilter = newValue.toLowerCase();
                    return item.getName().toLowerCase().contains(lowerCaseFilter);
                });
                patientComboBox.show(); // Show dropdown with filtered results
            }
        });
        
        // Configure ComboBox to use string converter for display
        patientComboBox.setConverter(new StringConverter<PatientItem>() {
            @Override
            public String toString(PatientItem patient) {
                if (patient == null) {
                    return "";
                }
                return patient.getName();
            }
            
            @Override
            public PatientItem fromString(String string) {
                // This is called when user types in the ComboBox
                // Return the first matching patient or null
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                
                // Find first matching patient
                String lowerCaseString = string.toLowerCase();
                for (PatientItem patient : patientsList) {
                    if (patient.getName().toLowerCase().contains(lowerCaseString)) {
                        return patient;
                    }
                }
                return null;
            }
        });
        
        // Add a section header for Record Details
        Label recordDetailsHeader = new Label("Record Details");
        recordDetailsHeader.getStyleClass().add("form-section-header");
        
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.setItems(FXCollections.observableArrayList(
            "Consultation", "Lab Results", "Prescription", "Immunization"
        ));
        typeComboBox.setPrefWidth(400);
        typeComboBox.getStyleClass().add("form-field");
        
        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(400);
        datePicker.setValue(LocalDate.now());
        datePicker.getStyleClass().add("form-field");
        
        // Replace doctor text field with ComboBox
        ComboBox<DoctorItem> doctorComboBox = new ComboBox<>(doctorsList);
        doctorComboBox.setPrefWidth(400);
        doctorComboBox.setPromptText("Type to search and select doctor");
        doctorComboBox.setEditable(true);
        doctorComboBox.getStyleClass().add("form-field");
        
        // Create filtered list for doctor search
        FilteredList<DoctorItem> filteredDoctors = new FilteredList<>(doctorsList, p -> true);
        doctorComboBox.setItems(filteredDoctors);
        
        // Add search functionality directly to the ComboBox
        doctorComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            final TextField editor = doctorComboBox.getEditor();
            final String selected = doctorComboBox.getSelectionModel().getSelectedItem() != null ? 
                    doctorComboBox.getSelectionModel().getSelectedItem().toString() : "";
            
            // If the user has typed something but not selected, filter the list
            if (editor.getText() != null && !editor.getText().equals(selected)) {
                filteredDoctors.setPredicate(item -> {
                    // If search field is empty, show all doctors
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    
                    // Compare doctor name with search text (case insensitive)
                    String lowerCaseFilter = newValue.toLowerCase();
                    return item.getName().toLowerCase().contains(lowerCaseFilter);
                });
                doctorComboBox.show(); // Show dropdown with filtered results
            }
        });
        
        // Configure ComboBox to use string converter for display
        doctorComboBox.setConverter(new StringConverter<DoctorItem>() {
            @Override
            public String toString(DoctorItem doctor) {
                if (doctor == null) {
                    return "";
                }
                return doctor.getName();
            }
            
            @Override
            public DoctorItem fromString(String string) {
                // This is called when user types in the ComboBox
                // Return the first matching doctor or null
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                
                // Find first matching doctor
                String lowerCaseString = string.toLowerCase();
                for (DoctorItem doctor : doctorsList) {
                    if (doctor.getName().toLowerCase().contains(lowerCaseString)) {
                        return doctor;
                    }
                }
                return null;
            }
        });
        
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.setItems(FXCollections.observableArrayList(
            "Completed", "Pending", "Expired", "Cancelled"
        ));
        statusComboBox.setPrefWidth(400);
        statusComboBox.getStyleClass().add("form-field");
        
        // Create and style form labels
        Label idLabel = new Label("Record ID:");
        Label patientLabel = new Label("Patient:");
        Label typeLabel = new Label("Record Type:");
        Label dateLabel = new Label("Date:");
        Label doctorLabel = new Label("Attending Doctor:");
        Label statusLabel = new Label("Status:");
        
        // Validation containers
        HBox patientValidationContainer = new HBox(5);
        patientValidationContainer.setAlignment(Pos.CENTER_LEFT);
        patientValidationContainer.getChildren().add(patientComboBox);
        
        Label patientValidationMsg = new Label("Patient selection is required");
        patientValidationMsg.getStyleClass().add("validation-message");
        patientValidationMsg.setVisible(false);
        
        // Add validation to patient selection
        patientComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                patientComboBox.getStyleClass().add("invalid-field");
                patientValidationMsg.setVisible(true);
                if (!patientValidationContainer.getChildren().contains(patientValidationMsg)) {
                    patientValidationContainer.getChildren().add(patientValidationMsg);
                }
            } else {
                patientComboBox.getStyleClass().remove("invalid-field");
                patientComboBox.getStyleClass().add("valid-field");
                patientValidationMsg.setVisible(false);
                patientValidationContainer.getChildren().remove(patientValidationMsg);
            }
        });
        
        // Add elements to grid
        basicGrid.add(idLabel, 0, 0);
        basicGrid.add(recordIdLabel, 1, 0);
        
        // Add patient section with validation
        basicGrid.add(patientLabel, 0, 1);
        basicGrid.add(patientValidationContainer, 1, 1);
        
        // Add record details section
        basicGrid.add(typeLabel, 0, 2);
        basicGrid.add(typeComboBox, 1, 2);
        basicGrid.add(dateLabel, 0, 3);
        basicGrid.add(datePicker, 1, 3);
        basicGrid.add(doctorLabel, 0, 4);
        basicGrid.add(doctorComboBox, 1, 4);
        basicGrid.add(statusLabel, 0, 5);
        basicGrid.add(statusComboBox, 1, 5);
        
        // Add elements to the VBox container
        basicInfoContainer.getChildren().addAll(
            patientInfoHeader,
            new HBox(10, idLabel, recordIdLabel),
            new HBox(10, patientLabel, patientValidationContainer),
            recordDetailsHeader,
            basicGrid
        );
        
        basicInfoTab.setContent(new ScrollPane(basicInfoContainer));
        
        // Create details tab
        Tab detailsTab = new Tab("Clinical Details");
        VBox detailsContainer = new VBox(15);
        detailsContainer.setPadding(new Insets(20));
        
        // Add a section header for Clinical Information
        Label clinicalInfoHeader = new Label("Clinical Information");
        clinicalInfoHeader.getStyleClass().add("form-section-header");
        
        // Create form fields for detailed clinical information
        Label symptomsLabel = new Label("Symptoms:");
        TextArea symptomsArea = new TextArea();
        symptomsArea.setPrefHeight(100);
        symptomsArea.setPrefWidth(400);
        symptomsArea.setWrapText(true);
        symptomsArea.getStyleClass().add("form-field");
        
        Label diagnosisLabel = new Label("Diagnosis:");
        TextArea diagnosisArea = new TextArea();
        diagnosisArea.setPrefHeight(100);
        diagnosisArea.setPrefWidth(400);
        diagnosisArea.setWrapText(true);
        diagnosisArea.getStyleClass().add("form-field");
        
        Label diagnosisCodesLabel = new Label("Diagnosis Codes:");
        TextField diagnosisCodesField = new TextField();
        diagnosisCodesField.setPrefWidth(400);
        diagnosisCodesField.getStyleClass().add("form-field");
        
        Label treatmentLabel = new Label("Treatment Plan:");
        TextArea treatmentArea = new TextArea();
        treatmentArea.setPrefHeight(100);
        treatmentArea.setPrefWidth(400);
        treatmentArea.setWrapText(true);
        treatmentArea.getStyleClass().add("form-field");
        
        // Add fields to the details container
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(15);
        detailsGrid.setPadding(new Insets(10, 0, 10, 0));
        
        detailsGrid.add(symptomsLabel, 0, 0);
        detailsGrid.add(symptomsArea, 1, 0);
        detailsGrid.add(diagnosisLabel, 0, 1);
        detailsGrid.add(diagnosisArea, 1, 1);
        detailsGrid.add(diagnosisCodesLabel, 0, 2);
        detailsGrid.add(diagnosisCodesField, 1, 2);
        detailsGrid.add(treatmentLabel, 0, 3);
        detailsGrid.add(treatmentArea, 1, 3);
        
        detailsContainer.getChildren().addAll(
            clinicalInfoHeader,
            detailsGrid
        );
        
        detailsTab.setContent(new ScrollPane(detailsContainer));
        
        // Create notes and attachments tab
        Tab attachmentsTab = new Tab("Notes & Attachments");
        VBox attachmentsContainer = new VBox(15);
        attachmentsContainer.setPadding(new Insets(20));
        
        // Add a section header for Notes
        Label notesHeader = new Label("Notes");
        notesHeader.getStyleClass().add("form-section-header");
        
        Label notesLabel = new Label("Additional Notes:");
        TextArea notesArea = new TextArea();
        notesArea.setPrefHeight(100);
        notesArea.setPrefWidth(400);
        notesArea.setWrapText(true);
        notesArea.getStyleClass().add("form-field");
        
        // Add a section header for File Attachments
        Label attachmentsHeader = new Label("File Attachments");
        attachmentsHeader.getStyleClass().add("form-section-header");
        
        // Create file upload capability
        VBox fileAttachmentsContainer = new VBox(10);
        Label attachmentsLabel = new Label("Upload Files:");
        
        // File selection component
        HBox fileInputContainer = new HBox(10);
        fileInputContainer.getStyleClass().add("file-input-container");
        fileInputContainer.setAlignment(Pos.CENTER_LEFT);
        
        Label fileNameDisplay = new Label("No file selected");
        fileNameDisplay.getStyleClass().add("file-name-display");
        
        Button browseButton = new Button("Browse Files");
        browseButton.getStyleClass().add("file-input-button");
        
        // File chooser setup
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachment");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Text Documents", "*.txt", "*.doc", "*.docx")
        );
        
        // Reference to store the selected file
        final File[] selectedFile = new File[1];
        
        // Browse button handler
        browseButton.setOnAction(event -> {
            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                selectedFile[0] = file;
                fileNameDisplay.setText(file.getName());
                
                // Show a preview if it's an image
                if (file.getName().toLowerCase().endsWith(".jpg") || 
                    file.getName().toLowerCase().endsWith(".jpeg") ||
                    file.getName().toLowerCase().endsWith(".png") ||
                    file.getName().toLowerCase().endsWith(".gif")) {
                    
                    try {
                        ImageView imagePreview = new ImageView(new Image(file.toURI().toString()));
                        imagePreview.setFitHeight(150);
                        imagePreview.setFitWidth(200);
                        imagePreview.setPreserveRatio(true);
                        
                        // If a preview already exists, replace it
                        if (fileInputContainer.getChildren().size() > 3) {
                            fileInputContainer.getChildren().remove(3);
                        }
                        
                        fileInputContainer.getChildren().add(imagePreview);
                    } catch (Exception e) {
                        System.err.println("Error loading image preview: " + e.getMessage());
                    }
                }
            }
        });
        
        // Add components to containers
        fileInputContainer.getChildren().addAll(browseButton, fileNameDisplay);
        fileAttachmentsContainer.getChildren().addAll(attachmentsLabel, fileInputContainer);
        
        // Add progress indicator for file operations
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(30, 30);
        
        // Add the notes and attachments to the container
        GridPane attachmentsGrid = new GridPane();
        attachmentsGrid.setHgap(15);
        attachmentsGrid.setVgap(15);
        attachmentsGrid.setPadding(new Insets(10, 0, 10, 0));
        
        attachmentsGrid.add(notesLabel, 0, 0);
        attachmentsGrid.add(notesArea, 1, 0);
        
        attachmentsContainer.getChildren().addAll(
            notesHeader,
            attachmentsGrid,
            attachmentsHeader,
            fileAttachmentsContainer,
            progressIndicator
        );
        
        attachmentsTab.setContent(new ScrollPane(attachmentsContainer));
        
        // Add tabs to tab pane
        tabPane.getTabs().addAll(basicInfoTab, detailsTab, attachmentsTab);
        
        // Add tab pane to dialog
        dialog.getDialogPane().setContent(tabPane);
        
        // If we're editing an existing record, pre-fill the form with the record data
        if (record != null) {
            // Find matching patient and doctor
            for (PatientItem patient : patientsList) {
                if (patient.getName().equals(record.getPatient())) {
                    patientComboBox.setValue(patient);
                    break;
                }
            }
            
            for (DoctorItem doctor : doctorsList) {
                if (doctor.getName().equals(record.getDoctor())) {
                    doctorComboBox.setValue(doctor);
                    break;
                }
            }
            
            // Set other fields
            typeComboBox.setValue(record.getType());
            statusComboBox.setValue(record.getStatus());
            
            try {
                // Parse date from the record
                LocalDate date = LocalDate.parse(record.getDate(), DATE_FORMATTER);
                datePicker.setValue(date);
            } catch (Exception e) {
                // If date parsing fails, use current date
                datePicker.setValue(LocalDate.now());
            }
            
            // Set notes and other details
            notesArea.setText(record.getNotes());
            symptomsArea.setText(record.getSymptoms());
            diagnosisArea.setText(record.getDiagnosis());
            diagnosisCodesField.setText(record.getDiagnosisCodes());
            treatmentArea.setText(record.getTreatment());
            
            // If this is just a view operation, disable editing
            if (title.startsWith("View")) {
                patientComboBox.setDisable(true);
                typeComboBox.setDisable(true);
                datePicker.setDisable(true);
                doctorComboBox.setDisable(true);
                statusComboBox.setDisable(true);
                notesArea.setDisable(true);
                symptomsArea.setDisable(true);
                diagnosisArea.setDisable(true);
                diagnosisCodesField.setDisable(true);
                treatmentArea.setDisable(true);
                browseButton.setDisable(true);
                
                // Hide the Save button
                Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
                saveButton.setVisible(false);
                saveButton.setManaged(false);
            }
        }
        
        // Disable the OK button if patient is not selected
        Button okButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        okButton.setDisable(patientComboBox.getValue() == null);
        
        // Add listener to enable/disable OK button based on patient selection
        patientComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(newVal == null);
        });
        
        // Convert the result when the dialog is confirmed
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Get values from the form
                String id = (record != null) ? record.getId() : String.format("MR-%06d", (int)(Math.random() * 1000000));
                String patient = patientComboBox.getValue() != null ? patientComboBox.getValue().getName() : "";
                Integer patientId = patientComboBox.getValue() != null ? patientComboBox.getValue().getId() : null;
                String date = datePicker.getValue().format(DATE_FORMATTER);
                String type = typeComboBox.getValue();
                String doctor = doctorComboBox.getValue() != null ? doctorComboBox.getValue().getName() : "";
                Integer doctorId = doctorComboBox.getValue() != null ? doctorComboBox.getValue().getId() : null;
                String status = statusComboBox.getValue();
                String notes = notesArea.getText();
                String diagnosisCodes = diagnosisCodesField.getText();
                String symptoms = symptomsArea.getText();
                String diagnosis = diagnosisArea.getText();
                String treatment = treatmentArea.getText();
                
                // Handle file attachment
                final String[] attachmentsRef = new String[1];
                attachmentsRef[0] = "";
                if (selectedFile[0] != null) {
                    // Show progress indicator
                    progressIndicator.setVisible(true);
                    
                    // Simulate file processing (in a real app this would be actual file upload/processing)
                    Platform.runLater(() -> {
                        try {
                            // In a real app, you'd save the file to a directory or database
                            attachmentsRef[0] = selectedFile[0].getName();
                            
                            // Hide progress indicator after processing
                            progressIndicator.setVisible(false);
                } catch (Exception e) {
                            System.err.println("Error processing file: " + e.getMessage());
                            progressIndicator.setVisible(false);
                        }
                    });
                }
                
                // Create a new MedicalRecord with all the field values
                MedicalRecord newRecord = new MedicalRecord(id, patient, date, type, doctor, status, 
                        notes, diagnosisCodes, attachmentsRef[0], symptoms, diagnosis, treatment);
                
                // Set additional fields on the record
                newRecord.patientId = patientId;
                newRecord.patientName = patient;
                newRecord.doctorId = doctorId;
                newRecord.doctorName = doctor;
                
                // Return the new record
                return newRecord;
            }
            return null;
        });
        
        // Show the dialog and wait for result
        Optional<MedicalRecord> result = dialog.showAndWait();
        
        // Process the result
        result.ifPresent(newRecord -> {
            // First, convert and save to database
            boolean saveSuccess = false;
            try {
                // Convert UI record to database model
                com.example.demo.model.MedicalRecord dbRecord = medicalRecordDAO.convertToDbFormat(newRecord);
                
                System.out.println("Saving medical record to database:");
                System.out.println("- Record ID: " + dbRecord.getRecordId());
                System.out.println("- Patient: " + dbRecord.getPatientName() + " (ID: " + dbRecord.getPatientId() + ")");
                System.out.println("- Doctor: " + dbRecord.getDoctorName() + " (ID: " + dbRecord.getDoctorId() + ")");
                
                // Create new record or update existing one
                if (record == null) {
                    // This is a new record
                    saveSuccess = medicalRecordDAO.createMedicalRecord(dbRecord);
                    if (saveSuccess && dbRecord.getRecordId() > 0) {
                        // Create a new record with the updated database ID
                        String newId = "MR-" + dbRecord.getRecordId();
                        System.out.println("Record saved with database ID: " + dbRecord.getRecordId());
                        
                        // Since id is final, create a new record with the correct ID
                        MedicalRecord updatedRecord = new MedicalRecord(
                            newId,
                            newRecord.getPatient(),
                            newRecord.getDate(),
                            newRecord.getType(),
                            newRecord.getDoctor(),
                            newRecord.getStatus(),
                            newRecord.getNotes(),
                            newRecord.getDiagnosisCodes(),
                            newRecord.getAttachments(),
                            newRecord.getSymptoms(),
                            newRecord.getDiagnosis(),
                            newRecord.getTreatment()
                        );
                        
                        // Copy non-final fields
                        updatedRecord.patientId = newRecord.patientId;
                        updatedRecord.patientName = newRecord.patientName;
                        updatedRecord.doctorId = newRecord.doctorId;
                        updatedRecord.doctorName = newRecord.doctorName;
                        
                        // Use the updated record with correct ID for UI
                        newRecord = updatedRecord;
                    }
                } else {
                    // This is an update to an existing record
                    saveSuccess = medicalRecordDAO.updateMedicalRecord(dbRecord);
                    System.out.println("Record updated in database: " + saveSuccess);
                }
                
                if (!saveSuccess) {
                    showErrorAlert("Database Error", "Failed to save the medical record to the database.");
                    return; // Don't update UI if database save failed
                }
            } catch (Exception e) {
                System.err.println("Error saving to database: " + e.getMessage());
                e.printStackTrace();
                showErrorAlert("Error", "Failed to save record: " + e.getMessage());
                return; // Don't update UI if database save failed
            }
            
            // Then update UI collections
            if (record == null) {
                // This is a new record
                addRecord(newRecord);
            } else {
                // This is an edit to an existing record
                updateRecord(record, newRecord);
            }
            
            // Show success message
            if (saveSuccess) {
                showSuccessAlert("Success", "The medical record has been successfully saved.");
                
                // Refresh records from database to ensure UI is in sync
                refreshRecords();
            }
        });
    }
    
    /**
     * Add a new medical record - only updates UI
     * (Database save is now handled before this method is called)
     */
    private void addRecord(MedicalRecord record) {
        // Add to UI
        allRecordsList.add(record);
        
        // Update type-specific collections
        String type = record.getType();
        if (recordsByType.containsKey(type)) {
            recordsByType.get(type).add(record);
        } else {
            ObservableList<MedicalRecord> typeList = FXCollections.observableArrayList(record);
            recordsByType.put(type, typeList);
        }
        
        // Update UI
        updateTotalRecordsLabel();
        updateStatsDashboard();
    }
    
    /**
     * Update an existing medical record - only updates UI
     * (Database update is now handled before this method is called)
     */
    private void updateRecord(MedicalRecord oldRecord, MedicalRecord newRecord) {
        // Update in UI
        int index = allRecordsList.indexOf(oldRecord);
        if (index >= 0) {
            allRecordsList.set(index, newRecord);
        }
        
        // Update in type-specific collections
        String oldType = oldRecord.getType();
        String newType = newRecord.getType();
        
        if (oldType.equals(newType)) {
            // Same type, just update
            if (recordsByType.containsKey(oldType)) {
                ObservableList<MedicalRecord> typeList = recordsByType.get(oldType);
                int typeIndex = typeList.indexOf(oldRecord);
                if (typeIndex >= 0) {
                    typeList.set(typeIndex, newRecord);
                }
            }
        } else {
            // Type changed, remove from old and add to new
            if (recordsByType.containsKey(oldType)) {
                recordsByType.get(oldType).remove(oldRecord);
            }
            
            if (recordsByType.containsKey(newType)) {
                recordsByType.get(newType).add(newRecord);
            } else {
                ObservableList<MedicalRecord> typeList = FXCollections.observableArrayList(newRecord);
                recordsByType.put(newType, typeList);
            }
        }
        
        // Update UI
        updateStatsDashboard();
    }
    
    /**
     * Handle deleting a record
     */
    private void handleDeleteRecord(MedicalRecord record) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Medical Record");
        confirmAlert.setContentText("Are you sure you want to delete this medical record? This action cannot be undone.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.get() == ButtonType.OK) {
            // Delete from UI
            allRecordsList.remove(record);
            
            // Delete from type-specific collections
            String type = record.getType();
            if (recordsByType.containsKey(type)) {
                recordsByType.get(type).remove(record);
            }
            
            // Delete from database
            try {
                int recordId = Integer.parseInt(record.getId().replace("MR-", ""));
                medicalRecordDAO.deleteMedicalRecord(recordId);
            } catch (Exception e) {
                System.err.println("Error deleting record from database: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Update UI
            updateTotalRecordsLabel();
            updateStatsDashboard();
            
            showSuccessAlert("Record Deleted", "The medical record has been successfully deleted.");
        }
    }
    
    /**
     * Class representing a Medical Record
     */
    public static class MedicalRecord {
        private final String id;
        private final String patient;
        private final String date;
        private final String type;
        private final String doctor;
        private final String status;
        private final String notes;
        private final String diagnosisCodes;
        private final String attachments;
        private final String symptoms;
        private final String diagnosis;
        private final String treatment;
        private Integer patientId;
        private String patientName;
        private Integer doctorId;
        private String doctorName;
        private String visitType;
        private Date visitDate;
        private Date followUpDate;
        private String recordType;
        
        public MedicalRecord(String id, String patient, String date, String type, String doctor, String status) {
            this(id, patient, date, type, doctor, status, "", "", "", "", "", "");
        }
        
        public MedicalRecord(String id, String patient, String date, String type, String doctor, String status, 
                            String notes, String diagnosisCodes, String attachments) {
            this(id, patient, date, type, doctor, status, notes, diagnosisCodes, attachments, "", "", "");
        }
        
        public MedicalRecord(String id, String patient, String date, String type, String doctor, String status, 
                            String notes, String diagnosisCodes, String attachments, 
                            String symptoms, String diagnosis, String treatment) {
            this.id = id;
            this.patient = patient;
            this.date = date;
            this.type = type;
            this.doctor = doctor;
            this.status = status;
            this.notes = notes;
            this.diagnosisCodes = diagnosisCodes;
            this.attachments = attachments;
            this.symptoms = symptoms;
            this.diagnosis = diagnosis;
            this.treatment = treatment;
        }
        
        public String getId() { return id; }
        public String getPatient() { return patient; }
        public String getDate() { return date; }
        public String getType() { return type; }
        public String getDoctor() { return doctor; }
        public String getStatus() { return status; }
        public String getNotes() { return notes; }
        public String getDiagnosisCodes() { return diagnosisCodes; }
        public String getAttachments() { return attachments; }
        public String getSymptoms() { return symptoms; }
        public String getDiagnosis() { return diagnosis; }
        public String getTreatment() { return treatment; }
        public Integer getPatientId() { return patientId; }
        public String getPatientName() { return patientName; }
        public Integer getDoctorId() { return doctorId; }
        public String getDoctorName() { return doctorName; }
        public String getVisitType() { return visitType; }
        public Date getVisitDate() { return visitDate; }
        public Date getFollowUpDate() { return followUpDate; }
        public String getRecordType() { return recordType; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MedicalRecord that = (MedicalRecord) o;
            return id.equals(that.id);
        }
        
        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
    
    /**
     * Handle export records functionality (for multiple records)
     */
    @FXML
    private void handleExportRecords() {
        // Open the export dialog
        showExportDialog();
    }
    
    /**
     * Show a dialog for exporting multiple records with options
     */
    private void showExportDialog() {
        // Create a dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Export Medical Records");
        dialog.setMinWidth(550);
        dialog.setMinHeight(550);
        
        // Create layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(25));
        root.getStyleClass().add("custom-dialog");
        
        // Create title
        VBox headerContainer = new VBox(10);
        Label titleLabel = new Label("Export Medical Records");
        titleLabel.getStyleClass().add("export-dialog-title");
        
        Label subtitleLabel = new Label("Configure your export options below");
        subtitleLabel.getStyleClass().add("section-subtitle");
        
        headerContainer.getChildren().addAll(titleLabel, subtitleLabel);
        headerContainer.setPadding(new Insets(0, 0, 15, 0));
        headerContainer.setStyle("-fx-border-color: transparent transparent -fx-grey transparent; -fx-border-width: 0 0 1 0;");
        
        // Create content grid with better spacing
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox contentContainer = new VBox(20);
        contentContainer.setPadding(new Insets(15, 5, 15, 5));
        
        // Format section title
        Label formatLabel = new Label("Export Format");
        formatLabel.getStyleClass().add("form-section-header");
        
        // Format options with better styling
        ToggleGroup formatGroup = new ToggleGroup();
        RadioButton csvOption = new RadioButton("CSV Format (.csv)");
        csvOption.setToggleGroup(formatGroup);
        csvOption.setSelected(true);
        
        RadioButton pdfOption = new RadioButton("PDF Format (.pdf)");
        pdfOption.setToggleGroup(formatGroup);
        
        // Add description for PDF format
        Label pdfDescriptionLabel = new Label("PDF exports include a detailed clinical report with follow-up recommendations");
        pdfDescriptionLabel.getStyleClass().add("section-subtitle");
        pdfDescriptionLabel.setWrapText(true);
        
        VBox formatOptionsBox = new VBox(10);
        formatOptionsBox.getChildren().addAll(csvOption, pdfOption, pdfDescriptionLabel);
        formatOptionsBox.setPadding(new Insets(5, 15, 15, 15));
        
        VBox formatBox = new VBox(5, formatLabel, formatOptionsBox);
        formatBox.getStyleClass().add("export-section");
        formatBox.setStyle("-fx-background-color: -fx-white; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 2);");
        formatBox.setPadding(new Insets(15));
        
        // Filter section title
        Label filterLabel = new Label("Export Filters");
        filterLabel.getStyleClass().add("form-section-header");
        
        // Filter options
        CheckBox currentFilterOption = new CheckBox("Apply current filters");
        currentFilterOption.setSelected(true);
        
        ComboBox<String> typeFilterCombo = new ComboBox<>();
        typeFilterCombo.setItems(FXCollections.observableArrayList(
            "All Types", "Lab Results", "Consultation", "Prescription", "Immunization"
        ));
        typeFilterCombo.setValue("All Types");
        typeFilterCombo.setDisable(!currentFilterOption.isSelected());
        typeFilterCombo.setPromptText("Select type");
        typeFilterCombo.setPrefWidth(250);
        typeFilterCombo.getStyleClass().add("form-field");
        
        // Link current filter checkbox with type filter combo
        currentFilterOption.selectedProperty().addListener((obs, oldVal, newVal) -> {
            typeFilterCombo.setDisable(!newVal);
        });
        
        Label typeFilterLabel = new Label("Record Type:");
        typeFilterLabel.getStyleClass().add("form-field-label");
        
        GridPane filterGrid = new GridPane();
        filterGrid.setHgap(10);
        filterGrid.setVgap(10);
        filterGrid.setPadding(new Insets(5, 15, 15, 15));
        
        filterGrid.add(currentFilterOption, 0, 0, 2, 1);
        filterGrid.add(typeFilterLabel, 0, 1);
        filterGrid.add(typeFilterCombo, 1, 1);
        
        VBox filterBox = new VBox(5, filterLabel, filterGrid);
        filterBox.getStyleClass().add("export-section");
        filterBox.setStyle("-fx-background-color: -fx-white; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 2);");
        filterBox.setPadding(new Insets(15));
        
        // Options section title
        Label optionsLabel = new Label("Export Options");
        optionsLabel.getStyleClass().add("form-section-header");
        
        // Export options with improved layout
        CheckBox includeHeadersOption = new CheckBox("Include column headers");
        includeHeadersOption.setSelected(true);
        
        CheckBox includeMetadataOption = new CheckBox("Include report metadata");
        includeMetadataOption.setSelected(true);
        
        CheckBox includeNotesOption = new CheckBox("Include notes and attachments");
        includeNotesOption.setSelected(true);
        
        VBox optionsContent = new VBox(10);
        optionsContent.getChildren().addAll(
            includeHeadersOption,
            includeMetadataOption,
            includeNotesOption
        );
        optionsContent.setPadding(new Insets(5, 15, 15, 15));
        
        VBox optionsBox = new VBox(5, optionsLabel, optionsContent);
        optionsBox.getStyleClass().add("export-section");
        optionsBox.setStyle("-fx-background-color: -fx-white; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 2);");
        optionsBox.setPadding(new Insets(15));
        
        // Add progress indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(30, 30);
        
        // Action buttons
        Button previewButton = new Button("Preview");
        previewButton.setPrefWidth(100);
        previewButton.getStyleClass().addAll("button", "button-secondary");
        
        Button exportButton = new Button("Export");
        exportButton.setPrefWidth(100);
        exportButton.getStyleClass().addAll("button");
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(100);
        cancelButton.getStyleClass().addAll("button-outline");
        
        HBox buttonBar = new HBox(15, cancelButton, previewButton, exportButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(15, 0, 0, 0));
        buttonBar.setStyle("-fx-border-color: -fx-grey transparent transparent transparent; -fx-border-width: 1 0 0 0;");
        
        // Add a status label
        Label statusLabel = new Label("");
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);
        
        HBox statusBox = new HBox(10, progressIndicator, statusLabel);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox bottomContainer = new VBox(10, statusBox, buttonBar);
        
        // Add all components to content container
        contentContainer.getChildren().addAll(formatBox, filterBox, optionsBox);
        
        // Set up scroll pane
        scrollPane.setContent(contentContainer);
        
        // Configure BorderPane layout
        root.setTop(headerContainer);
        root.setCenter(scrollPane);
        root.setBottom(bottomContainer);
        
        // Add event handlers for buttons
        cancelButton.setOnAction(e -> dialog.close());
        
        previewButton.setOnAction(e -> {
            // Show progress indicator
            progressIndicator.setVisible(true);
            statusLabel.setVisible(true);
            statusLabel.setText("Generating preview...");
            
            // Get export options
            boolean isCsvFormat = csvOption.isSelected();
            boolean useCurrentFilters = currentFilterOption.isSelected();
            String typeFilter = typeFilterCombo.getValue();
            boolean includeHeaders = includeHeadersOption.isSelected();
            boolean includeMetadata = includeMetadataOption.isSelected();
            boolean includeNotes = includeNotesOption.isSelected();
            
            // Get records to export based on filters
            List<MedicalRecord> recordsToExport = getRecordsForExport(useCurrentFilters, typeFilter);
            
            if (recordsToExport.isEmpty()) {
                progressIndicator.setVisible(false);
                statusLabel.setText("No records match the selected filters.");
                return;
            }
            
            // Show preview dialog
            showPreviewDialog(recordsToExport, isCsvFormat, includeHeaders, includeMetadata, includeNotes);
            
            // Hide progress indicator
            progressIndicator.setVisible(false);
            statusLabel.setVisible(false);
        });
        
        exportButton.setOnAction(e -> {
            // Show progress indicator
            progressIndicator.setVisible(true);
            statusLabel.setVisible(true);
            statusLabel.setText("Preparing export...");
            
            // Get export options
            boolean isCsvFormat = csvOption.isSelected();
            boolean useCurrentFilters = currentFilterOption.isSelected();
            String typeFilter = typeFilterCombo.getValue();
            boolean includeHeaders = includeHeadersOption.isSelected();
            boolean includeMetadata = includeMetadataOption.isSelected();
            boolean includeNotes = includeNotesOption.isSelected();
            
            // Get records to export based on filters
            List<MedicalRecord> recordsToExport = getRecordsForExport(useCurrentFilters, typeFilter);
            
            if (recordsToExport.isEmpty()) {
                progressIndicator.setVisible(false);
                statusLabel.setText("No records match the selected filters.");
                return;
            }
            
            statusLabel.setText("Exporting " + recordsToExport.size() + " records...");
            
            // Configure file chooser
            FileChooser fileChooser = new FileChooser();
            if (isCsvFormat) {
                fileChooser.setTitle("Export Medical Records as CSV");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
                );
                fileChooser.setInitialFileName("medical_records_export.csv");
            } else {
                fileChooser.setTitle("Export Medical Records as PDF");
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
                );
                fileChooser.setInitialFileName("medical_records_export.pdf");
            }
            
            // Show save dialog
            File file = fileChooser.showSaveDialog(dialog.getOwner());
            
            if (file != null) {
                // Perform the export in a background thread
                Task<Boolean> exportTask = new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        try {
                            boolean success;
                if (isCsvFormat) {
                    success = exportToCsv(file, recordsToExport, includeHeaders, includeMetadata, includeNotes);
                } else {
                    success = exportToPdf(file, recordsToExport, includeHeaders, includeMetadata, includeNotes);
                }
                            return success;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                };
                
                exportTask.setOnSucceeded(event -> {
                    boolean success = exportTask.getValue();
                    progressIndicator.setVisible(false);
                    
                if (success) {
                        statusLabel.setText("Export completed successfully!");
                        
                        // Show success message and close dialog after delay
                        Platform.runLater(() -> {
                            showSuccessMessage("Export Complete", "Records Exported", 
                                    recordsToExport.size() + " records have been exported to " + file.getName());
                            
                            // Close the dialog after 2 seconds
                            PauseTransition delay = new PauseTransition(Duration.seconds(2));
                            delay.setOnFinished(e2 -> dialog.close());
                            delay.play();
                        });
                } else {
                        statusLabel.setText("Export failed. Please try again.");
                    }
                });
                
                exportTask.setOnFailed(event -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText("Export failed: " + exportTask.getException().getMessage());
                });
                
                // Execute the export task
                new Thread(exportTask).start();
            } else {
                // User cancelled the save dialog
                progressIndicator.setVisible(false);
                statusLabel.setVisible(false);
            }
        });
        
        // Create scene and set it on the dialog
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/demo/css/modern-theme.css").toExternalForm());
        
        // Set scene and show dialog
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    /**
     * Get records for export based on current filters
     * 
     * @param useCurrentFilters Whether to apply current UI filters
     * @param typeFilter Optional additional type filter
     * @return List of records for export
     */
    private List<MedicalRecord> getRecordsForExport(boolean useCurrentFilters, String typeFilter) {
        // Start with all records
        List<MedicalRecord> records = new ArrayList<>(allRecordsList);
        
        // Apply current filters if requested
        if (useCurrentFilters) {
            String searchText = searchField.getText().toLowerCase();
            String selectedType = recordTypeFilter.getValue();
            String selectedStatus = statusFilter.getValue();
            LocalDate selectedDate = dateFilter.getValue();
            
            // Apply search filter
            if (searchText != null && !searchText.isEmpty()) {
                records = records.stream()
                    .filter(record -> 
                        record.getPatient().toLowerCase().contains(searchText) ||
                        record.getDoctor().toLowerCase().contains(searchText) ||
                        record.getType().toLowerCase().contains(searchText) ||
                        record.getStatus().toLowerCase().contains(searchText) ||
                        (record.getNotes() != null && record.getNotes().toLowerCase().contains(searchText)) ||
                        record.getId().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
            }
            
            // Apply type filter
            if (selectedType != null && !selectedType.equals("All Records")) {
                if (selectedType.equals("Recent Records")) {
                    // Handle recent records (last 7 days)
                    final LocalDate now = LocalDate.now();
                records = records.stream()
                        .filter(record -> {
                            try {
                                LocalDate recordDate = LocalDate.parse(record.getDate(), DATE_FORMATTER);
                                return ChronoUnit.DAYS.between(recordDate, now) <= 7;
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
                } else {
                    // Normal type filter
                    records = records.stream()
                        .filter(record -> selectedType.equals(record.getType()))
                    .collect(Collectors.toList());
            }
        }
        
            // Apply status filter
            if (selectedStatus != null && !selectedStatus.equals("All Statuses")) {
                records = records.stream()
                    .filter(record -> selectedStatus.equals(record.getStatus()))
                    .collect(Collectors.toList());
            }
            
            // Apply date filter
            if (selectedDate != null) {
                records = records.stream()
                    .filter(record -> {
                        try {
                            LocalDate recordDate = LocalDate.parse(record.getDate(), DATE_FORMATTER);
                            return recordDate.equals(selectedDate);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            }
            
            // Apply doctor filter if "Show My Patients Only" is enabled
            if (showMyPatientsOnly && currentDoctorId > 0) {
                records = records.stream()
                    .filter(record -> {
                        Integer recordDoctorId = record.getDoctorId();
                        return recordDoctorId != null && recordDoctorId == currentDoctorId;
                    })
                    .collect(Collectors.toList());
            }
        }
        
        // Apply additional type filter if provided and not "All Records"
        if (typeFilter != null && !typeFilter.equals("All Records")) {
            records = records.stream()
                .filter(record -> typeFilter.equals(record.getType()))
                .collect(Collectors.toList());
        }
        
        return records;
    }
    
    /**
     * Export records to CSV file
     */
    private boolean exportToCsv(File file, List<MedicalRecord> records, boolean includeHeaders, 
                               boolean includeMetadata, boolean includeNotes) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            // Add metadata if requested
            if (includeMetadata) {
                writer.println("# Medical Records Export");
                writer.println("# Date: " + LocalDate.now().format(DATE_FORMATTER));
                writer.println("# Total Records: " + records.size());
                writer.println("# ");
                writer.println();
            }
            
            // Add headers if requested
            if (includeHeaders) {
                StringBuilder header = new StringBuilder();
                header.append("Record ID,Patient,Date,Type,Doctor,Status");
                
                if (includeNotes) {
                    header.append(",Notes,Diagnosis Codes,Attachments");
                }
                
                writer.println(header.toString());
            }
            
            // Add record data
            for (MedicalRecord record : records) {
                StringBuilder line = new StringBuilder();
                
                // Add basic fields
                line.append(csvEscape(record.getId())).append(",");
                line.append(csvEscape(record.getPatient())).append(",");
                line.append(csvEscape(record.getDate())).append(",");
                line.append(csvEscape(record.getType())).append(",");
                line.append(csvEscape(record.getDoctor())).append(",");
                line.append(csvEscape(record.getStatus()));
                
                // Add optional fields if requested
                if (includeNotes) {
                    line.append(",").append(csvEscape(record.getNotes()));
                    line.append(",").append(csvEscape(record.getDiagnosisCodes()));
                    line.append(",").append(csvEscape(record.getAttachments()));
                }
                
                writer.println(line.toString());
            }
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Escape a string for CSV output
     */
    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        
        // Escape quotes and wrap in quotes if needed
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    /**
     * Export records to PDF file using a simplified PDF format
     * Note: In a production environment, a proper PDF library like Apache PDFBox would be used
     */
    private boolean exportToPdf(File file, List<MedicalRecord> records, boolean includeHeaders, 
                               boolean includeMetadata, boolean includeNotes) {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            // Create PDF content manually (simplified version)
            // In a real application, this would use Apache PDFBox or another library
            // This implementation creates a text-based representation that looks like a PDF report
            StringBuilder pdfContent = new StringBuilder();
            
            // Header
            pdfContent.append("===========================================================\n");
            pdfContent.append("                 MEDICAL RECORDS SUMMARY                   \n");
            pdfContent.append("===========================================================\n\n");
            
            // Add metadata if requested
            if (includeMetadata) {
                pdfContent.append("Report Generated: ").append(LocalDate.now().format(DATE_FORMATTER)).append("\n");
                pdfContent.append("Total Records: ").append(records.size()).append("\n");
                pdfContent.append("Generated By: Medical Records System\n");
                pdfContent.append("Report Type: Multiple Records Summary\n\n");
                pdfContent.append("-----------------------------------------------------------\n\n");
            }
            
            // Add summary statistics
            pdfContent.append("REPORT SUMMARY\n");
            pdfContent.append("-----------------------------------------------------------\n");
            
            // Count records by type
            Map<String, Long> recordsByType = records.stream()
                .collect(Collectors.groupingBy(MedicalRecord::getType, Collectors.counting()));
            
            // Count records by status
            Map<String, Long> recordsByStatus = records.stream()
                .collect(Collectors.groupingBy(MedicalRecord::getStatus, Collectors.counting()));
            
            pdfContent.append("Records by Type:\n");
            recordsByType.forEach((type, count) -> 
                pdfContent.append(String.format("- %s: %d\n", type, count))
            );
            
            pdfContent.append("\nRecords by Status:\n");
            recordsByStatus.forEach((status, count) -> 
                pdfContent.append(String.format("- %s: %d\n", status, count))
            );
            
            pdfContent.append("\n-----------------------------------------------------------\n\n");
            
            // Add records table
            pdfContent.append("RECORDS LIST\n");
            pdfContent.append("-----------------------------------------------------------\n");
            
            // Add headers if requested
            if (includeHeaders) {
                pdfContent.append(String.format("%-10s | %-20s | %-12s | %-15s | %-20s | %-10s", 
                    "Record ID", "Patient", "Date", "Type", "Doctor", "Status"));
                
                if (includeNotes) {
                    pdfContent.append(" | Notes");
                }
                
                pdfContent.append("\n");
                pdfContent.append("----------------------------------------------------------------------");
                pdfContent.append("-----------------------------------------------------------\n");
            }
            
            // Add record data
            for (MedicalRecord record : records) {
                pdfContent.append(String.format("%-10s | %-20s | %-12s | %-15s | %-20s | %-10s", 
                    record.getId(), 
                    truncateString(record.getPatient(), 20),
                    record.getDate(),
                    truncateString(record.getType(), 15),
                    truncateString(record.getDoctor(), 20),
                    record.getStatus()
                ));
                
                if (includeNotes) {
                    pdfContent.append(" | ").append(truncateString(record.getNotes(), 30));
                }
                
                pdfContent.append("\n");
                
                // Add more detailed information if notes are included
                if (includeNotes && (record.getNotes() != null && !record.getNotes().isEmpty())) {
                    pdfContent.append("\n  Details for Record ").append(record.getId()).append(":\n");
                    pdfContent.append("  --------------------------------------------------\n");
                    pdfContent.append("  Notes: ").append(record.getNotes()).append("\n");
                    
                    if (record.getDiagnosisCodes() != null && !record.getDiagnosisCodes().isEmpty()) {
                        pdfContent.append("  Diagnosis Codes: ").append(record.getDiagnosisCodes()).append("\n");
                    }
                    
                    if (record.getAttachments() != null && !record.getAttachments().isEmpty()) {
                        pdfContent.append("  Attachments: ").append(record.getAttachments()).append("\n");
                    }
                    
                    pdfContent.append("\n");
                }
                
                pdfContent.append("-----------------------------------------------------------\n");
            }
            
            // Footer
            pdfContent.append("\n\n===========================================================\n");
            pdfContent.append("This report is confidential and contains protected health information.\n");
            pdfContent.append("Unauthorized disclosure is prohibited by law.\n");
            pdfContent.append("===========================================================\n");
            
            // Write the content to the file
            outputStream.write(pdfContent.toString().getBytes());
            
            // Show informational dialog
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PDF Export");
                alert.setHeaderText("Enhanced PDF Summary Report");
                alert.setContentText("A summary report of multiple medical records has been generated in text-based PDF format.\n\n" +
                                   "The report includes summary statistics and details for each record. In a production environment, " +
                                   "a PDF library like Apache PDFBox would be used to create a properly formatted PDF document.");
                alert.getDialogPane().getStyleClass().add("custom-alert");
                alert.showAndWait();
            });
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Helper method to truncate strings for better formatting in PDFs
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        
        if (str.length() <= maxLength) {
            return str;
        }
        
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Export a single record to a detailed PDF report
     * This creates a more comprehensive report for individual records
     */
    private boolean exportDetailedRecordToPdf(File file, MedicalRecord record, boolean includeMetadata, boolean includeNotes) {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            // Create PDF content manually (enhanced version for single record)
            StringBuilder pdfContent = new StringBuilder();
            
            // Header
            pdfContent.append("===========================================================\n");
            pdfContent.append("               MEDICAL RECORD DETAILED REPORT              \n");
            pdfContent.append("===========================================================\n\n");
            
            // Add metadata if requested
            if (includeMetadata) {
                pdfContent.append("Report Generated: ").append(LocalDate.now().format(DATE_FORMATTER)).append("\n");
                pdfContent.append("Generated By: Medical Records System\n");
                pdfContent.append("Report Type: Detailed Individual Record\n\n");
                pdfContent.append("-----------------------------------------------------------\n\n");
            }
            
            // Record summary section
            pdfContent.append("RECORD SUMMARY\n");
            pdfContent.append("-----------------------------------------------------------\n");
            pdfContent.append(String.format("Record ID:       %s\n", record.getId()));
            pdfContent.append(String.format("Patient Name:    %s\n", record.getPatient()));
            pdfContent.append(String.format("Record Date:     %s\n", record.getDate()));
            pdfContent.append(String.format("Record Type:     %s\n", record.getType()));
            pdfContent.append(String.format("Attending Doctor: %s\n", record.getDoctor()));
            pdfContent.append(String.format("Status:          %s\n\n", record.getStatus()));
            
            // Add detailed information if notes are included
            if (includeNotes) {
                pdfContent.append("DETAILED INFORMATION\n");
                pdfContent.append("-----------------------------------------------------------\n");
                
                // Add notes
                pdfContent.append("Medical Notes:\n");
                pdfContent.append(record.getNotes() != null ? record.getNotes() : "No notes recorded");
                pdfContent.append("\n\n");
                
                // Add diagnosis codes if available
                if (record.getDiagnosisCodes() != null && !record.getDiagnosisCodes().isEmpty()) {
                    pdfContent.append("Diagnosis Codes:\n");
                    pdfContent.append(record.getDiagnosisCodes());
                    pdfContent.append("\n\n");
                }
                
                // Add attachments if available
                if (record.getAttachments() != null && !record.getAttachments().isEmpty()) {
                    pdfContent.append("Attachments:\n");
                    String[] attachments = record.getAttachments().split(",");
                    for (String attachment : attachments) {
                        pdfContent.append("- ").append(attachment.trim()).append("\n");
                    }
                    pdfContent.append("\n");
                }
            }
            
            // Add follow-up information based on record type
            pdfContent.append("FOLLOW-UP INFORMATION\n");
            pdfContent.append("-----------------------------------------------------------\n");
            
            switch (record.getType()) {
                case "Lab Results":
                    pdfContent.append("Follow-up Recommendations for Lab Results:\n");
                    pdfContent.append("- Review results with primary care physician\n");
                    pdfContent.append("- Schedule follow-up appointment if indicated\n");
                    pdfContent.append("- Complete any additional tests as ordered\n");
                    break;
                    
                case "Consultation":
                    pdfContent.append("Follow-up Recommendations for Consultation:\n");
                    pdfContent.append("- Follow treatment plan as prescribed\n");
                    pdfContent.append("- Schedule follow-up appointment in 2-4 weeks\n");
                    pdfContent.append("- Contact office if symptoms worsen\n");
                    break;
                    
                case "Prescription":
                    pdfContent.append("Follow-up Recommendations for Prescription:\n");
                    pdfContent.append("- Take medication as prescribed\n");
                    pdfContent.append("- Contact office if experiencing side effects\n");
                    pdfContent.append("- Schedule follow-up before prescription expires\n");
                    break;
                    
                case "Immunization":
                    pdfContent.append("Follow-up Recommendations for Immunization:\n");
                    pdfContent.append("- Monitor for any adverse reactions\n");
                    pdfContent.append("- Schedule next immunization as recommended\n");
                    pdfContent.append("- Keep immunization record for your files\n");
                    break;
                    
                default:
                    pdfContent.append("Follow-up Recommendations:\n");
                    pdfContent.append("- Follow specific instructions provided by your healthcare provider\n");
                    pdfContent.append("- Contact office with any questions or concerns\n");
            }
            
            // Footer
            pdfContent.append("\n\n===========================================================\n");
            pdfContent.append("This report is confidential and contains protected health information.\n");
            pdfContent.append("Unauthorized disclosure is prohibited by law.\n");
            pdfContent.append("===========================================================\n");
            
            // Write the content to the file
            outputStream.write(pdfContent.toString().getBytes());
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Show a welcome dialog with tips and information about the medical records module
     */
    private void showWelcomeDialog() {
        // Create a new dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Welcome to Medical Records");
        dialog.setHeaderText("Enhanced Medical Records Management");
        
        // Create a BorderPane for the content
        BorderPane content = new BorderPane();
        content.setPadding(new Insets(20));
        
        // Create a VBox for the main content
        VBox mainContent = new VBox(15);
        
        // Add a welcome message
        Label welcomeLabel = new Label("Welcome to the enhanced Medical Records module!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Create sections for key features
        VBox featuresBox = new VBox(12);
        featuresBox.setStyle("-fx-padding: 10 0 20 0;");
        
        Label featuresLabel = new Label("New Features and Improvements:");
        featuresLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        HBox feature1 = createFeatureRow("Enhanced Dashboard", 
            "Improved statistics cards with visual indicators for record status.");
        
        HBox feature2 = createFeatureRow("Advanced Filtering", 
            "More powerful search and filtering options for finding records quickly.");
        
        HBox feature3 = createFeatureRow("Export Options", 
            "Export individual or multiple records in CSV or PDF formats.");
        
        HBox feature4 = createFeatureRow("Status Indicators", 
            "Visual status badges clearly show the state of each record.");
        
        featuresBox.getChildren().addAll(featuresLabel, feature1, feature2, feature3, feature4);
        
        // Tips section
        VBox tipsBox = new VBox(12);
        tipsBox.setStyle("-fx-padding: 10 0 0 0;");
        
        Label tipsLabel = new Label("Quick Tips:");
        tipsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        HBox tip1 = createFeatureRow("Filtering", 
            "Use the search box to find records by patient name, doctor, record type, or status.");
        
        HBox tip2 = createFeatureRow("Record Management", 
            "Create new records using the 'Add Record' card at the top of the screen.");
        
        HBox tip3 = createFeatureRow("Record Actions", 
            "View, edit, or export individual records using the action buttons in each row.");
        
        tipsBox.getChildren().addAll(tipsLabel, tip1, tip2, tip3);
        
        // Add a checkbox to not show again
        CheckBox dontShowAgain = new CheckBox("Don't show this message again");
        dontShowAgain.setStyle("-fx-padding: 20 0 0 0;");
        
        // Add everything to the main content
        mainContent.getChildren().addAll(welcomeLabel, featuresBox, tipsBox, dontShowAgain);
        
        content.setCenter(mainContent);
        dialog.getDialogPane().setContent(content);
        
        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        
        // Set min width
        dialog.getDialogPane().setMinWidth(550);
        dialog.getDialogPane().getStyleClass().add("modern-dialog");
        
        // Show the dialog
        dialog.showAndWait();
        
        // Here you would typically save the preference if "don't show again" is checked
        if (dontShowAgain.isSelected()) {
            // Save preference (not implemented in this demo)
        }
    }
    
    /**
     * Create a feature row with an icon, title, and description
     */
    private HBox createFeatureRow(String title, String description) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        
        // Icon container
        StackPane iconContainer = new StackPane();
        iconContainer.setMinSize(32, 32);
        iconContainer.setMaxSize(32, 32);
        iconContainer.getStyleClass().add("feature-icon");
        iconContainer.setStyle("-fx-background-color: rgba(74, 107, 255, 0.1); -fx-background-radius: 16;");
        
        // Feature content
        VBox content = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        
        content.getChildren().addAll(titleLabel, descLabel);
        row.getChildren().addAll(iconContainer, content);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        return row;
    }
    
    /**
     * Inner class to represent a patient in the dropdown
     */
    private static class PatientItem {
        private final int id;
        private final String name;
        
        public PatientItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    /**
     * Inner class to represent a doctor in the dropdown
     */
    private static class DoctorItem {
        private final int id;
        private final String name;
        
        public DoctorItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    /**
     * Load patients for the dropdown
     */
    private void loadPatients() {
        patientsList.clear();
        
        try {
            List<Patient> patients = patientDAO.getAllPatients();
            for (Patient patient : patients) {
                patientsList.add(new PatientItem(
                    patient.getPatientId(),
                    patient.getFirstName() + " " + patient.getLastName()
                ));
            }
        } catch (Exception e) {
            System.err.println("Error loading patients: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load doctors for the dropdown
     */
    private void loadDoctors() {
        doctorsList.clear();
        
        try {
            List<User> doctors = userDAO.getUsersByRole(UserRole.DOCTOR);
            for (User doctor : doctors) {
                doctorsList.add(new DoctorItem(
                    doctor.getUserId(),
                    doctor.getFullName()
                ));
            }
        } catch (Exception e) {
            System.err.println("Error loading doctors: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Method to export a medical record
     * 
     * @param record The record to export
     */
    private void exportRecord(MedicalRecord record) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Export Record");
        dialog.setHeaderText("Export Options");
        
        // Create a GridPane for the dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Add format options
        Label formatLabel = new Label("Export Format:");
        ToggleGroup formatGroup = new ToggleGroup();
        RadioButton pdfRadio = new RadioButton("PDF");
        RadioButton csvRadio = new RadioButton("CSV");
        RadioButton webRadio = new RadioButton("Web Link (Recommended)");
        
        pdfRadio.setToggleGroup(formatGroup);
        csvRadio.setToggleGroup(formatGroup);
        webRadio.setToggleGroup(formatGroup);
        webRadio.setSelected(true); // Make web link the default option
        
        grid.add(formatLabel, 0, 0);
        grid.add(webRadio, 1, 0);
        grid.add(pdfRadio, 1, 1);
        grid.add(csvRadio, 1, 2);
        
        // Add additional options
        Label optionsLabel = new Label("Include:");
        CheckBox includeMetadataBox = new CheckBox("Metadata");
        CheckBox includeNotesBox = new CheckBox("Notes");
        CheckBox includeHeadersBox = new CheckBox("Headers");
        
        includeMetadataBox.setSelected(true);
        includeNotesBox.setSelected(true);
        includeHeadersBox.setSelected(true);
        
        grid.add(optionsLabel, 0, 3);
        grid.add(includeMetadataBox, 1, 3);
        grid.add(includeNotesBox, 1, 4);
        grid.add(includeHeadersBox, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // Disable checkbox options when web link is selected
        webRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            boolean disableOptions = newVal;
            includeMetadataBox.setDisable(disableOptions);
            includeNotesBox.setDisable(disableOptions);
            includeHeadersBox.setDisable(disableOptions);
        });
        
        // Trigger initial state
        includeMetadataBox.setDisable(true);
        includeNotesBox.setDisable(true);
        includeHeadersBox.setDisable(true);
        
        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Apply dialog styling
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        
        // Show the dialog and wait for user input
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean isWebLink = webRadio.isSelected();
            boolean isCsvFormat = csvRadio.isSelected();
            boolean includeMetadata = includeMetadataBox.isSelected();
            boolean includeNotes = includeNotesBox.isSelected();
            boolean includeHeaders = includeHeadersBox.isSelected();
            
            if (isWebLink) {
                // Use API to generate a link and open it in the browser
                handleWebLinkGeneration(record);
            } else {
                // Use the existing file-based export
                handleFileExport(record, isCsvFormat, includeMetadata, includeNotes, includeHeaders);
            }
        }
    }
    
    /**
     * Handle the generation of a web link for a record
     * 
     * @param record The record to generate a link for
     */
    private void handleWebLinkGeneration(MedicalRecord record) {
        ProgressIndicator progressIndicator = showProgressIndicator("Generating link...");
        new Thread(() -> {
            String url = generateRecordLink(record);
            Platform.runLater(() -> {
                closeProgressIndicator(progressIndicator);
                if (url != null) {
                    boolean opened = openInBrowser(url);
                    if (opened) {
                        showSuccessMessage("Web View",
                            "The medical record has been opened in your web browser.",
                            "You can now view, print, or share this record via the web interface.");
                    } else {
                        showErrorMessage("Failed to open browser",
                            "The link was generated but could not be opened in your browser.");
                    }
                } else {
                    showErrorMessage("Link Generation Failed",
                        "Could not generate a web link for this record. Please try again or use PDF export.");
                }
            });
        }).start();
    }
    
    /**
     * Handle the file-based export of a record
     * 
     * @param record The record to export
     * @param isCsvFormat Whether to export as CSV or PDF
     * @param includeMetadata Whether to include metadata
     * @param includeNotes Whether to include notes
     * @param includeHeaders Whether to include headers
     */
    private void handleFileExport(MedicalRecord record, boolean isCsvFormat, 
                                 boolean includeMetadata, boolean includeNotes, boolean includeHeaders) {
        // Set up file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Export File");
        
        // Set extension filter based on format
        if (isCsvFormat) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("medical_record_" + record.getId() + ".csv");
        } else {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("medical_record_" + record.getId() + ".pdf");
        }
        
        // Show save dialog
        File file = fileChooser.showSaveDialog(null);
        
        if (file != null) {
            // Create a list with just this record
            List<MedicalRecord> recordList = new ArrayList<>();
            recordList.add(record);
            
            // Export the record
            boolean success = false;
            if (isCsvFormat) {
                success = exportToCsv(file, recordList, includeHeaders, includeMetadata, includeNotes);
            } else {
                success = exportDetailedRecordToPdf(file, record, includeMetadata, includeNotes);
                
                // Show informational dialog specific to detailed PDF exports
                if (success) {
                    Platform.runLater(() -> {
                        showSuccessMessage("Detailed PDF Export", "Detailed Medical Record Report",
                            "A comprehensive clinical report has been generated in text-based PDF format. " +
                            "The report includes patient information, medical details, and follow-up recommendations specific to the record type.\n\n" +
                            "In a production environment, a proper PDF library would be used to create a better formatted document.");
                    });
                }
            }
            
            // Show status message
            if (success) {
                Platform.runLater(() -> {
                    showSuccessMessage("Export Successful", null,
                        "The record was successfully exported to " + file.getAbsolutePath());
                });
            } else {
                Platform.runLater(() -> {
                    showErrorMessage("Export Failed", 
                        "Failed to export the record. Please check file permissions and try again.");
                });
            }
        }
    }
    
    /**
     * Show a progress indicator dialog
     * 
     * @param message The message to display
     * @return The progress indicator dialog
     */
    private ProgressIndicator showProgressIndicator(String message) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        progressIndicator.setMaxSize(50, 50);
        
        Label statusLabel = new Label(message);
        statusLabel.setStyle("-fx-font-size: 14px;");
        
        VBox vbox = new VBox(10, progressIndicator, statusLabel);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        
        Stage progressStage = new Stage();
        progressStage.setScene(new Scene(vbox));
        progressStage.initModality(Modality.APPLICATION_MODAL);
        progressStage.initStyle(StageStyle.UNDECORATED);
        progressStage.show();
        
        return progressIndicator;
    }
    
    /**
     * Close a progress indicator dialog
     * 
     * @param progressIndicator The progress indicator to close
     */
    private void closeProgressIndicator(ProgressIndicator progressIndicator) {
        if (progressIndicator != null) {
            Stage stage = (Stage) progressIndicator.getScene().getWindow();
            stage.close();
        }
    }
    
    /**
     * Show a success message dialog
     * 
     * @param title The dialog title
     * @param headerText The header text (can be null)
     * @param message The message to display
     */
    private void showSuccessMessage(String title, String headerText, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(message);
        
        // Apply custom styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("custom-alert");
        dialogPane.getStyleClass().add("success-alert");
        
        // Set a custom icon for success (using JavaFX unicode character)
        Label icon = new Label("✓");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: -fx-success;");
        
        // Create a container for the icon
        StackPane iconContainer = new StackPane(icon);
        iconContainer.setMinSize(64, 64);
        iconContainer.setStyle("-fx-background-radius: 32; -fx-background-color: rgba(56, 161, 105, 0.2);");
        
        // Create header with icon
        HBox header = new HBox(15, iconContainer, new Label(headerText));
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Replace the standard header with our custom one
        dialogPane.setHeader(header);
        
        // Show the dialog
        alert.showAndWait();
    }
    
    /**
     * Show an error message dialog
     * 
     * @param title The dialog title
     * @param message The message to display
     */
    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("custom-alert");
        alert.showAndWait();
    }
    
    /**
     * Show an error alert
     */
    private void showErrorAlert(String title, String content) {
        showErrorMessage(title, content);
    }
    
    /**
     * Show a success alert
     */
    private void showSuccessAlert(String title, String content) {
        showSuccessMessage(title, null, content);
    }
    
    /**
     * Show a preview dialog of the records to be exported
     */
    private void showPreviewDialog(List<MedicalRecord> records, boolean isCsvFormat, 
                                 boolean includeHeaders, boolean includeMetadata, boolean includeNotes) {
        // Create a dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Export Preview");
        dialog.setMinWidth(700);
        dialog.setMinHeight(500);
        
        // Create layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(25));
        root.getStyleClass().add("custom-dialog");
        
        // Create title
        Label titleLabel = new Label("Export Preview");
        titleLabel.getStyleClass().add("export-dialog-title");
        
        // Create format label
        Label formatLabel = new Label(String.format(
            "Preview of %d records in %s format", 
            records.size(), 
            isCsvFormat ? "CSV" : "PDF"
        ));
        formatLabel.getStyleClass().add("form-field-label");
        
        VBox headerBox = new VBox(5, titleLabel, formatLabel);
        headerBox.setPadding(new Insets(0, 0, 15, 0));
        
        // Create preview area
        TextArea previewArea = new TextArea();
        previewArea.setEditable(false);
        previewArea.setWrapText(false);
        previewArea.setPrefRowCount(20);
        previewArea.getStyleClass().add("export-preview");
        
        // Generate preview content
        StringBuilder previewContent = new StringBuilder();
        
        if (includeMetadata) {
            // Add report metadata
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = dateFormat.format(new Date());
            
            previewContent.append("Export Date: ").append(timestamp).append("\n");
            previewContent.append("Total Records: ").append(records.size()).append("\n");
            previewContent.append("Format: ").append(isCsvFormat ? "CSV" : "PDF").append("\n");
            previewContent.append("\n");
        }
        
        if (isCsvFormat) {
            // CSV Format
            if (includeHeaders) {
                previewContent.append("ID,Date,Patient,Type,Doctor,Status");
                if (includeNotes) {
                    previewContent.append(",Notes,Attachments");
                }
                previewContent.append("\n");
            }
            
            // Add records
            for (MedicalRecord record : records) {
                previewContent.append(record.getId()).append(",");
                previewContent.append(record.getDate()).append(",");
                previewContent.append("\"").append(record.getPatient()).append("\",");
                previewContent.append("\"").append(record.getType()).append("\",");
                previewContent.append("\"").append(record.getDoctor()).append("\",");
                previewContent.append("\"").append(record.getStatus()).append("\"");
                
                if (includeNotes) {
                    previewContent.append(",\"").append(record.getNotes().replace("\"", "\"\"")).append("\",");
                    previewContent.append("\"").append(record.getAttachments()).append("\"");
                }
                
                previewContent.append("\n");
            }
        } else {
            // PDF Format (simulated as text)
            previewContent.append("==================================\n");
            previewContent.append("       MEDICAL RECORDS REPORT      \n");
            previewContent.append("==================================\n\n");
            
            if (includeHeaders) {
                previewContent.append(String.format("%-6s %-12s %-20s %-15s %-20s %-10s\n", 
                    "ID", "Date", "Patient", "Type", "Doctor", "Status"));
                previewContent.append("----------------------------------------------------------------------\n");
            }
            
            // Add records
            for (MedicalRecord record : records) {
                previewContent.append(String.format("%-6s %-12s %-20s %-15s %-20s %-10s\n", 
                    record.getId(), 
                    record.getDate(), 
                    record.getPatient(), 
                    record.getType(), 
                    record.getDoctor(), 
                    record.getStatus()
                ));
                
                if (includeNotes) {
                    previewContent.append("\nNotes: ").append(record.getNotes()).append("\n");
                    
                    if (record.getAttachments() != null && !record.getAttachments().isEmpty()) {
                        previewContent.append("Attachments: ").append(record.getAttachments()).append("\n");
                    }
                    
                    previewContent.append("----------------------------------------------------------------------\n");
                }
            }
        }
        
        previewArea.setText(previewContent.toString());
        
        // Create close button
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("action-button");
        closeButton.setPrefWidth(100);
        closeButton.setOnAction(e -> dialog.close());
        
        HBox buttons = new HBox(closeButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(15, 0, 0, 0));
        
        // Assemble layout
        root.setTop(headerBox);
        root.setCenter(previewArea);
        root.setBottom(buttons);
        
        // Set scene and show dialog
        Scene scene = new Scene(root);
        scene.getStylesheets().addAll(recordsTable.getScene().getStylesheets());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // New method to generate record link by consuming the external API
    private String generateRecordLink(MedicalRecord record) {
        String apiUrl = "http://localhost:5000/api/records/generate-link";
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);
            
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            
            org.json.JSONObject payload = new org.json.JSONObject();
            payload.put("patient_id", record.getPatientId() != null ? record.getPatientId() : "nan");
            payload.put("patient_name", record.getPatientName() != null ? record.getPatientName() : "nan");
            payload.put("doctor_id", record.getDoctorId() != null ? record.getDoctorId() : "nan");
            payload.put("doctor_name", record.getDoctorName() != null ? record.getDoctorName() : "nan");
            payload.put("visit_type", record.getVisitType() != null ? record.getVisitType() : "nan");
            payload.put("visit_date", record.getVisitDate() != null ? isoFormat.format(record.getVisitDate()) : "nan");
            payload.put("diagnosis", record.getDiagnosis() != null ? record.getDiagnosis() : "nan");
            payload.put("symptoms", record.getSymptoms() != null ? record.getSymptoms() : "nan");
            payload.put("treatment", record.getTreatment() != null ? record.getTreatment() : "nan");
            payload.put("notes", record.getNotes() != null ? record.getNotes() : "nan");
            payload.put("status", record.getStatus() != null ? record.getStatus() : "nan");
            payload.put("diagnosis_codes", record.getDiagnosisCodes() != null ? record.getDiagnosisCodes() : "nan");
            payload.put("attachments", record.getAttachments() != null ? record.getAttachments() : "nan");
            payload.put("follow_up_date", record.getFollowUpDate() != null ? isoFormat.format(record.getFollowUpDate()) : "nan");
            payload.put("record_type", record.getRecordType() != null ? record.getRecordType() : "nan");
            
            String jsonPayload = payload.toString();
            
            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            org.json.JSONObject jsonResponse = new org.json.JSONObject(response.toString());
            if(jsonResponse.getBoolean("success")) {
                return jsonResponse.getJSONObject("links").getString("record_page");
            } else {
                System.err.println("Failed to generate link: " + jsonResponse.getString("message"));
                return null;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // New helper method to open URL in default browser
    private boolean openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            } else {
                System.err.println("Desktop is not supported.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Initialize pagination controls
     */
    private void initializePagination() {
        // Set up pagination
        recordsPagination.setPageCount(1);
        recordsPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            // Handle pagination changes here
            System.out.println("Page changed to: " + newIndex);
            // In a real implementation, this would load a new page of records
        });
    }
    
    /**
     * Set up action buttons
     */
    private void setupActionButtons() {
        // Set up export button
        exportButton.setOnAction(e -> handleExportRecords());
        
        // Set up add record button
        addRecordButton.setOnAction(e -> handleAddRecord());
    }
} 
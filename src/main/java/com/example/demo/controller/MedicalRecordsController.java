package com.example.demo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize the filter dropdowns
        recordTypeFilter.setItems(FXCollections.observableArrayList(
            "All Records", "Recent Records", "Lab Results", "Consultations", "Prescriptions", "Immunizations"
        ));
        recordTypeFilter.getSelectionModel().selectFirst();
        
        statusFilter.setItems(FXCollections.observableArrayList(
            "All Statuses", "Completed", "Pending", "Expired", "Cancelled"
        ));
        statusFilter.getSelectionModel().selectFirst();
        
        // Set up date picker with default value
        dateFilter.setValue(null);
        
        // Set up main table columns
        setupTableColumns(recordsTable);
        
        // Set up type-specific tables
        setupTypeSpecificTables();
        
        // Load sample data
        loadSampleRecords();
        
        // Update stats dashboard
        updateStatsDashboard();
        
        // Create filtered list
        filteredRecords = new FilteredList<>(allRecordsList, p -> true);
        
        // Set up main table with data
        recordsTable.setItems(filteredRecords);
        
        // Update total records count
        updateTotalRecordsLabel();
        
        // Set up search functionality
        setupSearchAndFilters();
        
        // Add listener for tab changes to update the appropriate table
        recordsTabPane.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                String tabText = newValue.getText();
                if (tabText.equals("All Records")) {
                    // Already showing all records
                } else {
                    // Apply type-specific filter
                    String recordType = tabText;
                    // Special handling for specific tabs
                    if (tabText.equals("Lab Results")) {
                        recordType = "Lab Results";
                    } else if (tabText.equals("Consultations")) {
                        recordType = "Consultation";
                    } else if (tabText.equals("Prescriptions")) {
                        recordType = "Prescription";
                    } else if (tabText.equals("Immunizations")) {
                        recordType = "Immunization";
                    }
                    
                    final String finalRecordType = recordType;
                    filteredRecords.setPredicate(record -> record.getType().equals(finalRecordType));
                    updateTotalRecordsLabel();
                }
            }
        );
        
        // Show welcome dialog for first-time users
        // This would typically check a preferences/settings value
        // For demo purposes, we'll just show it
        Platform.runLater(this::showWelcomeDialog);
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
        
        // Set column widths
        idCol.setPrefWidth(100);
        patientCol.setPrefWidth(150);
        dateCol.setPrefWidth(120);
        typeCol.setPrefWidth(120);
        doctorCol.setPrefWidth(150);
        notesCol.setPrefWidth(250);
        statusCol.setPrefWidth(100);
        actionsCol.setPrefWidth(150);
        
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
                
                {
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
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        // Create an HBox to hold the buttons
                        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(8);
                        hbox.setAlignment(Pos.CENTER);
                        hbox.getChildren().addAll(viewButton, editButton, exportButton);
                        setGraphic(hbox);
                    }
                }
            };
            return cell;
        };
    }
    
    /**
     * Setup search and filter functionality
     */
    private void setupSearchAndFilters() {
        // Search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
        
        // Filter ComboBox listeners
        recordTypeFilter.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> updateFilters());
        
        statusFilter.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> updateFilters());
        
        // Date filter listener
        dateFilter.valueProperty().addListener(
            (observable, oldValue, newValue) -> updateFilters());
        
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
        updateFilters();
    }

    /**
     * Update filters based on current selections
     */
    private void updateFilters() {
        String searchText = searchField.getText().toLowerCase();
        String selectedType = recordTypeFilter.getValue();
        String selectedStatus = statusFilter.getValue();
        LocalDate selectedDate = dateFilter.getValue();
        
        filteredRecords.setPredicate(record -> {
            boolean matchesSearch = searchText == null || searchText.isEmpty() 
                || record.getPatient().toLowerCase().contains(searchText)
                || record.getDoctor().toLowerCase().contains(searchText)
                || record.getType().toLowerCase().contains(searchText)
                || record.getStatus().toLowerCase().contains(searchText)
                || record.getId().toLowerCase().contains(searchText);
            
            boolean matchesType = selectedType == null || selectedType.equals("All Records") 
                || record.getType().equals(selectedType.equals("Recent Records") ? record.getType() : selectedType);
            
            boolean matchesStatus = selectedStatus == null || selectedStatus.equals("All Statuses") 
                || record.getStatus().equals(selectedStatus);
            
            boolean matchesDate = selectedDate == null 
                || (record.getDate() != null && record.getDate().equals(selectedDate.format(DATE_FORMATTER)));
            
            if (selectedType.equals("Recent Records")) {
                // Recent records are within last 7 days
                LocalDate recordDate = LocalDate.parse(record.getDate(), DATE_FORMATTER);
                boolean isRecent = ChronoUnit.DAYS.between(recordDate, LocalDate.now()) <= 7;
                matchesType = isRecent;
            }
            
            return matchesSearch && matchesType && matchesStatus && matchesDate;
        });
        
        updateTotalRecordsLabel();
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
                "blood_test_requisition.pdf"),
                
            new MedicalRecord(
                "MR-10002", 
                "Sarah Johnson", 
                LocalDate.now().minusDays(1).format(DATE_FORMATTER), 
                "Lab Results", 
                "Dr. Michael Brown", 
                "Completed", 
                "Complete blood count and metabolic panel review",
                "Z00.00 (General adult medical exam)",
                "cbc_results.pdf, metabolic_panel.pdf"),
                
            new MedicalRecord(
                "MR-10003", 
                "Robert Garcia", 
                LocalDate.now().minusDays(2).format(DATE_FORMATTER), 
                "Prescription", 
                "Dr. Emily Davis", 
                "Completed", 
                "Prescription renewal for hypertension medication",
                "I10 (Essential hypertension)",
                "prescription_form.pdf"),
                
            new MedicalRecord(
                "MR-10004", 
                "Lisa Chen", 
                LocalDate.now().minusDays(3).format(DATE_FORMATTER), 
                "Immunization", 
                "Dr. James Smith", 
                "Completed", 
                "Seasonal flu vaccination",
                "Z23 (Encounter for immunization)",
                "immunization_record.pdf"),
                
            new MedicalRecord(
                "MR-10005", 
                "David Wilson", 
                LocalDate.now().minusDays(4).format(DATE_FORMATTER), 
                "Consultation", 
                "Dr. Jane Wilson", 
                "Completed", 
                "Follow-up appointment for diabetes management",
                "E11.9 (Type 2 diabetes mellitus without complications)",
                "diabetes_management_plan.pdf"),
                
            new MedicalRecord(
                "MR-10006", 
                "Maria Rodriguez", 
                LocalDate.now().minusDays(5).format(DATE_FORMATTER), 
                "Lab Results", 
                "Dr. Michael Brown", 
                "Pending", 
                "Lipid panel and A1C test results pending",
                "E78.5 (Hyperlipidemia, unspecified)",
                "test_order.pdf"),
                
            new MedicalRecord(
                "MR-10007", 
                "Thomas Lee", 
                LocalDate.now().minusDays(7).format(DATE_FORMATTER), 
                "Prescription", 
                "Dr. Emily Davis", 
                "Expired", 
                "Antibiotic prescription for respiratory infection",
                "J06.9 (Acute upper respiratory infection)",
                "antibiotic_prescription.pdf"),
                
            new MedicalRecord(
                "MR-10008", 
                "Jennifer Taylor", 
                LocalDate.now().minusDays(10).format(DATE_FORMATTER), 
                "Consultation", 
                "Dr. James Smith", 
                "Completed", 
                "Annual physical examination",
                "Z00.00 (General adult medical exam)",
                "exam_results.pdf"),
                
            new MedicalRecord(
                "MR-10009", 
                "William Johnson", 
                LocalDate.now().minusDays(12).format(DATE_FORMATTER), 
                "Lab Results", 
                "Dr. Jane Wilson", 
                "Pending", 
                "Thyroid function test results awaiting analysis",
                "E03.9 (Hypothyroidism, unspecified)",
                ""),
                
            new MedicalRecord(
                "MR-10010", 
                "Emily Davis", 
                LocalDate.now().minusDays(14).format(DATE_FORMATTER), 
                "Immunization", 
                "Dr. Michael Brown", 
                "Completed", 
                "COVID-19 booster vaccination",
                "Z23 (Encounter for immunization)",
                "covid_vaccination_record.pdf")
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
        showSingleRecordExportDialog(record);
    }
    
    /**
     * Show a dialog for exporting a single record
     */
    private void showSingleRecordExportDialog(MedicalRecord record) {
        // Create a dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Export Medical Record");
        dialog.setMinWidth(450);
        dialog.setMinHeight(400);
        
        // Create layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(25));
        root.getStyleClass().add("custom-dialog");
        
        // Create title
        Label titleLabel = new Label("Export Medical Record: " + record.getId());
        titleLabel.getStyleClass().add("export-dialog-title");
        
        // Create patient info
        Label patientLabel = new Label("Patient: " + record.getPatient());
        patientLabel.getStyleClass().add("form-field-label");
        
        Label dateLabel = new Label("Date: " + record.getDate());
        dateLabel.getStyleClass().add("form-field-label");
        
        Label typeLabel = new Label("Type: " + record.getType());
        typeLabel.getStyleClass().add("form-field-label");
        
        VBox recordInfoBox = new VBox(5, patientLabel, dateLabel, typeLabel);
        recordInfoBox.setPadding(new Insets(0, 0, 15, 0));
        
        // Create content grid
        GridPane content = new GridPane();
        content.setVgap(15);
        content.setHgap(15);
        
        // Format section title
        Label formatLabel = new Label("Export Format");
        formatLabel.getStyleClass().add("export-section-title");
        
        // Format options
        ToggleGroup formatGroup = new ToggleGroup();
        RadioButton csvOption = new RadioButton("CSV Format (.csv)");
        csvOption.setToggleGroup(formatGroup);
        csvOption.setSelected(true);
        
        RadioButton pdfOption = new RadioButton("PDF Format (.pdf)");
        pdfOption.setToggleGroup(formatGroup);
        
        // Add description for PDF format
        Label pdfDescriptionLabel = new Label("PDF exports include a detailed clinical report with follow-up recommendations");
        pdfDescriptionLabel.getStyleClass().add("form-field-label");
        pdfDescriptionLabel.setWrapText(true);
        pdfDescriptionLabel.setStyle("-fx-font-style: italic; -fx-text-fill: -fx-text-light; -fx-font-size: 11px;");
        
        VBox formatBox = new VBox(10, csvOption, pdfOption, pdfDescriptionLabel);
        formatBox.getStyleClass().add("export-section");
        
        // Options section title
        Label optionsLabel = new Label("Export Options");
        optionsLabel.getStyleClass().add("export-section-title");
        
        // Export options
        CheckBox includeHeadersOption = new CheckBox("Include column headers");
        includeHeadersOption.setSelected(true);
        
        CheckBox includeMetadataOption = new CheckBox("Include report metadata");
        includeMetadataOption.setSelected(true);
        
        CheckBox includeNotesOption = new CheckBox("Include notes and attachments");
        includeNotesOption.setSelected(true);
        
        VBox optionsBox = new VBox(10, includeHeadersOption, includeMetadataOption, includeNotesOption);
        optionsBox.getStyleClass().add("export-section");
        
        // Update UI based on format selection
        formatGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean isPdf = (newVal == pdfOption);
            includeHeadersOption.setDisable(isPdf); // Headers always included in detailed PDF
            if (isPdf) {
                includeHeadersOption.setSelected(true);
            }
        });
        
        // Add to content
        int row = 0;
        content.add(formatLabel, 0, row++, 2, 1);
        content.add(formatBox, 0, row++, 2, 1);
        
        Separator separator1 = new Separator();
        content.add(separator1, 0, row++, 2, 1);
        
        content.add(optionsLabel, 0, row++, 2, 1);
        content.add(optionsBox, 0, row++, 2, 1);
        
        // Create buttons
        Button exportButton = new Button("Export");
        exportButton.getStyleClass().addAll("action-button", "primary");
        exportButton.setPrefWidth(100);
        
        Button previewButton = new Button("Preview");
        previewButton.getStyleClass().add("action-button");
        previewButton.setPrefWidth(100);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("action-button");
        cancelButton.setPrefWidth(100);
        
        HBox buttons = new HBox(10, cancelButton, previewButton, exportButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(20, 0, 0, 0));
        
        // Handle button actions
        cancelButton.setOnAction(e -> dialog.close());
        
        previewButton.setOnAction(e -> {
            // Get selected options
            boolean isCsvFormat = csvOption.isSelected();
            boolean includeHeaders = includeHeadersOption.isSelected();
            boolean includeMetadata = includeMetadataOption.isSelected();
            boolean includeNotes = includeNotesOption.isSelected();
            
            // Create a list with just this record
            List<MedicalRecord> recordList = new ArrayList<>();
            recordList.add(record);
            
            // Show preview dialog
            showPreviewDialog(recordList, isCsvFormat, includeHeaders, includeMetadata, includeNotes);
        });
        
        exportButton.setOnAction(e -> {
            // Get selected options
            boolean isCsvFormat = csvOption.isSelected();
            boolean includeHeaders = includeHeadersOption.isSelected();
            boolean includeMetadata = includeMetadataOption.isSelected();
            boolean includeNotes = includeNotesOption.isSelected();
            
            // Build export filename suggestion and filter
            String fileExtension = isCsvFormat ? "csv" : "pdf";
            String formatDescription = isCsvFormat ? "CSV Files" : "PDF Files";
            String safeFileName = record.getId().replaceAll("[^a-zA-Z0-9\\-_]", "_");
            
            // Configure file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save " + formatDescription.replace(" Files", ""));
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(formatDescription, "*." + fileExtension)
            );
            fileChooser.setInitialFileName(safeFileName + "_medical_record." + fileExtension);
            
            // Show save dialog
            File file = fileChooser.showSaveDialog(dialog.getOwner());
            
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
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Detailed PDF Export");
                            alert.setHeaderText("Detailed Medical Record Report");
                            alert.setContentText("A comprehensive clinical report has been generated in text-based PDF format. " +
                                              "The report includes patient information, medical details, and follow-up recommendations specific to the record type.\n\n" +
                                              "In a production environment, a proper PDF library would be used to create a better formatted document.");
                            alert.getDialogPane().getStyleClass().add("custom-alert");
                            alert.showAndWait();
                        });
                    }
                }
                
                // Close the dialog
                dialog.close();
                
                // Show success or error message
                if (success) {
                    showSuccessAlert("Export Successful", "Medical record exported successfully to: " + file.getAbsolutePath());
                } else {
                    showErrorAlert("Export Failed", "Failed to export medical record. Please try again.");
                }
            }
        });
        
        // Assemble layout
        VBox topSection = new VBox(5, titleLabel, recordInfoBox);
        topSection.setPadding(new Insets(0, 0, 10, 0));
        root.setTop(topSection);
        root.setCenter(content);
        root.setBottom(buttons);
        
        // Set scene and show dialog
        Scene scene = new Scene(root);
        
        // Apply stylesheets from the main application
        scene.getStylesheets().addAll(recordsTable.getScene().getStylesheets());
        
        dialog.setScene(scene);
        dialog.showAndWait();
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
                    previewContent.append("\"").append(String.join("; ", record.getAttachments())).append("\"");
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
                    
                    if (!record.getAttachments().isEmpty()) {
                        previewContent.append("Attachments: ").append(String.join(", ", record.getAttachments())).append("\n");
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
    
    /**
     * Show an error alert
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().getStyleClass().add("custom-alert");
        alert.showAndWait();
    }
    
    /**
     * Show a success alert
     */
    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().getStyleClass().add("custom-alert");
        alert.showAndWait();
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
        dialog.getDialogPane().setPrefWidth(650);
        dialog.getDialogPane().setPrefHeight(550);
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        // Create tabs for different sections of the form
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Create basic info tab
        Tab basicInfoTab = new Tab("Basic Information");
        GridPane basicGrid = new GridPane();
        basicGrid.setHgap(15);
        basicGrid.setVgap(15);
        basicGrid.setPadding(new javafx.geometry.Insets(20));
        
        // Create form fields
        Label recordIdLabel = new Label("Auto-generated");
        recordIdLabel.setStyle("-fx-font-style: italic; -fx-text-fill: -fx-text-light;");
        if (record != null) {
            recordIdLabel = new Label(record.getId());
            recordIdLabel.setStyle("-fx-font-weight: bold;");
        }
        
        TextField patientField = new TextField();
        patientField.setPrefWidth(400);
        patientField.setPromptText("Enter patient name");
        patientField.getStyleClass().add("form-field");
        
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
        
        TextField doctorField = new TextField();
        doctorField.setPrefWidth(400);
        doctorField.setPromptText("Enter doctor's name");
        doctorField.getStyleClass().add("form-field");
        
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.setItems(FXCollections.observableArrayList(
            "Completed", "Pending", "Expired", "Cancelled"
        ));
        statusComboBox.setPrefWidth(400);
        statusComboBox.getStyleClass().add("form-field");
        
        // Create and style form labels
        Label patientLabel = new Label("Patient:");
        Label typeLabel = new Label("Record Type:");
        Label dateLabel = new Label("Date:");
        Label doctorLabel = new Label("Attending Doctor:");
        Label statusLabel = new Label("Status:");
        Label idLabel = new Label("Record ID:");
        
        // Apply styles to labels
        patientLabel.getStyleClass().add("form-field-label");
        typeLabel.getStyleClass().add("form-field-label");
        dateLabel.getStyleClass().add("form-field-label");
        doctorLabel.getStyleClass().add("form-field-label");
        statusLabel.getStyleClass().add("form-field-label");
        idLabel.getStyleClass().add("form-field-label");
        
        // Add fields to basic info grid
        int row = 0;
        basicGrid.add(idLabel, 0, row);
        basicGrid.add(recordIdLabel, 1, row++);
        
        basicGrid.add(patientLabel, 0, row);
        basicGrid.add(patientField, 1, row++);
        
        basicGrid.add(typeLabel, 0, row);
        basicGrid.add(typeComboBox, 1, row++);
        
        basicGrid.add(dateLabel, 0, row);
        basicGrid.add(datePicker, 1, row++);
        
        basicGrid.add(doctorLabel, 0, row);
        basicGrid.add(doctorField, 1, row++);
        
        basicGrid.add(statusLabel, 0, row);
        basicGrid.add(statusComboBox, 1, row++);
        
        basicInfoTab.setContent(basicGrid);
        
        // Create Details tab
        Tab detailsTab = new Tab("Medical Details");
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(15);
        detailsGrid.setPadding(new javafx.geometry.Insets(20));
        
        // Add a section title
        Label detailsSectionTitle = new Label("Additional Information");
        detailsSectionTitle.getStyleClass().add("form-section-title");
        
        TextArea notesArea = new TextArea();
        notesArea.setPrefWidth(400);
        notesArea.setPrefHeight(150);
        notesArea.setPromptText("Enter medical notes, findings, or observations");
        notesArea.setWrapText(true);
        notesArea.getStyleClass().add("form-field");
        
        TextField diagnosisCodesField = new TextField();
        diagnosisCodesField.setPrefWidth(400);
        diagnosisCodesField.setPromptText("E.g., ICD-10 codes: J45.909, I10");
        diagnosisCodesField.getStyleClass().add("form-field");
        
        // Attachments section with list view and buttons
        ListView<String> attachmentsListView = new ListView<>();
        attachmentsListView.setPrefHeight(120);
        attachmentsListView.getStyleClass().add("attachments-list");
        
        Button addAttachmentButton = new Button("Add");
        Button removeAttachmentButton = new Button("Remove");
        addAttachmentButton.getStyleClass().addAll("action-button", "primary");
        removeAttachmentButton.getStyleClass().add("action-button");
        
        HBox attachmentButtonsBox = new HBox(10, addAttachmentButton, removeAttachmentButton);
        
        // Create and style form labels
        Label notesLabel = new Label("Notes:");
        Label diagnosisCodesLabel = new Label("Diagnosis Codes:");
        Label attachmentsLabel = new Label("Attachments:");
        
        notesLabel.getStyleClass().add("form-field-label");
        diagnosisCodesLabel.getStyleClass().add("form-field-label");
        attachmentsLabel.getStyleClass().add("form-field-label");
        
        // Add detail fields to grid
        row = 0;
        detailsGrid.add(detailsSectionTitle, 0, row++, 2, 1);
        
        detailsGrid.add(notesLabel, 0, row);
        detailsGrid.add(notesArea, 1, row++);
        
        detailsGrid.add(diagnosisCodesLabel, 0, row);
        detailsGrid.add(diagnosisCodesField, 1, row++);
        
        detailsGrid.add(attachmentsLabel, 0, row);
        detailsGrid.add(attachmentsListView, 1, row++);
        
        detailsGrid.add(new Label(""), 0, row);
        detailsGrid.add(attachmentButtonsBox, 1, row++);
        
        detailsTab.setContent(detailsGrid);
        
        // Add tabs to tab pane
        tabPane.getTabs().addAll(basicInfoTab, detailsTab);
        
        // If in edit mode, populate fields with record data
        if (record != null) {
            patientField.setText(record.getPatient());
            typeComboBox.setValue(record.getType());
            datePicker.setValue(LocalDate.parse(record.getDate()));
            doctorField.setText(record.getDoctor());
            statusComboBox.setValue(record.getStatus());
            notesArea.setText(record.getNotes());
            diagnosisCodesField.setText(record.getDiagnosisCodes());
            
            if (record.getAttachments() != null && !record.getAttachments().isEmpty()) {
                String[] attachments = record.getAttachments().split(",");
                attachmentsListView.getItems().addAll(attachments);
            }
            
            // If it's view-only
            if (title.startsWith("View")) {
                patientField.setEditable(false);
                typeComboBox.setDisable(true);
                datePicker.setDisable(true);
                doctorField.setEditable(false);
                statusComboBox.setDisable(true);
                notesArea.setEditable(false);
                diagnosisCodesField.setEditable(false);
                addAttachmentButton.setDisable(true);
                removeAttachmentButton.setDisable(true);
            }
        } else {
            // Default values for new record
            typeComboBox.setValue("Consultation");
            statusComboBox.setValue("Pending");
        }
        
        // Setup attachment buttons
        addAttachmentButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Attachment");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt")
            );
            
            File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            
            if (selectedFile != null) {
                // In a real application, we would upload/attach the file
                // For this demo, we'll just add the file name to the list
                attachmentsListView.getItems().add(selectedFile.getName());
            }
        });
        
        removeAttachmentButton.setOnAction(e -> {
            int selectedIndex = attachmentsListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                attachmentsListView.getItems().remove(selectedIndex);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Selection");
                alert.setHeaderText(null);
                alert.setContentText("Please select an attachment to remove");
                alert.getDialogPane().getStyleClass().add("custom-alert");
                alert.showAndWait();
            }
        });
        
        // Form validation
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        // Add validation indicators
        Label patientValidationLabel = new Label("*");
        Label typeValidationLabel = new Label("*");
        Label dateValidationLabel = new Label("*");
        Label doctorValidationLabel = new Label("*");
        Label statusValidationLabel = new Label("*");
        
        patientValidationLabel.setStyle("-fx-text-fill: -fx-error-color;");
        typeValidationLabel.setStyle("-fx-text-fill: -fx-error-color;");
        dateValidationLabel.setStyle("-fx-text-fill: -fx-error-color;");
        doctorValidationLabel.setStyle("-fx-text-fill: -fx-error-color;");
        statusValidationLabel.setStyle("-fx-text-fill: -fx-error-color;");
        
        // Enable/disable save button based on form validation
        Runnable validateForm = () -> {
            boolean isPatientValid = !patientField.getText().trim().isEmpty();
            boolean isTypeValid = typeComboBox.getValue() != null;
            boolean isDateValid = datePicker.getValue() != null;
            boolean isDoctorValid = !doctorField.getText().trim().isEmpty();
            boolean isStatusValid = statusComboBox.getValue() != null;
            
            // Update validation indicators
            patientValidationLabel.setVisible(!isPatientValid);
            typeValidationLabel.setVisible(!isTypeValid);
            dateValidationLabel.setVisible(!isDateValid);
            doctorValidationLabel.setVisible(!isDoctorValid);
            statusValidationLabel.setVisible(!isStatusValid);
            
            boolean isValid = isPatientValid && isTypeValid && isDateValid && isDoctorValid && isStatusValid;
            saveButton.setDisable(!isValid);
        };
        
        // Add validation listeners
        patientField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        doctorField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        statusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        
        // Run initial validation
        validateForm.run();
        
        // Add content to dialog
        dialog.getDialogPane().setContent(tabPane);
        
        // Request focus on the patient field by default
        patientField.requestFocus();
        
        // Convert the result to MedicalRecord object when dialog is confirmed
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String id;
                if (record != null) {
                    // Editing existing record - use same ID
                    id = record.getId();
                } else {
                    // Generate a new ID for new record
                    id = "MR-" + String.format("%05d", (int)(Math.random() * 100000));
                }
                
                // Join attachments into a comma-separated string
                StringBuilder attachmentsStr = new StringBuilder();
                for (String attachment : attachmentsListView.getItems()) {
                    if (attachmentsStr.length() > 0) {
                        attachmentsStr.append(",");
                    }
                    attachmentsStr.append(attachment);
                }
                
                return new MedicalRecord(
                    id,
                    patientField.getText().trim(),
                    datePicker.getValue().format(DATE_FORMATTER),
                    typeComboBox.getValue(),
                    doctorField.getText().trim(),
                    statusComboBox.getValue(),
                    notesArea.getText().trim(),
                    diagnosisCodesField.getText().trim(),
                    attachmentsStr.toString()
                );
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<MedicalRecord> result = dialog.showAndWait();
        
        result.ifPresent(newRecord -> {
            if (record != null) {
                // Update existing record
                updateRecord(record, newRecord);
            } else {
                // Add new record
                addRecord(newRecord);
            }
        });
    }
    
    /**
     * Add a new medical record
     */
    private void addRecord(MedicalRecord record) {
        // Add to main record list
        allRecordsList.add(record);
        
        // Add to type-specific list
        String type = record.getType();
        if (recordsByType.containsKey(type)) {
            recordsByType.get(type).add(record);
        }
        
        // Update stats and refresh view
        updateStatsDashboard();
        updateTotalRecordsLabel();
        
        // Show success message
        showSuccessAlert("Record Added", "Medical record has been successfully added.");
    }
    
    /**
     * Update an existing medical record
     */
    private void updateRecord(MedicalRecord oldRecord, MedicalRecord newRecord) {
        // Find and update in main list
        int index = allRecordsList.indexOf(oldRecord);
        if (index >= 0) {
            allRecordsList.set(index, newRecord);
        }
        
        // Update in type-specific lists if type has changed
        if (!oldRecord.getType().equals(newRecord.getType())) {
            // Remove from old type list
            recordsByType.get(oldRecord.getType()).remove(oldRecord);
            
            // Add to new type list
            if (recordsByType.containsKey(newRecord.getType())) {
                recordsByType.get(newRecord.getType()).add(newRecord);
            }
        } else {
            // Just update in the same type list
            ObservableList<MedicalRecord> typeList = recordsByType.get(newRecord.getType());
            int typeIndex = typeList.indexOf(oldRecord);
            if (typeIndex >= 0) {
                typeList.set(typeIndex, newRecord);
            }
        }
        
        // Update stats and refresh view
        updateStatsDashboard();
        updateTotalRecordsLabel();
        
        // Show success message
        showSuccessAlert("Record Updated", "Medical record has been successfully updated.");
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
        
        public MedicalRecord(String id, String patient, String date, String type, String doctor, String status) {
            this(id, patient, date, type, doctor, status, "", "", "");
        }
        
        public MedicalRecord(String id, String patient, String date, String type, String doctor, String status, 
                            String notes, String diagnosisCodes, String attachments) {
            this.id = id;
            this.patient = patient;
            this.date = date;
            this.type = type;
            this.doctor = doctor;
            this.status = status;
            this.notes = notes;
            this.diagnosisCodes = diagnosisCodes;
            this.attachments = attachments;
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
        dialog.setMinWidth(500);
        dialog.setMinHeight(500);
        
        // Create layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(25));
        root.getStyleClass().add("custom-dialog");
        
        // Create title
        Label titleLabel = new Label("Export Medical Records");
        titleLabel.getStyleClass().add("export-dialog-title");
        
        // Create content grid
        GridPane content = new GridPane();
        content.setVgap(15);
        content.setHgap(15);
        
        // Format section title
        Label formatLabel = new Label("Export Format");
        formatLabel.getStyleClass().add("export-section-title");
        
        // Format options
        ToggleGroup formatGroup = new ToggleGroup();
        RadioButton csvOption = new RadioButton("CSV Format (.csv)");
        csvOption.setToggleGroup(formatGroup);
        csvOption.setSelected(true);
        
        RadioButton pdfOption = new RadioButton("PDF Format (.pdf)");
        pdfOption.setToggleGroup(formatGroup);
        
        // Add description for PDF format
        Label pdfDescriptionLabel = new Label("PDF exports include a detailed clinical report with follow-up recommendations");
        pdfDescriptionLabel.getStyleClass().add("form-field-label");
        pdfDescriptionLabel.setWrapText(true);
        pdfDescriptionLabel.setStyle("-fx-font-style: italic; -fx-text-fill: -fx-text-light; -fx-font-size: 11px;");
        
        VBox formatBox = new VBox(10, csvOption, pdfOption, pdfDescriptionLabel);
        formatBox.getStyleClass().add("export-section");
        
        // Filter section title
        Label filterLabel = new Label("Export Filters");
        filterLabel.getStyleClass().add("export-section-title");
        
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
        
        HBox typeFilterBox = new HBox(10, typeFilterLabel, typeFilterCombo);
        typeFilterBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox filterBox = new VBox(10, currentFilterOption, typeFilterBox);
        filterBox.getStyleClass().add("export-section");
        
        // Options section title
        Label optionsLabel = new Label("Export Options");
        optionsLabel.getStyleClass().add("export-section-title");
        
        // Export options
        CheckBox includeHeadersOption = new CheckBox("Include column headers");
        includeHeadersOption.setSelected(true);
        
        CheckBox includeMetadataOption = new CheckBox("Include report metadata");
        includeMetadataOption.setSelected(true);
        
        CheckBox includeNotesOption = new CheckBox("Include notes and attachments");
        includeNotesOption.setSelected(true);
        
        VBox optionsBox = new VBox(10, includeHeadersOption, includeMetadataOption, includeNotesOption);
        optionsBox.getStyleClass().add("export-section");
        
        // Update UI based on format selection
        formatGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean isPdf = (newVal == pdfOption);
            includeHeadersOption.setDisable(isPdf); // Headers always included in detailed PDF
            if (isPdf) {
                includeHeadersOption.setSelected(true);
            }
        });
        
        // Add to content
        int row = 0;
        content.add(formatLabel, 0, row++, 2, 1);
        content.add(formatBox, 0, row++, 2, 1);
        
        Separator separator1 = new Separator();
        content.add(separator1, 0, row++, 2, 1);
        
        content.add(filterLabel, 0, row++, 2, 1);
        content.add(filterBox, 0, row++, 2, 1);
        
        Separator separator2 = new Separator();
        content.add(separator2, 0, row++, 2, 1);
        
        content.add(optionsLabel, 0, row++, 2, 1);
        content.add(optionsBox, 0, row++, 2, 1);
        
        // Preview section
        Label previewLabel = new Label("Export Preview");
        previewLabel.getStyleClass().add("export-section-title");
        content.add(previewLabel, 0, row++, 2, 1);
        
        Label previewSummaryLabel = new Label("");
        previewSummaryLabel.getStyleClass().add("form-field-label");
        content.add(previewSummaryLabel, 0, row++, 2, 1);
        
        // Update the preview when options change
        Runnable updatePreview = () -> {
            boolean useCurrentFilters = currentFilterOption.isSelected();
            String typeFilter = typeFilterCombo.getValue();
            
            // Calculate how many records would be exported
            List<MedicalRecord> recordsToExport = getRecordsForExport(useCurrentFilters, typeFilter);
            int recordCount = recordsToExport.size();
            
            String formatType = csvOption.isSelected() ? "CSV" : "PDF";
            String filterDescription = useCurrentFilters ? 
                "with current filters applied" : 
                ("All Types".equals(typeFilter) ? "all records" : "filtered by " + typeFilter);
            
            previewSummaryLabel.setText(String.format(
                "Will export %d records as %s %s", 
                recordCount, formatType, filterDescription
            ));
        };
        
        // Add listeners to update preview
        csvOption.selectedProperty().addListener((obs, old, val) -> updatePreview.run());
        pdfOption.selectedProperty().addListener((obs, old, val) -> updatePreview.run());
        currentFilterOption.selectedProperty().addListener((obs, old, val) -> updatePreview.run());
        typeFilterCombo.valueProperty().addListener((obs, old, val) -> updatePreview.run());
        
        // Update preview initially
        updatePreview.run();
        
        // Create buttons
        Button exportButton = new Button("Export");
        exportButton.getStyleClass().addAll("action-button", "primary");
        exportButton.setPrefWidth(100);
        
        Button previewButton = new Button("Preview");
        previewButton.getStyleClass().add("action-button");
        previewButton.setPrefWidth(100);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("action-button");
        cancelButton.setPrefWidth(100);
        
        HBox buttons = new HBox(10, cancelButton, previewButton, exportButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(20, 0, 0, 0));
        
        // Handle button actions
        cancelButton.setOnAction(e -> dialog.close());
        
        previewButton.setOnAction(e -> {
            // Get selected options
            boolean isCsvFormat = csvOption.isSelected();
            boolean useCurrentFilters = currentFilterOption.isSelected();
            String typeFilter = typeFilterCombo.getValue();
            boolean includeHeaders = includeHeadersOption.isSelected();
            boolean includeMetadata = includeMetadataOption.isSelected();
            boolean includeNotes = includeNotesOption.isSelected();
            
            // Get the records to export
            List<MedicalRecord> recordsToExport = getRecordsForExport(useCurrentFilters, typeFilter);
            
            // Show preview dialog
            showPreviewDialog(recordsToExport, isCsvFormat, includeHeaders, includeMetadata, includeNotes);
        });
        
        exportButton.setOnAction(e -> {
            // Get selected options
            boolean isCsvFormat = csvOption.isSelected();
            boolean useCurrentFilters = currentFilterOption.isSelected();
            String typeFilter = typeFilterCombo.getValue();
            boolean includeHeaders = includeHeadersOption.isSelected();
            boolean includeMetadata = includeMetadataOption.isSelected();
            boolean includeNotes = includeNotesOption.isSelected();
            
            // Build export filename suggestion and filter
            String fileExtension = isCsvFormat ? "csv" : "pdf";
            String formatDescription = isCsvFormat ? "CSV Files" : "PDF Files";
            
            // Configure file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save " + formatDescription.replace(" Files", ""));
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(formatDescription, "*." + fileExtension)
            );
            fileChooser.setInitialFileName("medical_records_export." + fileExtension);
            
            // Show save dialog
            File file = fileChooser.showSaveDialog(dialog.getOwner());
            
            if (file != null) {
                // Get the records to export
                List<MedicalRecord> recordsToExport = getRecordsForExport(useCurrentFilters, typeFilter);
                
                // Export the records
                boolean success = false;
                if (isCsvFormat) {
                    success = exportToCsv(file, recordsToExport, includeHeaders, includeMetadata, includeNotes);
                } else {
                    success = exportToPdf(file, recordsToExport, includeHeaders, includeMetadata, includeNotes);
                }
                
                // Close the dialog
                dialog.close();
                
                // Show success or error message
                if (success) {
                    showSuccessAlert("Export Successful", "Medical records exported successfully to: " + file.getAbsolutePath());
                } else {
                    showErrorAlert("Export Failed", "Failed to export medical records. Please try again.");
                }
            }
        });
        
        // Assemble layout
        VBox topSection = new VBox(titleLabel);
        topSection.setPadding(new Insets(0, 0, 15, 0));
        root.setTop(topSection);
        root.setCenter(content);
        root.setBottom(buttons);
        
        // Set scene and show dialog
        Scene scene = new Scene(root);
        
        // Apply stylesheets from the main application
        scene.getStylesheets().addAll(recordsTable.getScene().getStylesheets());
        
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    /**
     * Get the records to export based on filters
     * @param useCurrentFilters Whether to use current UI filters
     * @param typeFilter Record type filter to apply
     * @return List of records to export
     */
    private List<MedicalRecord> getRecordsForExport(boolean useCurrentFilters, String typeFilter) {
        // Start with all records
        List<MedicalRecord> records = new ArrayList<>(allRecordsList);
        
        // Apply current filters if requested
        if (useCurrentFilters) {
            // Apply search term filter if active
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                String searchTerm = searchField.getText().toLowerCase();
                records = records.stream()
                    .filter(record -> record.getPatient().toLowerCase().contains(searchTerm) || 
                                     record.getType().toLowerCase().contains(searchTerm) ||
                                     record.getDoctor().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
            }
            
            // Apply status filter if active
            String statusFilter = recordTypeFilter.getValue();
            if (!"All Records".equals(statusFilter)) {
                records = records.stream()
                    .filter(record -> statusFilter.equals(record.getStatus()))
                    .collect(Collectors.toList());
            }
        }
        
        // Apply type filter if not "All Types"
        if (!"All Types".equals(typeFilter)) {
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
} 
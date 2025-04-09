package com.example.personaltrial;

import javafx.scene.input.KeyEvent;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.*;

public class HelloController implements Initializable {

    @FXML
    private Button btnDownload;

    @FXML
    private Button btnUpload;

    @FXML
    private Button btnGroup;

    @FXML
    private TextField txtMembers;

    @FXML
    private Label lblCharacters;

    @FXML
    private MenuItem mnClear;

    @FXML
    private MenuItem mnClose;

    @FXML
    private MenuItem mnSave;

    @FXML
    private MenuItem mnUpload;

    @FXML
    private TextArea txtDisplay;

    // Declaring a dialogbox
    FileChooser dialogbox = new FileChooser();

    File selectedFile = null;
    Scanner inputFile = null;
    List<String> data = new ArrayList<>();
    PrintWriter outputFile = null;
    private boolean isManualInput = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize button states - disable download and group buttons when starting
        updateButtonStates();
        // Initialize character count
        lblCharacters.setText("Character count: 0");

        // Add listener to txtDisplay to detect direct input
        txtDisplay.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                // User is typing directly
                isManualInput = true;
                updateButtonStates();
            }
        });
    }

    @FXML
    void getCharacters(KeyEvent event) {
        lblCharacters.setText("Character count: " + txtDisplay.getText().length());
        // Update button states whenever text changes
        updateButtonStates();
    }

    // Helper method to update button states based on txtDisplay content
    private void updateButtonStates() {
        boolean isEmpty = txtDisplay.getText().isEmpty();
        btnDownload.setDisable(isEmpty);

        // Enable group button if there's text, regardless of whether a file is loaded
        btnGroup.setDisable(isEmpty);
    }

    @FXML
    void onCancel(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    void onClear(ActionEvent event) {
        // Clear the text display
        txtDisplay.clear();

        // Update character count label
        lblCharacters.setText("Character count: 0");

        // Reset the selectedFile
        selectedFile = null;
        data.clear();
        isManualInput = false;

        // Update button states
        updateButtonStates();
    }

    @FXML
    void onDownload(ActionEvent event) throws FileNotFoundException {
        // Clear any existing filters first
        dialogbox.getExtensionFilters().clear();

        // Add filters for text and Excel files
        dialogbox.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Display save dialog
        File groupedFile = dialogbox.showSaveDialog(new Stage());

        if (groupedFile != null) {
            // Check if we need to handle Excel files
            if (groupedFile.getName().endsWith(".xlsx") || groupedFile.getName().endsWith(".xls")) {
                saveToExcel(groupedFile);
            } else {
                // Default to text file
                PrintWriter outputFile = new PrintWriter(groupedFile);
                outputFile.println(txtDisplay.getText());
                outputFile.close();
            }

            txtDisplay.clear();
            lblCharacters.setText("Character count: 0");
            txtMembers.setText("");
            isManualInput = false;

            // Update button states after clearing
            updateButtonStates();
        }
    }

    // Method to save content to Excel file
    private void saveToExcel(File file) {
        Workbook workbook;

        if (file.getName().endsWith(".xlsx")) {
            workbook = new XSSFWorkbook();
        } else {
            workbook = new HSSFWorkbook();  // For older .xls format
        }

        Sheet sheet = workbook.createSheet("Groups");

        // Split the text content by lines
        String[] lines = txtDisplay.getText().split("\n");

        // Write each line to a new row in the Excel sheet
        for (int i = 0; i < lines.length; i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(lines[i]);
        }

        // Write the workbook to the file
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onUpload(ActionEvent event) {
        try {
            // Clear any existing filters first
            dialogbox.getExtensionFilters().clear();

            // Add filters for text and Excel files
            dialogbox.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            selectedFile = dialogbox.showOpenDialog(new Stage());

            if (selectedFile != null) {
                // Clear content that was already on the txtDisplay
                txtDisplay.setText("");
                isManualInput = false;

                // Check file type and process accordingly
                if (selectedFile.getName().endsWith(".xlsx") || selectedFile.getName().endsWith(".xls")) {
                    // Handle Excel files
                    readFromExcel(selectedFile);
                } else {
                    // Handle text files
                    inputFile = new Scanner(selectedFile);
                    while (inputFile.hasNextLine()) {
                        txtDisplay.appendText(inputFile.nextLine() + "\n");
                    }
                    inputFile.close();
                }

                lblCharacters.setText("Character count: " + txtDisplay.getText().length());

                // Update button states after loading content
                updateButtonStates();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Show error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not read file");
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // Method to read data from Excel file
    private void readFromExcel(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        Workbook workbook;

        // Create appropriate workbook based on file extension
        if (file.getName().endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(fileInputStream);
        } else {
            workbook = new HSSFWorkbook(fileInputStream);
        }

        // Get the first sheet
        Sheet sheet = workbook.getSheetAt(0);

        // Read rows and cells
        for (Row row : sheet) {
            StringBuilder lineBuilder = new StringBuilder();

            // Process each cell in the row
            for (Cell cell : row) {
                switch (cell.getCellType()) {
                    case STRING:
                        lineBuilder.append(cell.getStringCellValue());
                        break;
                    case NUMERIC:
                        lineBuilder.append(cell.getNumericCellValue());
                        break;
                    case BOOLEAN:
                        lineBuilder.append(cell.getBooleanCellValue());
                        break;
                    default:
                        lineBuilder.append(" ");
                }
                lineBuilder.append("\t"); // Add tab between cells
            }

            // Append the line to text display
            txtDisplay.appendText(lineBuilder.toString().trim() + "\n");
        }

        // Close resources
        workbook.close();
        fileInputStream.close();
    }

    @FXML
    void onGroup(ActionEvent event) throws IOException {
        // Check to see if the number required to make a group is empty
        if (txtMembers.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setHeaderText("Missing Information");
            alert.setContentText("Enter the number required to make a group");
            alert.showAndWait();
            return; // Exit the method early
        }

        // Check if there's any content to group
        if (txtDisplay.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setHeaderText("No Names to Group");
            alert.setContentText("Please enter names or upload a file first");
            alert.showAndWait();
            return;
        }

        // Store current content before clearing
        String originalContent = txtDisplay.getText();
        txtDisplay.clear();

        int number = Integer.parseInt(txtMembers.getText());

        if (selectedFile != null && !isManualInput) {
            // Process from file if a file was uploaded
            if (selectedFile.getName().endsWith(".txt")) {
                // Handle text file grouping
                processTextFile(number);
            } else if (selectedFile.getName().endsWith(".xlsx") || selectedFile.getName().endsWith(".xls")) {
                // Handle Excel file grouping
                processExcelFile(number);
            }
        } else {
            // Process manually entered names
            processManualInput(originalContent, number);
        }

        // Update button states after processing
        updateButtonStates();
    }

    // New method to process manually entered names
    private void processManualInput(String content, int number) {
        // Clear previous data
        data.clear();

        // Split the content by line breaks
        String[] lines = content.split("\n");

        // Add non-empty lines to the data list
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                data.add(line.trim());
            }
        }

        Random rand = new Random();
        int groupNum = 1;

        // Create a copy of the data list to avoid duplicates
        List<String> remainingData = new ArrayList<>(data);

        // Loop through to get all names
        while (remainingData.size() >= number) {
            List<String> selected = new ArrayList<>();

            // Select people randomly
            for (int i = 0; i < number; i++) {
                int index = rand.nextInt(remainingData.size());
                selected.add(remainingData.get(index));
                remainingData.remove(index);
            }

            // Append to text display
            txtDisplay.appendText("Group " + groupNum + ":\n");
            for (String s : selected) {
                txtDisplay.appendText(s + "\n");
            }
            txtDisplay.appendText("\n");

            groupNum++;
        }

        // Handle remaining members if any
        if (!remainingData.isEmpty()) {
            txtDisplay.appendText("Remaining (not enough for a full group):\n");
            for (String s : remainingData) {
                txtDisplay.appendText(s + "\n");
            }
        }

        // Update character count
        lblCharacters.setText("Character count: " + txtDisplay.getText().length());
    }

    // Method to process text file for grouping
    private void processTextFile(int number) throws FileNotFoundException {
        int groupNum = 1;
        data.clear(); // Clear previous data

        // Getting the data from the file
        inputFile = new Scanner(selectedFile);
        while (inputFile.hasNextLine()) {
            String line = inputFile.nextLine();
            if (!line.trim().isEmpty()) {
                data.add(line);
            }
        }

        Random rand = new Random();

        // Create a copy of the data list to avoid duplicates
        List<String> remainingData = new ArrayList<>(data);

        // Loop through to get all names
        while (remainingData.size() >= number) {
            List<String> selected = new ArrayList<>();

            // Select people randomly
            for (int i = 0; i < number; i++) {
                int index = rand.nextInt(remainingData.size());
                selected.add(remainingData.get(index));
                remainingData.remove(index);
            }

            // Append to text display
            txtDisplay.appendText("Group " + groupNum + ":\n");
            for (String s : selected) {
                txtDisplay.appendText(s + "\n");
            }
            txtDisplay.appendText("\n");

            groupNum++;
        }

        // Handle remaining members if any
        if (!remainingData.isEmpty()) {
            txtDisplay.appendText("Remaining (not enough for a full group):\n");
            for (String s : remainingData) {
                txtDisplay.appendText(s + "\n");
            }
        }

        inputFile.close();

        // Update character count after processing
        lblCharacters.setText("Character count: " + txtDisplay.getText().length());
    }

    // Method to process Excel file for grouping
    private void processExcelFile(int number) throws IOException {
        // Use the selectedFile path directly

        // Read from Excel file
        List<String> studentNames = readStudentNamesFromExcel(selectedFile);

        // Shuffle the names
        Collections.shuffle(studentNames);

        // Create groups
        List<List<String>> studentGroups = createStudentGroups(studentNames, number);

        // Display groups in the text area
        displayStudentGroups(studentGroups);

        // Update character count after processing
        lblCharacters.setText("Character count: " + txtDisplay.getText().length());
    }

    // Modified to read from the selected file
    private List<String> readStudentNamesFromExcel(File file) throws IOException {
        List<String> studentNames = new ArrayList<>();

        // Read excel file
        FileInputStream fileInputStream = new FileInputStream(file);
        Workbook workbook;

        // Create appropriate workbook based on file extension
        if (file.getName().endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(fileInputStream);
        } else {
            workbook = new HSSFWorkbook(fileInputStream);
        }

        Sheet sheet = workbook.getSheetAt(0);

        // Iterate through rows
        for (Row row : sheet) {
            // Skip if row is null
            if (row == null) continue;

            // Get the first cell
            Cell cell = row.getCell(0);

            // Skip if cell is null
            if (cell == null) continue;

            // Get cell value based on type
            String studentName;
            switch (cell.getCellType()) {
                case STRING:
                    studentName = cell.getStringCellValue();
                    break;
                case NUMERIC:
                    studentName = String.valueOf(cell.getNumericCellValue());
                    break;
                default:
                    continue; // Skip non-string/non-numeric cells
            }

            // Add name to the list if not empty
            if (studentName != null && !studentName.trim().isEmpty()) {
                studentNames.add(studentName);
            }
        }

        workbook.close();
        fileInputStream.close();
        return studentNames;
    }

    // Display groups in the text area instead of console
    private void displayStudentGroups(List<List<String>> studentGroups) {
        for (int i = 0; i < studentGroups.size(); i++) {
            List<String> group = studentGroups.get(i);
            txtDisplay.appendText("Group " + (i + 1) + ":\n");
            for (String studentName : group) {
                txtDisplay.appendText(studentName + "\n");
            }
            txtDisplay.appendText("\n");
        }
    }

    // Fixed integer division issue
    private List<List<String>> createStudentGroups(List<String> studentNames, int studentPerGroup) {
        List<List<String>> studentGroups = new ArrayList<>();

        // Calculate number of groups (fixing the integer division issue)
        int numberOfGroups = (int) Math.ceil((double) studentNames.size() / studentPerGroup);

        for (int i = 0; i < numberOfGroups; i++) {
            int startIndex = i * studentPerGroup;
            int endIndex = Math.min(startIndex + studentPerGroup, studentNames.size());
            List<String> group = new ArrayList<>(studentNames.subList(startIndex, endIndex));
            studentGroups.add(group);
        }
        return studentGroups;
    }
}
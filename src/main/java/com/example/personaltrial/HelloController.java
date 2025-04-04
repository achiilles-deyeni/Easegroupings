package com.example.personaltrial;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class HelloController {

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

    //    Declaring a dialogbox
    FileChooser dialogbox = new FileChooser();

    @FXML
    void onCancel(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    void onClear(ActionEvent event) {
        txtDisplay.clear();
        btnDownload.setDisable(true);
        btnGroup.setDisable(false);
        lblCharacters.setText("Character count: " + txtDisplay.getText().length());
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
            lblCharacters.setText(txtDisplay.getText());
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
    File selectedFile = null;
    Scanner inputFile = null;
    List<String> data = new ArrayList<>();
    PrintWriter outputFile = null;
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

                lblCharacters.setText("Characters: " + selectedFile.length());
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
    void onGroup(ActionEvent event) throws FileNotFoundException {
//        Check to see if the number required to make a group is empty
        if (txtMembers.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setHeaderText("Missing Information");
            alert.setContentText("Enter the number required to make a group");
            alert.showAndWait(); // This displays the alert

//            lblCharacters.setText("Enter Members");
        } else if (selectedFile.getName().endsWith(".txt")) {
            int groupNum = 1;
// Getting the number typed in by the user
            inputFile = new Scanner(selectedFile);
            int number = Integer.parseInt(txtMembers.getText());

            while(inputFile.hasNextLine()) {
                String line = inputFile.nextLine();
                data.add(line);
            }
            Random rand = new Random();

// Create a copy of the data list to avoid duplicates
            List<String> remainingData = new ArrayList<>(data);

// Initialize a print writer
            outputFile = new PrintWriter("Groups.txt");
            txtDisplay.clear(); // Clear display once at the beginning

// Loop through to get all names
            while (remainingData.size() >= number) {
                List<String> selected = new ArrayList<>();

                // Select people randomly
                for (int i = 0; i < number; i++) {
                    int index = rand.nextInt(remainingData.size());
                    selected.add(remainingData.get(index));
                    remainingData.remove(index);
                }

                // Write to file
                outputFile.println("Group " + groupNum + ":");
                for (String s : selected) {
                    outputFile.println(s);
                }
                outputFile.println(); // Add blank line between groups

                // Append to text display
                txtDisplay.appendText("Group " + groupNum + ":\n");
                for (String s : selected) {
                    txtDisplay.appendText(s + "\n");
                }
                txtDisplay.appendText("\n");

                groupNum++;
            }

            inputFile.close();
            outputFile.close();

// If you want to read from the file after writing, do it outside the loop
// Scanner fileReader = new Scanner(new File("Groups.txt"));
// while (fileReader.hasNextLine()) {
//     txtDisplay.appendText(fileReader.nextLine() + "\n");
// }
// fileReader.close();
        }else{

        }
        if(txtDisplay.getText().isEmpty()) {
            btnDownload.setDisable(false);
        }
        btnGroup.setDisable(true);
    }
}



package com.example.personaltrial;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
        // Show the save dialog box and add filters
        dialogbox.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        // Display save dialog instead of open dialog
        File groupedFile = dialogbox.showSaveDialog(new Stage());
        if (groupedFile != null) {
            PrintWriter outputFile = new PrintWriter(groupedFile);
            outputFile.println(txtDisplay.getText());
            txtDisplay.clear();
            lblCharacters.setText(txtDisplay.getText());
            outputFile.close();
        }
    }
    File selectedFile = null;
    Scanner inputFile = null;
    List<String> data = new ArrayList<>();
    PrintWriter outputFile = null;
    @FXML
    void onUpload(ActionEvent event) throws FileNotFoundException {
//        Add filters that point to files that can be selected
        dialogbox.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        selectedFile = dialogbox.showOpenDialog(new Stage());
        if (selectedFile != null) {
//            clear content that was already on the txtDisplay
            txtDisplay.setText("");

//            Loop through and print onto the display
            inputFile = new Scanner(selectedFile);
            while (inputFile.hasNextLine()) {
                txtDisplay.appendText(inputFile.nextLine() + "\n");
            }
            lblCharacters.setText("Characters: " + selectedFile.length());

        }
    }

    @FXML
    void onGroup(ActionEvent event) throws FileNotFoundException {
//        Check to see if the number required to make a group is empty
        if (txtMembers.getText().isEmpty()) {
            lblCharacters.setText("Enter Members");
        } else {
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
        }
        if(txtDisplay.getText().isEmpty()) {
            btnDownload.setDisable(false);
        }
        btnGroup.setDisable(true);
    }
}



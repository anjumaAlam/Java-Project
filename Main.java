package application;
	
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main extends Application {

    private static final int SIZE = 9;
    private TextField[][] cells = new TextField[SIZE][SIZE];
    private final int[][] initialBoard = {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
    };

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Create Sudoku grid
        GridPane grid = createSudokuGrid();

        // Create buttons
        Button solveButton = new Button("Solve");
        solveButton.setOnAction(e -> validateAndSolveSudoku());

        Button showSolutionButton = new Button("Show Solution");
        showSolutionButton.setOnAction(e -> showSolutionWithoutValidation());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearGrid());

        Button saveButton = new Button("Save to File");
        saveButton.setOnAction(e -> saveToFile());

        VBox layout = new VBox(10, grid, solveButton, showSolutionButton, clearButton, saveButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 400, 550);
        stage.setScene(scene);
        stage.setTitle("Sudoku Solver with File Handling");
        stage.show();
    }

    private GridPane createSudokuGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                TextField cell = new TextField();
                cell.setPrefSize(40, 40);
                cell.setAlignment(Pos.CENTER);
                cell.setStyle("-fx-font-size: 16;");

                // Pre-fill cells from initialBoard
                if (initialBoard[row][col] != 0) {
                    cell.setText(String.valueOf(initialBoard[row][col]));
                    cell.setEditable(false); // Prevent editing pre-filled cells
                    cell.setStyle("-fx-font-size: 16; -fx-background-color: lightgray;");
                }

                grid.add(cell, col, row);
                cells[row][col] = cell;
            }
        }

        return grid;
    }

    private void validateAndSolveSudoku() {
        boolean isValid = true;

        // Reset styles for validation
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (initialBoard[row][col] == 0) { // Only validate user inputs
                    cells[row][col].setStyle("-fx-font-size: 16;");
                }
            }
        }

        try {
            int[][] board = getBoardFromGrid();

            // Validate and highlight invalid inputs
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (initialBoard[row][col] == 0 && !isValidPlacement(board, row, col, board[row][col])) {
                        cells[row][col].setStyle("-fx-font-size: 16; -fx-background-color: red;");
                        isValid = false;
                    }
                }
            }

            if (isValid) {
                if (SudokuSolver.solve(board)) {
                    setGridFromBoard(board);
                    showAlert(AlertType.INFORMATION, "Solved", "The Sudoku has been solved!");
                } else {
                    showAlert(AlertType.WARNING, "Unsolvable", "The Sudoku puzzle cannot be solved.");
                }
            } else {
                showAlert(AlertType.ERROR, "Invalid Inputs", "Some inputs are invalid. Correct them to proceed.");
            }
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Invalid Input", "Please enter numbers between 1 and 9 only.");
        }
    }

    private void showSolutionWithoutValidation() {
        int[][] board = new int[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col] = initialBoard[row][col];
            }
        }

        if (SudokuSolver.solve(board)) {
            setGridFromBoard(board);
            showAlert(AlertType.INFORMATION, "Solution", "Here is the solution to the Sudoku.");
        } else {
            showAlert(AlertType.WARNING, "No Solution", "The Sudoku puzzle has no solution.");
        }
    }

    private boolean isValidPlacement(int[][] board, int row, int col, int num) {
        if (num == 0) return true; // Empty cells are always valid
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == num && i != col) return false; // Check row
            if (board[i][col] == num && i != row) return false; // Check column
            if (board[row / 3 * 3 + i / 3][col / 3 * 3 + i % 3] == num && (row != row / 3 * 3 + i / 3 || col != col / 3 * 3 + i % 3)) return false; // Check box
        }
        return true;
    }

    private void clearGrid() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (initialBoard[row][col] == 0) {
                    cells[row][col].setText(""); // Clear only non-pre-filled cells
                    cells[row][col].setStyle("-fx-font-size: 16;"); // Reset styles
                }
            }
        }
    }

    private int[][] getBoardFromGrid() throws NumberFormatException {
        int[][] board = new int[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String text = cells[row][col].getText().trim();
                if (!text.isEmpty()) {
                    int value = Integer.parseInt(text);
                    if (value < 1 || value > 9) {
                        throw new NumberFormatException("Invalid number at row " + (row + 1) + ", column " + (col + 1));
                    }
                    board[row][col] = value;
                } else {
                    board[row][col] = 0;
                }
            }
        }
        return board;
    }

    private void setGridFromBoard(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (initialBoard[row][col] == 0) { // Update only non-pre-filled cells
                    cells[row][col].setText(board[row][col] == 0 ? "" : String.valueOf(board[row][col]));
                    cells[row][col].setStyle("-fx-font-size: 16;"); // Keep default style
                }
            }
        }
    }

    private void saveToFile() {
        String filePath = "C:\\Users\\User\\OneDrive\\Desktop\\Project\\sudoku_output.txt"; // Replace YourUsername with your actual username
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    String value = cells[row][col].getText().isEmpty() ? "0" : cells[row][col].getText();
                    writer.write(value + (col == SIZE - 1 ? "" : " "));
                }
                writer.newLine();
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "The Sudoku grid has been saved to: " + filePath);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save the Sudoku grid: " + e.getMessage());
        }
    }


    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

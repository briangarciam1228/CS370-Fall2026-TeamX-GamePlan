import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MainWindow extends Application {

    private static class Task{
        private final String name;
        private final LocalDate dueDate;
        private final int points;

    Task(String taskName, LocalDate taskDate, int taskPoints){
        name = taskName;
        dueDate = taskDate;
        points = taskPoints;
    }

    @Override
        public String toString(){
        return name + " | Due: " + dueDate + " | Points: " + points;
    }
    }
    @Override
    public void start(Stage stage) {
        // Main window layout
        BorderPane root = new BorderPane();

        // Title
        Label titleLabel = new Label("Welcome to GamePlan!");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        VBox topSection = new VBox(titleLabel);
        topSection.setAlignment(Pos.CENTER);
        topSection.setPadding(new Insets(20));
        root.setTop(topSection);

        //task list
        Label taskLabel = new Label("Your tasks will appear here.");
        //display tasks
        ListView<Task> taskList = new ListView<>();
        taskList.setPlaceholder(taskLabel);

        VBox centerSection = new VBox(taskList);
        centerSection.setAlignment(Pos.CENTER);
        VBox.setVgrow(taskList, Priority.ALWAYS);
        root.setCenter(centerSection);

        // Buttons (functionality will be added later)
        Button addButton = new Button("Add Task");
        Button editButton = new Button("Edit Task");
        Button deleteButton = new Button("Delete Task");

        addButton.setOnAction(event -> {
            //new window to add task
            Stage addTaskWindow = new Stage();
            addTaskWindow.setTitle("Add Task");
            addTaskWindow.initOwner(stage);
            addTaskWindow.setResizable(false);

            //fields to input
            TextField nameField = new TextField();
            DatePicker dueDateField = new DatePicker();
            TextField pointsField = new TextField();
            GridPane fields = new GridPane();
            fields.setHgap(10);
            fields.setVgap(10);
            fields.addRow(0, new Label("Task Name:"), nameField);
            fields.addRow(1, new Label("Due Date:"), dueDateField);
            fields.addRow(2, new Label("Points:"), pointsField);

            //buttons to save and cancel - add task
            Button saveButton = new Button("Save Task");
            Button cancelButton = new Button("Cancel");

            cancelButton.setOnAction(e -> addTaskWindow.close());

            // Save Task functionality
            saveButton.setOnAction(e -> {

                String taskName = nameField.getText().trim();
                LocalDate taskDate = dueDateField.getValue();

                int taskPoints;

                try {
                    taskPoints = Integer.parseInt(pointsField.getText().trim());
                }
                catch (NumberFormatException ex) {
                    taskPoints = -1;
                }

                // Check for invalid input
                if (taskName.isEmpty() || taskDate == null || taskPoints < 0) {

                    Alert alert = new Alert( Alert.AlertType.WARNING, "Please enter a task name, due date, and points.");

                    alert.showAndWait();
                    return;
                }

                // Add task to home page
                taskList.getItems().add(new Task(taskName, taskDate, taskPoints));

                // Close the Add Task window
                addTaskWindow.close();
            });

            // Arrange popup buttons
            HBox buttons = new HBox(10, saveButton, cancelButton);
            buttons.setAlignment(Pos.CENTER_RIGHT);

            // Popup window layout
            VBox popupLayout = new VBox(15, fields, buttons);
            popupLayout.setPadding(new Insets(20));

            // Display popup window
            Scene popupScene = new Scene(popupLayout, 420, 230);
            addTaskWindow.setScene(popupScene);
            addTaskWindow.show();
        });




        HBox buttonPanel = new HBox(10, addButton, deleteButton, editButton);
        buttonPanel.setAlignment(Pos.CENTER_LEFT);
        buttonPanel.setPadding(new Insets(0, 10, 0, 10)); //edited padding to align buttons order is (top, right, bot, left)

        //topSection.getChildren().add(buttonPanel); replacing this to align the buttons at top and align SortLevelExp
        BorderPane Menu = new BorderPane(); //make top menu

        Menu.setLeft(buttonPanel);
        Menu.setRight(new SortLevelExp());
        topSection.getChildren().add(0, Menu);

        // Display window
        Scene scene = new Scene(root, 800, 500);
        stage.setTitle("GamePlan - Task Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

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
        Button settingsButton = new Button("Settings");

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

        // Edit Task functionality
        editButton.setOnAction(event -> {
            Task selectedTask = taskList.getSelectionModel().getSelectedItem();

            if (selectedTask == null) {
                Alert alert = new Alert(
                    Alert.AlertType.WARNING,
                    "Please select a task to edit!"
                );
                alert.showAndWait();
                return;
            }

            // Open Edit Task window
            Stage editTaskWindow = new Stage();
            editTaskWindow.setTitle("Edit Task");
            editTaskWindow.initOwner(stage);
            editTaskWindow.setResizable(false);

            // Fields to edit the selected task
            TextField editNameField = new
            TextField(selectedTask.name);
            DatePicker editDueDateField = new
            DatePicker(selectedTask.dueDate);
            TextField editPointsField = new
            TextField(String.valueOf(selectedTask.points));

            // Arrange the edit fields
            GridPane editFields = new GridPane();
            editFields.setHgap(10);
            editFields.setVgap(10);

            editFields.addRow(0, new Label("Task Name:"), editNameField);
            editFields.addRow(1, new Label("Due Date:"), editDueDateField);
            editFields.addRow(2, new Label("Points:"), editPointsField);

            // Buttons for Edit Task window
            Button saveEditButton = new Button("Save Changes");
            Button cancelEditButton = new Button ("Cancel");

            // Close the window when Cancel is clicked
            cancelEditButton.setOnAction(e -> editTaskWindow.close());

            // Save the edited task
            saveEditButton.setOnAction(e -> {
                String newName = editNameField.getText().trim();
                LocalDate newDate = editDueDateField.getValue();
                int newPoints;

                try {
                    newPoints = Integer.parseInt(editPointsField.getText().trim());
                } catch (NumberFormatException ex) {
                    newPoints = -1;
                }   

                // Check for invalid input
                if (newName.isEmpty() || newDate == null || newPoints < 0) {
                    Alert alert = new Alert(
                        Alert.AlertType.WARNING,
                        "Please enter a task name, due date, and valid points."
                    );
                    alert.showAndWait();
                    return;
                }

                // Replace the old task with the edited task
                int index = taskList.getItems().indexOf(selectedTask);
                taskList.getItems().set(index, new Task(newName, newDate, newPoints));

                editTaskWindow.close();
            });

            // Arrange the buttons
            HBox editButtons = new HBox(10, saveEditButton, cancelEditButton);
            editButtons.setAlignment(Pos.CENTER_RIGHT);

            // Edit Task window layout
            VBox editLayout = new VBox(15, editFields, editButtons);
            editLayout.setPadding(new Insets(20));

            // Display Edit Task window
            Scene editScene = new Scene(editLayout, 420, 230);
            editTaskWindow.setScene(editScene);
            editTaskWindow.show();

             }); // End of Edit Task fuctionality

        // Delete Task functionality
        deleteButton.setOnAction(event -> {
            Task selectedTask = taskList.getSelectionModel().getSelectedItem();

            // Verify that a task is selected, if not; warn user to select one
            if (selectedTask == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a task to delete!");
                alert.showAndWait();
                return;
            }

            // Remove task from home page
            taskList.getItems().remove(selectedTask);
        });


        HBox buttonPanel = new HBox(10, addButton, deleteButton, editButton, settingsButton);
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

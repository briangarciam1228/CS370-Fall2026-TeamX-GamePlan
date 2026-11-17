import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import java.util.ArrayDeque;
import java.util.Deque;

public class MainWindow extends Application {

    // A removed task, where it was in the list, and why it was removed
    private record RemovedTask(Task task, int index, String action) {}

    // Most recent removal is on top, so undo restores newest first
    private final Deque<RemovedTask> undoStack = new ArrayDeque<>();
    private final Button undoButton = new Button("Undo");

    static class Task{
        private final String name;
        private final LocalDate dueDate;
        private final int points;
        private final String description; // optional, empty if none given

        Task(String taskName, LocalDate taskDate, int taskPoints){
            this(taskName, taskDate, taskPoints, "");
        }

        Task(String taskName, LocalDate taskDate, int taskPoints, String taskDescription){
            name = taskName;
            dueDate = taskDate;
            points = taskPoints;
            description = taskDescription == null ? "" : taskDescription.trim();
        }


        public String getName() {
            return name;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public int getPoints() {
            return points;
        }

        public String getDescription() {
            return description;
        }

        public boolean hasDescription() {
            return !description.isEmpty();
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

        // Each row shows a checkbox and the task text; checking it "completes" the task
        taskList.setCellFactory(list -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();
            private final Label label = new Label();
            private final Label descriptionLabel = new Label();
            private final VBox text = new VBox(2, label, descriptionLabel);
            private final HBox row = new HBox(10, checkBox, text);

            {
                row.setAlignment(Pos.CENTER_LEFT);
                descriptionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");
                descriptionLabel.setWrapText(true);
                // Hidden descriptions take up no space
                descriptionLabel.managedProperty().bind(descriptionLabel.visibleProperty());
                checkBox.setOnAction(e -> {
                    Task task = getItem();
                    if (task != null && checkBox.isSelected()) {
                        removeTask(getListView(), task, "Complete");
                    }
                });
            }

            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                setText(null);
                if (empty || task == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(false); // cells are reused, so reset the box
                    label.setText(task.toString());
                    descriptionLabel.setText(task.getDescription());
                    descriptionLabel.setVisible(task.hasDescription());
                    setGraphic(row);
                }
            }
        });

        VBox centerSection = new VBox(taskList);
        centerSection.setAlignment(Pos.CENTER);
        VBox.setVgrow(taskList, Priority.ALWAYS);
        root.setCenter(centerSection);

        // Buttons (functionality will be added later)
        Button addButton = new Button("Add Task");
        Button editButton = new Button("Edit Task");
        Button deleteButton = new Button("Delete Task");
        Button settingsButton = new Button("Settings");
        Button calendarButton = new Button("Calendar");

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
            TextArea descriptionField = new TextArea();
            descriptionField.setPromptText("Optional: add any extra details");
            descriptionField.setPrefRowCount(3);
            descriptionField.setPrefColumnCount(20); // default is 40, too wide for the window
            descriptionField.setWrapText(true);
            GridPane fields = new GridPane();
            fields.setHgap(10);
            fields.setVgap(10);
            setupFormColumns(fields);
            fields.addRow(0, new Label("Task Name:"), nameField);
            fields.addRow(1, new Label("Due Date:"), dueDateField);
            fields.addRow(2, new Label("Points:"), pointsField);
            fields.addRow(3, new Label("Description:"), descriptionField);

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
                taskList.getItems().add(new Task(taskName, taskDate, taskPoints, descriptionField.getText()));

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
            Scene popupScene = new Scene(popupLayout, 420, 250);
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
            TextArea editDescriptionField = new TextArea(selectedTask.description);
            editDescriptionField.setPromptText("Optional: add any extra details");
            editDescriptionField.setPrefRowCount(3);
            editDescriptionField.setPrefColumnCount(20); // default is 40, too wide for the window
            editDescriptionField.setWrapText(true);

            // Arrange the edit fields
            GridPane editFields = new GridPane();
            editFields.setHgap(10);
            editFields.setVgap(10);

            setupFormColumns(editFields);
            editFields.addRow(0, new Label("Task Name:"), editNameField);
            editFields.addRow(1, new Label("Due Date:"), editDueDateField);
            editFields.addRow(2, new Label("Points:"), editPointsField);
            editFields.addRow(3, new Label("Description:"), editDescriptionField);

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
                taskList.getItems().set(index, new Task(newName, newDate, newPoints, editDescriptionField.getText()));

                editTaskWindow.close();
            });

            // Arrange the buttons
            HBox editButtons = new HBox(10, saveEditButton, cancelEditButton);
            editButtons.setAlignment(Pos.CENTER_RIGHT);

            // Edit Task window layout
            VBox editLayout = new VBox(15, editFields, editButtons);
            editLayout.setPadding(new Insets(20));

            // Display Edit Task window
            Scene editScene = new Scene(editLayout, 420, 250);
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

            // Remove task from home page (can be undone)
            removeTask(taskList, selectedTask, "Delete");
        });

// Create the calendar window
        Stage calendarWindow = new Stage();
        calendarWindow.setTitle("GamePlan - Calendar");
        calendarWindow.initOwner(stage);

// Connect calendar to our existing task list
        TaskCalendar taskCalendar = new TaskCalendar(taskList.getItems());

// Create the calendar scene
        Scene calendarScene = new Scene(taskCalendar, 1000, 700);
        calendarWindow.setScene(calendarScene);

// Open the calendar when the button is clicked
        calendarButton.setOnAction(e -> {
            calendarWindow.show();
            calendarWindow.toFront();
        });


        // Undo the most recent delete/complete
        undoButton.setOnAction(e -> undo(taskList));
        updateUndoButton(); // starts disabled

        HBox buttonPanel = new HBox(
                10,
                addButton,
                deleteButton,
                editButton,
                undoButton,
                settingsButton,
                calendarButton
        );
        buttonPanel.setAlignment(Pos.CENTER_LEFT);
        buttonPanel.setPadding(new Insets(0, 10, 0, 10)); //edited padding to align buttons order is (top, right, bot, left)

        //topSection.getChildren().add(buttonPanel); replacing this to align the buttons at top and align SortLevelExp
        BorderPane Menu = new BorderPane(); //make top menu

        Menu.setLeft(buttonPanel);
        Menu.setRight(new SortLevelExp());
        topSection.getChildren().add(0, Menu);

        // Display window
        Scene scene = new Scene(root, 800, 500);
        // Cmd+Z on Mac, Ctrl+Z on Windows/Linux
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN),
                () -> undo(taskList));
        stage.setTitle("GamePlan - Task Manager");
        stage.setScene(scene);
        stage.show();
    }

    // Label column never shrinks (so labels don't turn into "..."); field column fills the rest
    private static void setupFormColumns(GridPane grid) {
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);
        fieldColumn.setFillWidth(true);
        grid.getColumnConstraints().addAll(labelColumn, fieldColumn);
    }

    // Remove a task and remember it so it can be undone
    private void removeTask(ListView<Task> taskList, Task task, String action) {
        int index = taskList.getItems().indexOf(task);
        if (index < 0) return;
        undoStack.push(new RemovedTask(task, index, action));
        taskList.getItems().remove(index);
        updateUndoButton();
    }

    // Put the most recently removed task back where it was
    private void undo(ListView<Task> taskList) {
        RemovedTask last = undoStack.poll();
        if (last == null) return;
        // Clamp in case the list got shorter since the removal
        int index = Math.min(last.index(), taskList.getItems().size());
        taskList.getItems().add(index, last.task());
        taskList.getSelectionModel().select(index);
        updateUndoButton();
    }

    // Disable the button when there's nothing to undo; otherwise say what it will undo
    private void updateUndoButton() {
        RemovedTask last = undoStack.peek();
        undoButton.setDisable(last == null);
        undoButton.setText(last == null ? "Undo" : "Undo");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class MainWindow extends Application {

    // A removed task, where it was in the list, and why it was removed
    private record RemovedTask(Task task, int index, String action) {}

    // Most recent removal is on top, so undo restores newest first
    private final Deque<RemovedTask> undoStack = new ArrayDeque<>();
    private final Button undoButton = new Button("Undo");

    // How due dates are shown in the task table, e.g. "Nov 21, 2026"
    private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    //Creates a array of finished tasks
    private final ObservableList<Task> completedTasks =
            FXCollections.observableArrayList();

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

        // Title on top, then the menu bar (buttons and level) underneath it
        VBox topSection = new VBox(10, titleLabel);
        topSection.setAlignment(Pos.CENTER);
        topSection.setPadding(new Insets(15));
        root.setTop(topSection);

        //task list
        Label taskLabel = new Label("Your tasks will appear here.");
        //display tasks in a table; click a column header to sort by it
        TableView<Task> taskList = new TableView<>();
        taskList.setPlaceholder(taskLabel);
        // Columns share the table's width, so there's no blank space or sideways scrolling
        taskList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Checkbox column: checking the box "completes" the task
        TableColumn<Task, Task> doneColumn = new TableColumn<>("");
        doneColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        doneColumn.setSortable(false);
        doneColumn.setReorderable(false);
        doneColumn.setResizable(false);
        doneColumn.setMinWidth(40);
        doneColumn.setMaxWidth(40);
        doneColumn.setCellFactory(column -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                setAlignment(Pos.CENTER);
                checkBox.setOnAction(e -> {
                    Task task = getItem();
                    if (task != null && checkBox.isSelected()) {
                        // Ask the user to confirm before completing the task
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                                "Are you sure you want to complete the task?",
                                ButtonType.YES, ButtonType.NO);
                        confirm.setTitle("Complete Task");
                        confirm.setHeaderText(null);
                        boolean confirmed = confirm.showAndWait()
                                .filter(response -> response == ButtonType.YES)
                                .isPresent();
                        if (confirmed) {
                            removeTask(getTableView(), task, "Complete");
                        } else {
                            checkBox.setSelected(false); // user cancelled, so uncheck the box
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                checkBox.setSelected(false); // cells are reused, so reset the box
                setGraphic(empty || task == null ? null : checkBox);
            }
        });

        // Task name column, with the optional description in gray underneath; sorts A-Z by name
        TableColumn<Task, Task> nameColumn = new TableColumn<>("Task");
        nameColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        nameColumn.setComparator(Comparator.comparing(Task::getName, String.CASE_INSENSITIVE_ORDER));
        nameColumn.setPrefWidth(400);
        nameColumn.setCellFactory(column -> new TableCell<>() {
            private final Label nameLabel = new Label();
            // A Text node (not a wrapping Label) so the row is only as tall as the wrapped lines;
            // a wrapping Label in a table cell gets measured as one character per line
            private final Text descriptionLabel = new Text();
            private final VBox text = new VBox(2, nameLabel, descriptionLabel);

            {
                descriptionLabel.setFont(Font.font(11));
                descriptionLabel.setFill(Color.GRAY);
                // Wrap long descriptions to the column's width (minus the cell's padding)
                descriptionLabel.wrappingWidthProperty().bind(column.widthProperty().subtract(16));
                // Hidden descriptions take up no space
                descriptionLabel.managedProperty().bind(descriptionLabel.visibleProperty());
            }

            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(task.getName());
                    descriptionLabel.setText(task.getDescription());
                    descriptionLabel.setVisible(task.hasDescription());
                    setGraphic(text);
                }
            }
        });

        // Due date column; sorts by date (earliest first)
        TableColumn<Task, LocalDate> dueColumn = new TableColumn<>("Due Date");
        dueColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getDueDate()));
        dueColumn.setPrefWidth(120);
        dueColumn.setMaxWidth(160);
        dueColumn.setStyle("-fx-alignment: CENTER;");
        dueColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(DUE_DATE_FORMAT));
            }
        });

        // Points column; sorts numerically
        TableColumn<Task, Integer> pointsColumn = new TableColumn<>("Points");
        pointsColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getPoints()));
        pointsColumn.setPrefWidth(80);
        pointsColumn.setMaxWidth(100);
        pointsColumn.setStyle("-fx-alignment: CENTER;");

        taskList.getColumns().addAll(List.of(doneColumn, nameColumn, dueColumn, pointsColumn));

        VBox centerSection = new VBox(taskList);
        centerSection.setAlignment(Pos.CENTER);
        VBox.setVgrow(taskList, Priority.ALWAYS);
        root.setCenter(centerSection);

        // Buttons
        Button addButton = new Button("Add Task");
        Button editButton = new Button("Edit Task");
        Button deleteButton = new Button("Delete Task");
        Button settingsButton = new Button("Settings");
        Button calendarButton = new Button("Calendar");
        Button completeButton = new Button("Complete");

        //Complete button action
        completeButton.setOnAction(e -> {
            Stage completedWindow = new Stage();
            completedWindow.setTitle("GamePlan - Completed Tasks");
            completedWindow.initOwner(stage);

            CompletedTasksWindow completedView =
                    new CompletedTasksWindow(completedTasks);

            completedWindow.setScene(new Scene(completedView, 600, 400));
            completedWindow.show();
        });

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
            descriptionField.setPromptText("Optional: Add any extra details");
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
                taskList.sort(); // put the new task in the right spot if a column is sorted

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
                taskList.sort(); // edited values may change where the task belongs

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

        // Task buttons, centered under the title
        HBox taskButtons = new HBox(10, addButton, deleteButton, editButton, undoButton);
        taskButtons.setAlignment(Pos.CENTER);
        taskButtons.setMaxWidth(Region.USE_PREF_SIZE); // only as wide as the buttons, so it stays centered

        // Other buttons on the right (Settings and Calendar may move later)
        HBox otherButtons = new HBox(10, completeButton, settingsButton, calendarButton);
        otherButtons.setAlignment(Pos.CENTER_RIGHT);

        // Level/EXP on the left, other buttons on the right
        BorderPane sideItems = new BorderPane();
        SortLevelExp levelExp = new SortLevelExp();
        levelExp.setAlignment(Pos.CENTER_LEFT);
        sideItems.setLeft(levelExp);
        sideItems.setRight(otherButtons);
        BorderPane.setAlignment(levelExp, Pos.CENTER_LEFT);
        BorderPane.setAlignment(otherButtons, Pos.CENTER_RIGHT);
        sideItems.setPickOnBounds(false); // let clicks in its empty middle reach the task buttons

        // Stack the centered task buttons over the side items so the buttons are centered
        // on the whole window, not just on the space between the side items
        StackPane menu = new StackPane(sideItems, taskButtons);
        menu.setPadding(new Insets(0, 10, 0, 10)); // order is (top, right, bottom, left)
        topSection.getChildren().add(menu); // below the title

        // Display window
        Scene scene = new Scene(root, 800, 500);
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
    private void removeTask(TableView<Task> taskList, Task task, String action) {
        int index = taskList.getItems().indexOf(task);
        if (index < 0) return;
        undoStack.push(new RemovedTask(task, index, action));
        // Only completed tasks go into the completed table
        if (action.equals("Complete")) {
            completedTasks.add(task);
        }
        taskList.getItems().remove(index);
        updateUndoButton();
    }

    // Put the most recently removed task back where it was
    private void undo(TableView<Task> taskList) {
        RemovedTask last = undoStack.poll();
        if (last == null) return;
        if (last.action().equals("Complete")) {
            completedTasks.remove(last.task());
        }
        // Clamp in case the list got shorter since the removal
        int index = Math.min(last.index(), taskList.getItems().size());
        taskList.getItems().add(index, last.task());
        taskList.sort(); // keep the current column sort, if any
        taskList.getSelectionModel().select(last.task());
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

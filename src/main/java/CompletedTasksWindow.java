
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.beans.property.ReadOnlyObjectWrapper;

public class CompletedTasksWindow extends BorderPane {

    public CompletedTasksWindow(ObservableList<MainWindow.Task> completedTasks) {

        // Table to display completed tasks
        TableView<MainWindow.Task> table = new TableView<>(completedTasks);

        // Task name
        TableColumn<MainWindow.Task, String> nameColumn =
                new TableColumn<>("Task");

        nameColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getName()));

        // Due date
        TableColumn<MainWindow.Task, Object> dateColumn =
                new TableColumn<>("Due Date");

        dateColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getDueDate()));

        // Points
        TableColumn<MainWindow.Task, Integer> pointsColumn =
                new TableColumn<>("Points");

        pointsColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getPoints()));

        // Add columns to table
        table.getColumns().addAll(nameColumn, dateColumn, pointsColumn);

        // Make columns fit the window
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        setCenter(table);
    }
}

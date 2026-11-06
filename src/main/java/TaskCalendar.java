
package com.example.lab5;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;

import javafx.collections.ObservableList;
import javafx.scene.layout.BorderPane;

public class TaskCalendar extends BorderPane {

    // Calendar that stores our tasks
    private final Calendar taskCalendar = new Calendar("My Tasks");

    public TaskCalendar(ObservableList<MainWindow.Task> tasks) {

        // Create the CalendarFX display
        CalendarView calendarView = new CalendarView();

        // Prevent users from directly changing calendar entries
        taskCalendar.setReadOnly(true);

        // Create a source to hold our task calendar
        CalendarSource source = new CalendarSource("GamePlan");

        source.getCalendars().add(taskCalendar);

        // Connect our source to the calendar display
        calendarView.getCalendarSources().add(source);

        // Open the calendar in month view
        calendarView.showMonthPage();

        // Place the calendar in this BorderPane
        setCenter(calendarView);

        // Display any tasks already in the list
        updateTasks(tasks);

        // Refresh calendar whenever the task list changes
        tasks.addListener(
                (javafx.collections.ListChangeListener<MainWindow.Task>)
                        change -> updateTasks(tasks)
        );
    }

    // Refresh all tasks in the calendar
    private void updateTasks(ObservableList<MainWindow.Task> tasks) {

        // Remove old calendar entries
        taskCalendar.clear();

        // Add each task to its due date
        for (MainWindow.Task task : tasks) {

            // Create entry with task name and points
            Entry<?> entry = new Entry<>(
                    task.getName() + " (" + task.getPoints() + " pts)"
            );

            // Set the due date
            entry.setInterval(task.getDueDate());

            // Task lasts all day
            entry.setFullDay(true);

            // Add task to calendar
            taskCalendar.addEntry(entry);
        }
    }
}

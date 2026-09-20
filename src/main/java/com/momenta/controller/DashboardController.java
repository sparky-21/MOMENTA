package com.momenta.controller;

import com.momenta.model.Task;
import com.momenta.service.TaskService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.time.LocalDateTime;

/**
 * Controller for dashboard.fxml.
 *
 * RULE FOLLOWED HERE: this class contains NO SQL and NO business logic
 * (like "what counts as overdue"). It only:
 *   - reads values out of FXML controls
 *   - asks TaskService to do the real work
 *   - puts the result back into FXML controls
 *
 * That's what "keep controllers thin" means in practice.
 */
public class DashboardController {

    // --- injected from dashboard.fxml (fx:id must match these names) ---
    @FXML private TextField titleField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private Spinner<Integer> importanceSpinner;
    @FXML private DatePicker deadlinePicker;
    @FXML private Button addTaskButton;

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> categoryColumn;
    @FXML private TableColumn<Task, Number> importanceColumn;
    @FXML private TableColumn<Task, String> statusColumn;
    @FXML private TableColumn<Task, Void> actionColumn;

    @FXML private Label taskCountLabel;

    private final TaskService taskService = new TaskService();

    /** JavaFX calls this automatically right after the FXML is loaded. */
    @FXML
    public void initialize() {
        categoryBox.getItems().addAll(
                "Work", "Personal", "Study", "Family", "Finance", "Health", "Project", "Other");
        categoryBox.getSelectionModel().selectFirst();

        importanceSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 3));

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        importanceColumn.setCellValueFactory(new PropertyValueFactory<>("importance"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        addDoneButtonColumn();
        refreshTasks();
    }

    @FXML
    public void onAddTask() {
        String title = titleField.getText();
        if (title == null || title.isBlank()) {
            return; // Phase-2+ will replace this with a proper validation dialog
        }

        LocalDateTime deadline = deadlinePicker.getValue() != null
                ? deadlinePicker.getValue().atTime(23, 59)
                : null;

        Task task = new Task(
                title,
                categoryBox.getValue(),
                importanceSpinner.getValue(),
                deadline,
                30 // default estimated minutes for now
        );

        taskService.createTaskAsync(task, savedTask -> {
            taskTable.getItems().add(savedTask);
            updateTaskCount();
            titleField.clear();
        });
    }

    private void refreshTasks() {
        taskService.loadAllTasksAsync(this::setTasks);
    }

    private void setTasks(ObservableList<Task> tasks) {
        taskTable.setItems(tasks);
        updateTaskCount();
    }

    private void updateTaskCount() {
        long remaining = taskTable.getItems().stream().filter(t -> !t.isDone()).count();
        taskCountLabel.setText(remaining + " task(s) remaining");
    }

    /** Adds a "Done" button inside each table row — a common TableView pattern. */
    private void addDoneButtonColumn() {
        Callback<TableColumn<Task, Void>, TableCell<Task, Void>> cellFactory = column -> new TableCell<>() {
            private final Button doneButton = new Button("Done");
            {
                doneButton.setStyle(
                        "-fx-background-color: #34d399; -fx-text-fill: #06251a; -fx-font-weight: bold; -fx-background-radius: 6;");
                doneButton.setOnAction(e -> {
                    Task task = getTableView().getItems().get(getIndex());
                    taskService.markCompleteAsync(task, () -> {
                        taskTable.refresh();
                        updateTaskCount();
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Task task = getTableView().getItems().get(getIndex());
                    setGraphic(task.isDone() ? null : doneButton);
                }
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }
}

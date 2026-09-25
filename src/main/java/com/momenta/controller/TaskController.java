package com.momenta.controller;

import com.momenta.model.Goal;
import com.momenta.model.Project;
import com.momenta.model.Task;
import com.momenta.service.GoalService;
import com.momenta.service.ProjectService;
import com.momenta.service.TaskService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.AlertUtil;
import com.momenta.utility.SceneManager;
import com.momenta.utility.CurrentUser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

/**
 * Controller for Tasks.fxml (Section 6 — Task Management).
 *
 * Follows the same controller-is-thin rule as DashboardController: this
 * class only wires UI events to TaskService calls and updates the
 * ObservableList bound to the TableView. All persistence lives in
 * TaskDAOImpl, all scoring logic lives in PriorityEngine.
 */
public class TaskController {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> categoryColumn;
    @FXML private TableColumn<Task, String> deadlineColumn;
    @FXML private TableColumn<Task, Number> progressColumn;
    @FXML private TableColumn<Task, Number> priorityColumn;
    @FXML private TableColumn<Task, String> statusColumn;

    private final TaskService taskService = new TaskService();
    private final GoalService goalService = new GoalService();
    private final ProjectService projectService = new ProjectService();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObservableList<Goal> availableGoals = FXCollections.observableArrayList();
    private final ObservableList<Project> availableProjects = FXCollections.observableArrayList();

    private static final List<String> CATEGORIES =
            List.of("Work", "Personal", "Study", "Family", "Finance", "Health", "Project", "Other");

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priorityScore"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        taskTable.setItems(tasks);
        loadTasks();
    }

    /** Background load, same JavaFX Task pattern as DashboardController. */
    private void loadTasks() {
        javafx.concurrent.Task<Object[]> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected Object[] call() {
                List<Task> taskList = taskService.getAllTasks(CurrentUser.getId());
                List<Goal> goals = goalService.getAllGoals(CurrentUser.getId());
                List<Project> projectList = projectService.getAllProjects(CurrentUser.getId());
                return new Object[]{taskList, goals, projectList};
            }
        };
        loadTask.setOnSucceeded(e -> {
            Object[] result = loadTask.getValue();
            @SuppressWarnings("unchecked")
            List<Task> taskList = (List<Task>) result[0];
            @SuppressWarnings("unchecked")
            List<Goal> goals = (List<Goal>) result[1];
            @SuppressWarnings("unchecked")
            List<Project> projectList = (List<Project>) result[2];

            tasks.setAll(taskList);
            availableGoals.setAll(goals);
            availableProjects.setAll(projectList);
        });
        loadTask.setOnFailed(e ->
                AlertUtil.showError("Load failed", "Could not load tasks from the database.",
                        loadTask.getException()));

        TaskExecutor.getInstance().workPool().submit(loadTask);
    }

    @FXML
    private void onAddTask() {
        Optional<Task> result = showTaskDialog(null);
        result.ifPresent(task -> runInBackground(
                () -> taskService.createTask(task),
                saved -> {
                    tasks.add(0, saved);
                    SceneManager.getInstance().invalidate("Dashboard");
                    SceneManager.getInstance().invalidate("Goals");
                    SceneManager.getInstance().invalidate("Projects");
                }
        ));
    }

    @FXML
    private void onEditTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("No task selected", "Select a task from the table first.");
            return;
        }
        Optional<Task> result = showTaskDialog(selected);
        result.ifPresent(task -> runInBackground(
                () -> { taskService.updateTask(task); return task; },
                updated -> {
                    taskTable.refresh();
                    SceneManager.getInstance().invalidate("Dashboard");
                    SceneManager.getInstance().invalidate("Goals");
                    SceneManager.getInstance().invalidate("Projects");
                }
        ));
    }

    @FXML
    private void onCompleteTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("No task selected", "Select a task from the table first.");
            return;
        }
        runInBackground(
                () -> { taskService.completeTask(selected); return selected; },
                t -> {
                    taskTable.refresh();
                    // Goal/Project progress and Dashboard's MOMENTA NOW / task
                    // counts all depend on this task's status, so force them
                    // to reload next time they're opened.
                    SceneManager.getInstance().invalidate("Dashboard");
                    SceneManager.getInstance().invalidate("Goals");
                    SceneManager.getInstance().invalidate("Projects");
                }
        );
    }

    @FXML
    private void onDeleteTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("No task selected", "Select a task from the table first.");
            return;
        }
        if (!AlertUtil.confirm("Delete task", "Delete \"" + selected.getTitle() + "\"? This cannot be undone.")) {
            return;
        }
        runInBackground(
                () -> { taskService.deleteTask(selected.getId()); return selected; },
                deleted -> {
                    tasks.remove(deleted);
                    SceneManager.getInstance().invalidate("Dashboard");
                    SceneManager.getInstance().invalidate("Goals");
                    SceneManager.getInstance().invalidate("Projects");
                }
        );
    }

    @FXML
    private void onBackToDashboard() {
        SceneManager.getInstance().invalidate("Dashboard");
        SceneManager.getInstance().switchTo("Dashboard");
    }

    /**
     * Runs {@code work} on the shared background pool and applies
     * {@code onDone} back on the JavaFX Application Thread — the same
     * background -> foreground handoff pattern used throughout MOMENTA.
     */
    private <T> void runInBackground(java.util.concurrent.Callable<T> work, java.util.function.Consumer<T> onDone) {
        javafx.concurrent.Task<T> bgTask = new javafx.concurrent.Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        bgTask.setOnSucceeded(e -> onDone.accept(bgTask.getValue()));
        bgTask.setOnFailed(e -> AlertUtil.showError("Operation failed",
                "Something went wrong while saving. Please try again.", bgTask.getException()));
        TaskExecutor.getInstance().workPool().submit(bgTask);
    }

    /**
     * A simple modal dialog built entirely from JavaFX controls (no CSS,
     * no FXML — Dialog + GridPane is enough for a form this small and it
     * keeps the create/edit logic in one place).
     */
    private Optional<Task> showTaskDialog(Task existing) {
        boolean isEdit = existing != null;
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Task" : "New Task");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField(isEdit ? existing.getTitle() : "");
        titleField.setPromptText("Task title");

        TextArea descriptionArea = new TextArea(isEdit ? existing.getDescription() : "");
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);

        ComboBox<String> categoryBox = new ComboBox<>(FXCollections.observableArrayList(CATEGORIES));
        categoryBox.setValue(isEdit ? existing.getCategory() : "Other");

        Spinner<Integer> importanceSpinner = new Spinner<>(1, 5, isEdit ? existing.getImportance() : 3);

        DatePicker deadlinePicker = new DatePicker();
        if (isEdit && existing.getDeadline() != null && !existing.getDeadline().isBlank()) {
            deadlinePicker.setValue(java.time.LocalDate.parse(existing.getDeadline()));
        }

        Spinner<Integer> minutesSpinner = new Spinner<>(5, 600, isEdit ? existing.getEstimatedMinutes() : 30, 5);

        Spinner<Integer> progressSpinner = new Spinner<>(0, 100, isEdit ? existing.getProgress() : 0, 5);

        ComboBox<Project> projectBox = new ComboBox<>(FXCollections.observableArrayList(availableProjects));
        projectBox.setPromptText("No project");
        if (isEdit && existing.getProjectId() != null) {
            availableProjects.stream().filter(p -> p.getId() == existing.getProjectId())
                    .findFirst().ifPresent(projectBox::setValue);
        }

        ComboBox<Goal> goalBox = new ComboBox<>(FXCollections.observableArrayList(availableGoals));
        goalBox.setPromptText("No goal");
        if (isEdit && existing.getGoalId() != null) {
            availableGoals.stream().filter(g -> g.getId() == existing.getGoalId())
                    .findFirst().ifPresent(goalBox::setValue);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Title"), titleField);
        grid.addRow(1, new Label("Description"), descriptionArea);
        grid.addRow(2, new Label("Category"), categoryBox);
        grid.addRow(3, new Label("Importance (1-5)"), importanceSpinner);
        grid.addRow(4, new Label("Deadline"), deadlinePicker);
        grid.addRow(5, new Label("Estimated minutes"), minutesSpinner);
        grid.addRow(6, new Label("Progress (%)"), progressSpinner);
        grid.addRow(7, new Label("Project"), projectBox);
        grid.addRow(8, new Label("Goal"), goalBox);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) return null;
            if (titleField.getText() == null || titleField.getText().isBlank()) {
                AlertUtil.showInfo("Title required", "Please enter a task title.");
                return null;
            }
            Task task = isEdit ? existing : new Task();
            task.setTitle(titleField.getText().trim());
            task.setDescription(descriptionArea.getText());
            task.setCategory(categoryBox.getValue());
            task.setImportance(importanceSpinner.getValue());
            task.setDeadline(deadlinePicker.getValue() == null ? "" : deadlinePicker.getValue().toString());
            task.setEstimatedMinutes(minutesSpinner.getValue());
            task.setProgress(progressSpinner.getValue());
            task.setProjectId(projectBox.getValue() == null ? null : projectBox.getValue().getId());
            task.setGoalId(goalBox.getValue() == null ? null : goalBox.getValue().getId());
            if (!isEdit) {
                task.setStatus("PENDING");
            } else if (task.getProgress() == 100) {
                task.setStatus("COMPLETED");
            }
            return task;
        });

        return dialog.showAndWait();
    }
}

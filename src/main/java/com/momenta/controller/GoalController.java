package com.momenta.controller;

import com.momenta.model.Goal;
import com.momenta.service.GoalService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.AlertUtil;
import com.momenta.utility.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

/**
 * Controller for Goals.fxml (Section 7).
 *
 * Goals are shown as a flat TableView with a "Parent Goal" column rather
 * than a nested TreeView — the hierarchy (Life -> Year -> Monthly ->
 * Project) still exists via parentGoalId, it's just displayed flat for
 * Phase 5. A TreeTableView upgrade is a clean, isolated later change since
 * nothing about GoalService or GoalDAO would need to move.
 */
public class GoalController {

    @FXML private TableView<Goal> goalTable;
    @FXML private TableColumn<Goal, String> titleColumn;
    @FXML private TableColumn<Goal, String> parentColumn;
    @FXML private TableColumn<Goal, String> deadlineColumn;
    @FXML private TableColumn<Goal, Number> progressColumn;
    @FXML private TableColumn<Goal, Number> importanceColumn;
    @FXML private TableColumn<Goal, String> statusColumn;

    private final GoalService goalService = new GoalService();
    private final ObservableList<Goal> goals = FXCollections.observableArrayList();
    private static final int CURRENT_USER_ID = 1;

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        importanceColumn.setCellValueFactory(new PropertyValueFactory<>("importance"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Parent Goal column shows the parent's title, looked up from the
        // currently loaded list rather than a stored DB column — simple and
        // always up to date since 'goals' is refreshed on every load.
        parentColumn.setCellValueFactory(data -> {
            Integer parentId = data.getValue().getParentGoalId();
            String parentTitle = parentId == null ? "—" :
                    goals.stream().filter(g -> g.getId() == parentId)
                            .findFirst().map(Goal::getTitle).orElse("—");
            return new javafx.beans.property.SimpleStringProperty(parentTitle);
        });

        goalTable.setItems(goals);
        loadGoals();
    }

    private void loadGoals() {
        javafx.concurrent.Task<List<Goal>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Goal> call() {
                goalService.recalculateAllProgress(CURRENT_USER_ID);
                return goalService.getAllGoals(CURRENT_USER_ID);
            }
        };
        loadTask.setOnSucceeded(e -> {
            goals.setAll(loadTask.getValue());
            goalTable.refresh(); // re-resolve parent-title lookups now that 'goals' is populated
        });
        loadTask.setOnFailed(e -> AlertUtil.showError("Load failed",
                "Could not load goals from the database.", loadTask.getException()));

        TaskExecutor.getInstance().workPool().submit(loadTask);
    }

    @FXML
    private void onAddGoal() {
        showGoalDialog(null).ifPresent(goal -> runInBackground(
                () -> goalService.createGoal(goal),
                saved -> goals.add(0, saved)
        ));
    }

    @FXML
    private void onEditGoal() {
        Goal selected = goalTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("No goal selected", "Select a goal from the table first.");
            return;
        }
        showGoalDialog(selected).ifPresent(goal -> runInBackground(
                () -> { goalService.updateGoal(goal); return goal; },
                updated -> goalTable.refresh()
        ));
    }

    @FXML
    private void onDeleteGoal() {
        Goal selected = goalTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("No goal selected", "Select a goal from the table first.");
            return;
        }
        if (!AlertUtil.confirm("Delete goal", "Delete \"" + selected.getTitle() +
                "\"? Sub-goals will become top-level, and linked tasks/projects keep their data but lose this link.")) {
            return;
        }
        runInBackground(
                () -> { goalService.deleteGoal(selected.getId()); return selected; },
                deleted -> {
                    goals.remove(deleted);
                    SceneManager.getInstance().invalidate("Dashboard");
                }
        );
    }

    @FXML
    private void onBackToDashboard() {
        SceneManager.getInstance().invalidate("Dashboard");
        SceneManager.getInstance().switchTo("Dashboard");
    }

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

    private Optional<Goal> showGoalDialog(Goal existing) {
        boolean isEdit = existing != null;
        Dialog<Goal> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Goal" : "New Goal");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField(isEdit ? existing.getTitle() : "");
        titleField.setPromptText("Goal title");

        TextArea descriptionArea = new TextArea(isEdit ? existing.getDescription() : "");
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);

        // Parent goal picker — "No parent (Life Goal)" plus every other goal
        // except the one being edited (a goal can't be its own parent).
        ComboBox<Goal> parentBox = new ComboBox<>();
        Goal noParent = null; // sentinel represented by null selection
        ObservableList<Goal> parentOptions = FXCollections.observableArrayList(goals);
        if (isEdit) parentOptions.removeIf(g -> g.getId() == existing.getId());
        parentBox.setItems(parentOptions);
        parentBox.setPromptText("No parent (top-level / Life Goal)");
        if (isEdit && existing.getParentGoalId() != null) {
            parentOptions.stream().filter(g -> g.getId() == existing.getParentGoalId())
                    .findFirst().ifPresent(parentBox::setValue);
        }

        Spinner<Integer> importanceSpinner = new Spinner<>(1, 5, isEdit ? existing.getImportance() : 3);

        DatePicker deadlinePicker = new DatePicker();
        if (isEdit && existing.getDeadline() != null && !existing.getDeadline().isBlank()) {
            deadlinePicker.setValue(java.time.LocalDate.parse(existing.getDeadline()));
        }

        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "PAUSED", "ACHIEVED"));
        statusBox.setValue(isEdit ? existing.getStatus() : "ACTIVE");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Title"), titleField);
        grid.addRow(1, new Label("Description"), descriptionArea);
        grid.addRow(2, new Label("Parent goal"), parentBox);
        grid.addRow(3, new Label("Importance (1-5)"), importanceSpinner);
        grid.addRow(4, new Label("Deadline"), deadlinePicker);
        grid.addRow(5, new Label("Status"), statusBox);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) return null;
            if (titleField.getText() == null || titleField.getText().isBlank()) {
                AlertUtil.showInfo("Title required", "Please enter a goal title.");
                return null;
            }
            Goal goal = isEdit ? existing : new Goal();
            goal.setTitle(titleField.getText().trim());
            goal.setDescription(descriptionArea.getText());
            goal.setParentGoalId(parentBox.getValue() == null ? null : parentBox.getValue().getId());
            goal.setImportance(importanceSpinner.getValue());
            goal.setDeadline(deadlinePicker.getValue() == null ? "" : deadlinePicker.getValue().toString());
            goal.setStatus(statusBox.getValue());
            return goal;
        });

        return dialog.showAndWait();
    }
}

package com.momenta.controller;

import com.momenta.model.Goal;
import com.momenta.model.Project;
import com.momenta.service.GoalService;
import com.momenta.service.ProjectService;
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
 * Controller for Projects.fxml (Section 8).
 *
 * The Progress column always reflects live task completion — ProjectService
 * computes it fresh on every load, so unlike Task/Goal there's nothing to
 * "keep in sync" here.
 */
public class ProjectController {

    @FXML private TableView<Project> projectTable;
    @FXML private TableColumn<Project, String> titleColumn;
    @FXML private TableColumn<Project, String> goalColumn;
    @FXML private TableColumn<Project, String> deadlineColumn;
    @FXML private TableColumn<Project, Number> progressColumn;
    @FXML private TableColumn<Project, String> statusColumn;

    private final ProjectService projectService = new ProjectService();
    private final GoalService goalService = new GoalService();
    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final ObservableList<Goal> availableGoals = FXCollections.observableArrayList();
    private static final int CURRENT_USER_ID = 1;

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        goalColumn.setCellValueFactory(data -> {
            Integer goalId = data.getValue().getGoalId();
            String goalTitle = goalId == null ? "—" :
                    availableGoals.stream().filter(g -> g.getId() == goalId)
                            .findFirst().map(Goal::getTitle).orElse("—");
            return new javafx.beans.property.SimpleStringProperty(goalTitle);
        });

        projectTable.setItems(projects);
        loadProjects();
    }

    private void loadProjects() {
        javafx.concurrent.Task<Object[]> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected Object[] call() {
                List<Goal> goals = goalService.getAllGoals(CURRENT_USER_ID);
                List<Project> projectList = projectService.getAllProjects(CURRENT_USER_ID);
                return new Object[]{goals, projectList};
            }
        };
        loadTask.setOnSucceeded(e -> {
            Object[] result = loadTask.getValue();
            @SuppressWarnings("unchecked")
            List<Goal> goals = (List<Goal>) result[0];
            @SuppressWarnings("unchecked")
            List<Project> projectList = (List<Project>) result[1];

            availableGoals.setAll(goals);
            projects.setAll(projectList);
            projectTable.refresh();
        });
        loadTask.setOnFailed(e -> AlertUtil.showError("Load failed",
                "Could not load projects from the database.", loadTask.getException()));

        TaskExecutor.getInstance().workPool().submit(loadTask);
    }

    @FXML
    private void onAddProject() {
        showProjectDialog(null).ifPresent(project -> runInBackground(
                () -> projectService.createProject(project),
                saved -> { projects.add(0, saved); projectTable.refresh(); }
        ));
    }

    @FXML
    private void onEditProject() {
        Project selected = projectTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("No project selected", "Select a project from the table first.");
            return;
        }
        showProjectDialog(selected).ifPresent(project -> runInBackground(
                () -> { projectService.updateProject(project); return project; },
                updated -> projectTable.refresh()
        ));
    }

    @FXML
    private void onDeleteProject() {
        Project selected = projectTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("No project selected", "Select a project from the table first.");
            return;
        }
        if (!AlertUtil.confirm("Delete project", "Delete \"" + selected.getTitle() +
                "\"? Linked tasks keep their data but lose this project link.")) {
            return;
        }
        runInBackground(
                () -> { projectService.deleteProject(selected.getId()); return selected; },
                deleted -> {
                    projects.remove(deleted);
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

    private Optional<Project> showProjectDialog(Project existing) {
        boolean isEdit = existing != null;
        Dialog<Project> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Project" : "New Project");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField(isEdit ? existing.getTitle() : "");
        titleField.setPromptText("Project title");

        TextArea descriptionArea = new TextArea(isEdit ? existing.getDescription() : "");
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);

        ComboBox<Goal> goalBox = new ComboBox<>(FXCollections.observableArrayList(availableGoals));
        goalBox.setPromptText("No linked goal");
        if (isEdit && existing.getGoalId() != null) {
            availableGoals.stream().filter(g -> g.getId() == existing.getGoalId())
                    .findFirst().ifPresent(goalBox::setValue);
        }

        DatePicker deadlinePicker = new DatePicker();
        if (isEdit && existing.getDeadline() != null && !existing.getDeadline().isBlank()) {
            deadlinePicker.setValue(java.time.LocalDate.parse(existing.getDeadline()));
        }

        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "PAUSED", "COMPLETED"));
        statusBox.setValue(isEdit ? existing.getStatus() : "ACTIVE");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Title"), titleField);
        grid.addRow(1, new Label("Description"), descriptionArea);
        grid.addRow(2, new Label("Linked goal"), goalBox);
        grid.addRow(3, new Label("Deadline"), deadlinePicker);
        grid.addRow(4, new Label("Status"), statusBox);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) return null;
            if (titleField.getText() == null || titleField.getText().isBlank()) {
                AlertUtil.showInfo("Title required", "Please enter a project title.");
                return null;
            }
            Project project = isEdit ? existing : new Project();
            project.setTitle(titleField.getText().trim());
            project.setDescription(descriptionArea.getText());
            project.setGoalId(goalBox.getValue() == null ? null : goalBox.getValue().getId());
            project.setDeadline(deadlinePicker.getValue() == null ? "" : deadlinePicker.getValue().toString());
            project.setStatus(statusBox.getValue());
            return project;
        });

        return dialog.showAndWait();
    }
}

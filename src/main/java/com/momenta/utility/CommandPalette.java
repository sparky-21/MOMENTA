package com.momenta.utility;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Phase 21 — Ctrl+K command palette.
 *
 * The palette is installed once per Scene by SceneManager. It uses JavaFX
 * keyboard events and navigates through the existing SceneManager, so no
 * controller-specific navigation logic is duplicated.
 */
public final class CommandPalette {

    private static final String INSTALLED_KEY = "momenta.commandPalette.installed";

    private CommandPalette() {
    }

    public static void install(Scene scene) {
        if (scene == null || Boolean.TRUE.equals(scene.getProperties().get(INSTALLED_KEY))) {
            return;
        }

        scene.getProperties().put(INSTALLED_KEY, Boolean.TRUE);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.K && event.isControlDown()) {
                event.consume();
                show(scene);
            }
        });
    }

    private static void show(Scene scene) {
        List<Command> commands = createCommands();
        ObservableList<Command> filtered = FXCollections.observableArrayList(commands);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("MOMENTA Command Palette");
        dialog.initModality(Modality.WINDOW_MODAL);

        Window owner = scene.getWindow();
        if (owner != null) {
            dialog.initOwner(owner);
        }

        TextField search = new TextField();
        search.setPromptText("Type a command...");
        search.setFocusTraversable(true);

        ListView<Command> list = new ListView<>(filtered);
        list.setPrefSize(560, 360);
        list.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Command item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name() + "    —    " + item.shortcut());
                }
            }
        });

        Label hint = new Label("↑ ↓ to move   •   Enter to run   •   Esc to close");

        VBox content = new VBox(10, search, list, hint);
        content.setPrefWidth(580);
        content.setPadding(new javafx.geometry.Insets(16));

        MomentaTheme.apply(content, "Dashboard");

        DialogPane pane = dialog.getDialogPane();
        pane.setContent(content);
        pane.getButtonTypes().clear();
        pane.setHeaderText("What do you want to do?");

        Runnable executeSelected = () -> {
            Command selected = list.getSelectionModel().getSelectedItem();
            if (selected != null) {
                dialog.close();
                selected.action().run();
            }
        };

        ChangeListener<String> filterListener = (obs, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase(Locale.ROOT);

            List<Command> matches = new ArrayList<>();
            for (Command command : commands) {
                if (query.isBlank()
                        || command.name().toLowerCase(Locale.ROOT).contains(query)
                        || command.shortcut().toLowerCase(Locale.ROOT).contains(query)) {
                    matches.add(command);
                }
            }

            filtered.setAll(matches);
            if (!matches.isEmpty()) {
                list.getSelectionModel().select(0);
            }
        };

        search.textProperty().addListener(filterListener);

        search.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DOWN) {
                list.requestFocus();
                if (!list.getItems().isEmpty()) {
                    list.getSelectionModel().select(0);
                }
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                executeSelected.run();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                dialog.close();
                event.consume();
            }
        });

        list.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                executeSelected.run();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                dialog.close();
                event.consume();
            }
        });

        list.getSelectionModel().selectFirst();

        dialog.setOnShown(event -> search.requestFocus());
        dialog.showAndWait();
    }

    private static List<Command> createCommands() {
        return List.of(
                new Command("Create Task", "Open task manager", () -> open("Tasks")),
                new Command("Create Goal", "Open goal manager", () -> open("Goals")),
                new Command("Create Project", "Open project manager", () -> open("Projects")),
                new Command("Start Focus Session", "Open Focus Mode", () -> open("Focus")),
                new Command("Add Expense", "Open finance manager", () -> open("Finance")),
                new Command("Open Calendar", "Open calendar", () -> open("Calendar")),
                new Command("View Analytics", "Open analytics", () -> open("Analytics")),
                new Command("Search Tasks", "Open tasks for searching", () -> open("Tasks")),
                new Command("Open Settings", "Personalize MOMENTA", () -> open("Settings"))
        );
    }

    private static void open(String view) {
        SceneManager.getInstance().invalidate(view);
        SceneManager.getInstance().switchTo(view);
    }

    private record Command(String name, String shortcut, Runnable action) {
    }
}

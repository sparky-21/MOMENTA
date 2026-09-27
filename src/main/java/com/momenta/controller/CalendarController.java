package com.momenta.controller;

import com.momenta.model.Event;
import com.momenta.model.Task;
import com.momenta.service.EventService;
import com.momenta.service.TaskService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.AlertUtil;
import com.momenta.utility.SceneManager;
import com.momenta.utility.CurrentUser;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Google-Calendar-style month view for MOMENTA Phase 6.
 *
 * The controller loads existing Tasks and Events, groups them by date and
 * renders a 7 x 6 month grid. Events are editable from the calendar itself;
 * tasks are read from the existing Phase 5 task module and shown as deadlines.
 */
public class CalendarController {

    @FXML
    private Label monthLabel;

    @FXML
    private Label selectedDateLabel;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private BorderPane calendarPane;


    private final EventService eventService = new EventService();

    private final TaskService taskService = new TaskService();

    private static final DateTimeFormatter HEADER_FORMAT =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);


    private YearMonth displayedMonth = YearMonth.now();

    private LocalDate selectedDate = LocalDate.now();


    private List<Event> allEvents = new ArrayList<>();

    private List<Task> allTasks = new ArrayList<>();


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        updateSelectedDateLabel();

        loadMonthData();
    }


    // =========================================================
    // PREVIOUS MONTH
    // =========================================================

    @FXML
    private void onPreviousMonth() {

        displayedMonth = displayedMonth.minusMonths(1);

        selectedDate = displayedMonth.atDay(1);

        updateSelectedDateLabel();

        loadMonthData();
    }


    // =========================================================
    // NEXT MONTH
    // =========================================================

    @FXML
    private void onNextMonth() {

        displayedMonth = displayedMonth.plusMonths(1);

        selectedDate = displayedMonth.atDay(1);

        updateSelectedDateLabel();

        loadMonthData();
    }


    // =========================================================
    // TODAY
    // =========================================================

    @FXML
    private void onToday() {

        displayedMonth = YearMonth.now();

        selectedDate = LocalDate.now();

        updateSelectedDateLabel();

        loadMonthData();
    }


    // =========================================================
    // ADD EVENT
    // =========================================================

    @FXML
    private void onAddEvent() {

        Event event =
                showEventDialog(null, selectedDate);

        if (event == null) {
            return;
        }


        javafx.concurrent.Task<Void> saveTask =
                new javafx.concurrent.Task<>() {

                    @Override
                    protected Void call() {

                        eventService.createEvent(event);

                        return null;
                    }
                };


        saveTask.setOnSucceeded(e ->
                loadMonthData());


        saveTask.setOnFailed(e ->
                AlertUtil.showError(
                        "Save Failed",
                        "Could not save event.",
                        saveTask.getException()));


        TaskExecutor.getInstance()
                .workPool()
                .submit(saveTask);
    }


    // =========================================================
    // LOAD MONTH DATA
    // =========================================================

    private void loadMonthData() {

        String monthText =
                displayedMonth.toString();


        javafx.concurrent.Task<CalendarData> loadTask =
                new javafx.concurrent.Task<>() {

                    @Override
                    protected CalendarData call() {

                        /*
                         * Phase 5 already provides findAll()
                         * for both modules.
                         *
                         * So all existing data is loaded once
                         * and then grouped by date.
                         */

                        List<Event> events =
                                eventService.getAllEvents(
                                        CurrentUser.getId());


                        List<Task> tasks =
                                taskService.getAllTasks(
                                        CurrentUser.getId());


                        return new CalendarData(
                                events,
                                tasks);
                    }
                };


        loadTask.setOnSucceeded(e -> {

            CalendarData data =
                    loadTask.getValue();


            allEvents =
                    data.events();


            allTasks =
                    data.tasks();


            renderCalendar();
        });


        loadTask.setOnFailed(e ->
                AlertUtil.showError(
                        "Calendar Error",
                        "Could not load calendar data.",
                        loadTask.getException()));


        monthLabel.setText(
                YearMonth.parse(monthText)
                        .format(HEADER_FORMAT));


        TaskExecutor.getInstance()
                .workPool()
                .submit(loadTask);
    }


    // =========================================================
    // RENDER CALENDAR
    // =========================================================

    private void renderCalendar() {

        monthLabel.setText(
                displayedMonth.format(
                        HEADER_FORMAT));


        calendarGrid.getChildren().clear();


        addWeekHeaders();


        LocalDate firstDay =
                displayedMonth.atDay(1);


        /*
         * Sunday = 0
         * Monday = 1
         * ...
         * Saturday = 6
         */

        int firstColumn =
                firstDay.getDayOfWeek().getValue() % 7;


        LocalDate gridStart =
                firstDay.minusDays(firstColumn);


        Map<LocalDate, List<Event>> eventsByDate =
                allEvents.stream()

                        .filter(e ->
                                e.getEventDate() != null
                                        &&
                                        !e.getEventDate().isBlank())

                        .collect(Collectors.groupingBy(
                                e -> LocalDate.parse(
                                        e.getEventDate())));


        Map<LocalDate, List<Task>> tasksByDate =
                allTasks.stream()

                        .filter(t ->
                                t.getDeadline() != null
                                        &&
                                        !t.getDeadline().isBlank())

                        .collect(Collectors.groupingBy(
                                t -> LocalDate.parse(
                                        t.getDeadline())));


        /*
         * 6 rows × 7 columns = 42 cells
         */

        for (int row = 0; row < 6; row++) {

            for (int col = 0; col < 7; col++) {

                LocalDate date =
                        gridStart.plusDays(
                                row * 7L + col);


                VBox cell =
                        createDayCell(
                                date,
                                eventsByDate,
                                tasksByDate);


                calendarGrid.add(
                        cell,
                        col,
                        row + 1);
            }
        }


        updateSelectedDateLabel();
    }


    // =========================================================
    // WEEK HEADERS
    // =========================================================

    private void addWeekHeaders() {

        for (int col = 0; col < 7; col++) {

            DayOfWeek day =
                    DayOfWeek.of(
                            col == 0 ? 7 : col);


            String name =
                    day.getDisplayName(
                            TextStyle.SHORT,
                            Locale.ENGLISH);


            Label label =
                    new Label(name);


            label.setMaxWidth(
                    Double.MAX_VALUE);


            label.setAlignment(
                    Pos.CENTER);


            label.setPadding(
                    new Insets(8));


            label.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 13));
            label.setTextFill(com.momenta.utility.MomentaTheme.SKY_BLUE);
            label.setBackground(new Background(new BackgroundFill(
                    com.momenta.utility.MomentaTheme.SURFACE_3, new CornerRadii(8), Insets.EMPTY)));


            calendarGrid.add(
                    label,
                    col,
                    0);
        }
    }


    // =========================================================
    // CREATE DAY CELL
    // =========================================================

    private VBox createDayCell(
            LocalDate date,
            Map<LocalDate, List<Event>> eventsByDate,
            Map<LocalDate, List<Task>> tasksByDate) {


        VBox cell =
                new VBox(4);


        cell.setPadding(
                new Insets(7));


        cell.setPrefHeight(100);

        cell.setMinHeight(100);

        cell.setMaxWidth(
                Double.MAX_VALUE);


        boolean currentMonth =
                YearMonth.from(date)
                        .equals(displayedMonth);


        boolean today =
                date.equals(LocalDate.now());


        boolean selected =
                date.equals(selectedDate);


        Color background = currentMonth
                ? com.momenta.utility.MomentaTheme.SURFACE_2
                : com.momenta.utility.MomentaTheme.SURFACE;
        Color border = selected
                ? com.momenta.utility.MomentaTheme.SKY_BLUE
                : com.momenta.utility.MomentaTheme.BORDER_SOFT;
        double borderWidth = selected ? 2 : 1;

        cell.setBackground(new Background(new BackgroundFill(
                background, new CornerRadii(6), Insets.EMPTY)));
        cell.setBorder(new Border(new BorderStroke(
                border, BorderStrokeStyle.SOLID, new CornerRadii(6),
                new BorderWidths(borderWidth))));


        // -----------------------------------------------------
        // DAY NUMBER
        // -----------------------------------------------------

        Label dayNumber =
                new Label(
                        String.valueOf(
                                date.getDayOfMonth()));


        dayNumber.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 13));
        dayNumber.setTextFill(today ? com.momenta.utility.MomentaTheme.VOID : com.momenta.utility.MomentaTheme.TEXT_2);
        if (today) {
            dayNumber.setBackground(new Background(new BackgroundFill(
                    com.momenta.utility.MomentaTheme.SKY_BLUE, new CornerRadii(10), Insets.EMPTY)));
        }


        if (today) {

            dayNumber.setPadding(
                    new Insets(
                            3,
                            6,
                            3,
                            6));
        }


        cell.getChildren().add(
                dayNumber);


        // -----------------------------------------------------
        // EVENTS
        // -----------------------------------------------------

        List<Event> events =
                eventsByDate
                        .getOrDefault(
                                date,
                                List.of())
                        .stream()

                        .sorted(
                                Comparator.comparing(
                                        Event::getStartTime,
                                        Comparator.nullsFirst(
                                                String::compareTo)))

                        .collect(
                                Collectors.toList());


        // -----------------------------------------------------
        // TASKS
        // -----------------------------------------------------

        List<Task> tasks =
                tasksByDate
                        .getOrDefault(
                                date,
                                List.of());


        int shown = 0;

        int maxItems = 4;


        // -----------------------------------------------------
        // EVENT BUTTONS
        // -----------------------------------------------------

        for (Event event : events) {

            if (shown >= maxItems) {
                break;
            }


            Button eventButton =
                    new Button(
                            formatEvent(event));


            eventButton.setMaxWidth(
                    Double.MAX_VALUE);


            eventButton.setAlignment(
                    Pos.CENTER_LEFT);


            eventButton.setPadding(
                    new Insets(
                            3,
                            5,
                            3,
                            5));


            eventButton.setFont(Font.font("System", 11));
            eventButton.setTextFill(com.momenta.utility.MomentaTheme.SKY_BLUE);
            eventButton.setBackground(new Background(new BackgroundFill(
                    com.momenta.utility.MomentaTheme.SURFACE_3, new CornerRadii(6), Insets.EMPTY)));
            eventButton.setBorder(new Border(new BorderStroke(
                    com.momenta.utility.MomentaTheme.SKY_BLUE, BorderStrokeStyle.SOLID,
                    new CornerRadii(6), new BorderWidths(1))));


            eventButton.setOnAction(e -> {

                selectedDate = date;

                updateSelectedDateLabel();

                editEvent(event);
            });


            cell.getChildren().add(
                    eventButton);


            shown++;
        }


        // -----------------------------------------------------
        // TASK LABELS
        // -----------------------------------------------------

        for (Task task : tasks) {

            if (shown >= maxItems) {
                break;
            }


            boolean completed =
                    "COMPLETED".equalsIgnoreCase(task.getStatus());


            // Rule: once a task is completed (i.e. submitted), the calendar
            // should offer a way to remove its deadline entry instead of it
            // sitting there forever. A plain Label had no click handler at
            // all, so completed tasks could never be cleared from here —
            // only from the Tasks screen. Turning this into a Button lets a
            // finished task be deleted right from the calendar cell.
            Button taskButton =
                    new Button(
                            (completed ? "✓ " : "• ")
                                    +
                                    task.getTitle());


            taskButton.setMaxWidth(
                    Double.MAX_VALUE);


            taskButton.setAlignment(
                    Pos.CENTER_LEFT);


            taskButton.setPadding(
                    new Insets(2, 5, 2, 5));


            taskButton.setFont(Font.font("System", 11));
            taskButton.setTextFill(completed
                    ? com.momenta.utility.MomentaTheme.MUTED
                    : com.momenta.utility.MomentaTheme.TEXT_2);
            taskButton.setBackground(new Background(new BackgroundFill(
                    Color.TRANSPARENT, new CornerRadii(6), Insets.EMPTY)));
            taskButton.setBorder(Border.EMPTY);
            taskButton.setCursor(javafx.scene.Cursor.HAND);


            taskButton.setOnAction(e -> {

                selectedDate = date;

                updateSelectedDateLabel();

                if (completed) {

                    if (AlertUtil.confirm(
                            "Delete task",
                            "\"" + task.getTitle()
                                    + "\" is already completed. Remove it from the calendar?")) {

                        deleteTaskFromCalendar(task);
                    }

                } else {

                    AlertUtil.showInfo(
                            "Task not finished yet",
                            "Only completed tasks can be removed from the calendar. "
                                    + "Finish or delete \"" + task.getTitle()
                                    + "\" from the Tasks screen.");
                }
            });


            cell.getChildren().add(
                    taskButton);


            shown++;
        }


        // -----------------------------------------------------
        // MORE ITEMS
        // -----------------------------------------------------

        int totalItems =
                events.size()
                        +
                        tasks.size();


        if (totalItems > shown) {

            Label more =
                    new Label(
                            "+ "
                                    +
                                    (totalItems - shown)
                                    +
                                    " more");


            more.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 10));
            more.setTextFill(com.momenta.utility.MomentaTheme.MUTED);


            cell.getChildren().add(
                    more);
        }


        // -----------------------------------------------------
        // CELL CLICK
        // -----------------------------------------------------

        cell.setOnMouseClicked(mouse -> {

            selectedDate = date;

            updateSelectedDateLabel();


            /*
             * IMPORTANT:
             *
             * Don't call renderCalendar() before checking
             * double click, because that can replace the cell
             * while the mouse event is being processed.
             */

            if (mouse.getButton() == MouseButton.PRIMARY
                    &&
                    mouse.getClickCount() == 2) {


                Event newEvent =
                        showEventDialog(
                                null,
                                date);


                if (newEvent != null) {

                    saveEvent(newEvent);
                }

            } else {

                renderCalendar();
            }
        });


        return cell;
    }


    // =========================================================
    // FORMAT EVENT
    // =========================================================

    private String formatEvent(
            Event event) {

        String time =
                event.getStartTime();


        if (time == null
                ||
                time.isBlank()) {

            return event.getTitle();
        }


        return time
                + "  "
                + event.getTitle();
    }


    // =========================================================
    // EDIT EVENT
    // =========================================================

    private void editEvent(
            Event event) {


        Event updatedEvent =
                showEventDialog(
                        event,
                        selectedDate);


        if (updatedEvent != null) {

            updateEvent(updatedEvent);
        }
    }


    // =========================================================
    // SAVE EVENT
    // =========================================================

    private void saveEvent(
            Event event) {


        javafx.concurrent.Task<Void> task =
                new javafx.concurrent.Task<>() {

                    @Override
                    protected Void call() {

                        eventService.createEvent(
                                event);

                        return null;
                    }
                };


        task.setOnSucceeded(
                e -> loadMonthData());


        task.setOnFailed(
                e -> AlertUtil.showError(
                        "Save Failed",
                        "Could not save event.",
                        task.getException()));


        TaskExecutor.getInstance()
                .workPool()
                .submit(task);
    }


    // =========================================================
    // UPDATE EVENT
    // =========================================================

    private void updateEvent(
            Event event) {


        javafx.concurrent.Task<Void> task =
                new javafx.concurrent.Task<>() {

                    @Override
                    protected Void call() {

                        eventService.updateEvent(
                                event);

                        return null;
                    }
                };


        task.setOnSucceeded(
                e -> loadMonthData());


        task.setOnFailed(
                e -> AlertUtil.showError(
                        "Update Failed",
                        "Could not update event.",
                        task.getException()));


        TaskExecutor.getInstance()
                .workPool()
                .submit(task);
    }


    // =========================================================
    // DELETE EVENT
    // =========================================================

    private void deleteEvent(
            Event event) {


        javafx.concurrent.Task<Void> task =
                new javafx.concurrent.Task<>() {

                    @Override
                    protected Void call() {

                        eventService.deleteEvent(
                                event.getId());

                        return null;
                    }
                };


        task.setOnSucceeded(
                e -> loadMonthData());


        task.setOnFailed(
                e -> AlertUtil.showError(
                        "Delete Failed",
                        "Could not delete event.",
                        task.getException()));


        TaskExecutor.getInstance()
                .workPool()
                .submit(task);
    }


    // =========================================================
    // DELETE TASK (from calendar, completed tasks only)
    // =========================================================

    private void deleteTaskFromCalendar(Task task) {

        javafx.concurrent.Task<Void> task2 =
                new javafx.concurrent.Task<>() {

                    @Override
                    protected Void call() {

                        taskService.deleteTask(task.getId());

                        return null;
                    }
                };


        task2.setOnSucceeded(
                e -> loadMonthData());


        task2.setOnFailed(
                e -> AlertUtil.showError(
                        "Delete Failed",
                        "Could not delete task.",
                        task2.getException()));


        TaskExecutor.getInstance()
                .workPool()
                .submit(task2);
    }


    // =========================================================
    // EVENT DIALOG
    // =========================================================

    private Event showEventDialog(
            Event existing,
            LocalDate defaultDate) {


        boolean edit =
                existing != null;


        Dialog<Event> dialog =
                new Dialog<>();


        dialog.setTitle(
                edit
                        ? "Edit Event"
                        : "New Event");


        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        ButtonType.OK,
                        ButtonType.CANCEL);


        // EventDAO/EventService already implement delete() (Section 20), but
        // no screen ever called it, so an event could be created and edited
        // from the calendar but never removed — an incomplete CRUD chain for
        // this module. Only offer it once there is an existing event to
        // delete.
        ButtonType deleteButtonType =
                new ButtonType("Delete", ButtonBar.ButtonData.LEFT);

        if (edit) {
            dialog.getDialogPane()
                    .getButtonTypes()
                    .add(deleteButtonType);
        }


        TextField titleField =
                new TextField(
                        edit
                                ? existing.getTitle()
                                : "");


        TextArea descriptionArea =
                new TextArea(
                        edit
                                ? existing.getDescription()
                                : "");


        descriptionArea.setPrefRowCount(
                3);


        DatePicker eventDatePicker =
                new DatePicker();


        TextField startField =
                new TextField(
                        edit
                                ? existing.getStartTime()
                                : "");


        TextField endField =
                new TextField(
                        edit
                                ? existing.getEndTime()
                                : "");


        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        if (edit
                &&
                existing.getEventDate() != null
                &&
                !existing.getEventDate().isBlank()) {


            eventDatePicker.setValue(
                    LocalDate.parse(
                            existing.getEventDate()));

        } else {

            eventDatePicker.setValue(
                    defaultDate);
        }


        // -----------------------------------------------------
        // PROMPTS
        // -----------------------------------------------------

        titleField.setPromptText(
                "Event title");


        descriptionArea.setPromptText(
                "Description");


        startField.setPromptText(
                "HH:mm");


        endField.setPromptText(
                "HH:mm");


        // -----------------------------------------------------
        // FORM
        // -----------------------------------------------------

        GridPane form =
                new GridPane();


        form.setHgap(10);

        form.setVgap(10);

        form.setPadding(
                new Insets(12));


        addFormRow(
                form,
                0,
                "Title",
                titleField);


        addFormRow(
                form,
                1,
                "Description",
                descriptionArea);


        addFormRow(
                form,
                2,
                "Date",
                eventDatePicker);


        addFormRow(
                form,
                3,
                "Start time",
                startField);


        addFormRow(
                form,
                4,
                "End time",
                endField);


        dialog.getDialogPane()
                .setContent(form);

        com.momenta.utility.MomentaTheme.styleDialog(dialog.getDialogPane());


        // -----------------------------------------------------
        // DELETE (edit mode only)
        // -----------------------------------------------------

        if (edit) {

            Button deleteButton =
                    (Button) dialog.getDialogPane()
                            .lookupButton(deleteButtonType);

            // A plain ActionEvent filter, consumed, lets us skip the
            // dialog's normal "close and run the result converter" flow so
            // Delete can never be misread as OK by showEventDialog's caller.
            deleteButton.addEventFilter(
                    javafx.event.ActionEvent.ACTION,
                    evt -> {

                        evt.consume();

                        if (AlertUtil.confirm(
                                "Delete event",
                                "Delete \"" + existing.getTitle()
                                        + "\"? This cannot be undone.")) {

                            deleteEvent(existing);
                            dialog.close();
                        }
                    });
        }


        // -----------------------------------------------------
        // RESULT CONVERTER
        // -----------------------------------------------------

        dialog.setResultConverter(
                button -> {


                    if (button != ButtonType.OK) {

                        return null;
                    }


                    // TITLE VALIDATION

                    if (titleField.getText() == null
                            ||
                            titleField.getText()
                                    .isBlank()) {


                        AlertUtil.showInfo(
                                "Title Required",
                                "Please enter an event title.");


                        return null;
                    }


                    // DATE VALIDATION

                    if (eventDatePicker.getValue() == null) {


                        AlertUtil.showInfo(
                                "Date Required",
                                "Please select an event date.");


                        return null;
                    }


                    // CREATE / EDIT EVENT

                    Event event =
                            edit
                                    ? existing
                                    : new Event();


                    event.setUserId(
                            CurrentUser.getId());


                    event.setTitle(
                            titleField
                                    .getText()
                                    .trim());


                    event.setDescription(
                            descriptionArea
                                    .getText()
                                    .trim());


                    event.setEventDate(
                            eventDatePicker
                                    .getValue()
                                    .toString());


                    event.setStartTime(
                            startField
                                    .getText()
                                    .trim());


                    event.setEndTime(
                            endField
                                    .getText()
                                    .trim());


                    return event;
                });


        /*
         * IMPORTANT CORRECTION:
         *
         * dialog.showAndWait() returns Optional<Event>.
         *
         * But this method is declared as Event.
         *
         * Therefore we extract the Event using orElse(null).
         */

        return dialog.showAndWait()
                .orElse(null);
    }


    // =========================================================
    // FORM ROW
    // =========================================================

    private void addFormRow(
            GridPane grid,
            int row,
            String label,
            Node node) {


        grid.add(
                new Label(label),
                0,
                row);


        grid.add(
                node,
                1,
                row);
    }


    // =========================================================
    // SELECTED DATE LABEL
    // =========================================================

    private void updateSelectedDateLabel() {

        selectedDateLabel.setText(
                selectedDate.format(
                        DateTimeFormatter.ofPattern(
                                "EEEE, d MMMM yyyy",
                                Locale.ENGLISH)));
    }


    // =========================================================
    // BACK TO DASHBOARD
    // =========================================================

    @FXML
    private void onBackToDashboard() {

        SceneManager.getInstance()
                .invalidate("Dashboard");


        SceneManager.getInstance()
                .switchTo("Dashboard");
    }


    // =========================================================
    // CALENDAR DATA RECORD
    // =========================================================

    private record CalendarData(
            List<Event> events,
            List<Task> tasks) {
    }
}
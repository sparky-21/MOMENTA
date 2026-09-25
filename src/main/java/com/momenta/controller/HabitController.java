package com.momenta.controller;

import com.momenta.model.Habit;
import com.momenta.service.HabitService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.AlertUtil;
import com.momenta.utility.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.time.LocalDate;
import java.util.Optional;

public class HabitController {
    @FXML private TableView<Habit> habitTable;
    @FXML private TableColumn<Habit,String> nameColumn;
    @FXML private TableColumn<Habit,Number> currentStreakColumn;
    @FXML private TableColumn<Habit,Number> longestStreakColumn;
    @FXML private DatePicker datePicker;
    @FXML private Label selectedStatusLabel;
    @FXML private ProgressBar weeklyProgressBar;
    @FXML private Label weeklyLabel;

    private final HabitService service=new HabitService();
    private final ObservableList<Habit> habits=FXCollections.observableArrayList();
    private static final int CURRENT_USER_ID=1;

    @FXML public void initialize(){
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        currentStreakColumn.setCellValueFactory(new PropertyValueFactory<>("currentStreak"));
        longestStreakColumn.setCellValueFactory(new PropertyValueFactory<>("longestStreak"));
        habitTable.setItems(habits);
        datePicker.setValue(LocalDate.now());
        loadHabits();
    }

    private void loadHabits(){
        javafx.concurrent.Task<java.util.List<Habit>> t=new javafx.concurrent.Task<>(){
            @Override protected java.util.List<Habit> call(){return service.getAllHabits(CURRENT_USER_ID);}
        };
        t.setOnSucceeded(e->{habits.setAll(t.getValue()); updateSelection();});
        t.setOnFailed(e->AlertUtil.showError("Load failed","Could not load habits.",t.getException()));
        TaskExecutor.getInstance().workPool().submit(t);
    }

    @FXML private void onDateChanged(){updateSelection();}
    @FXML private void onAddHabit(){showDialog(null).ifPresent(h->run(() -> service.createHabit(h), saved->{habits.add(saved);habitTable.getSelectionModel().select(saved);updateSelection();}));}
    @FXML private void onEditHabit(){
        Habit h=habitTable.getSelectionModel().getSelectedItem(); if(h==null){AlertUtil.showInfo("No habit selected","Select a habit first.");return;}
        showDialog(h).ifPresent(updated->run(()->{service.updateHabit(updated);return updated;}, x->{habitTable.refresh();updateSelection();}));
    }
    @FXML private void onDeleteHabit(){
        Habit h=habitTable.getSelectionModel().getSelectedItem(); if(h==null){AlertUtil.showInfo("No habit selected","Select a habit first.");return;}
        if(!AlertUtil.confirm("Delete habit","Delete "+h.getName()+" and its history?"))return;
        run(()->{service.deleteHabit(h.getId());return h;}, deleted->{habits.remove(deleted);updateSelection();});
    }
    @FXML private void onToggleToday(){
        Habit h=habitTable.getSelectionModel().getSelectedItem(); if(h==null){AlertUtil.showInfo("No habit selected","Select a habit first.");return;}
        LocalDate d=datePicker.getValue()==null?LocalDate.now():datePicker.getValue();
        run(()->{service.setCompleted(h,d,!service.isCompleted(h,d));return h;}, x->{habitTable.refresh();updateSelection();});
    }
    @FXML private void onSelectHabit(){updateSelection();}
    @FXML private void onBackToDashboard(){SceneManager.getInstance().invalidate("Dashboard");SceneManager.getInstance().switchTo("Dashboard");}

    private void updateSelection(){
        Habit h=habitTable.getSelectionModel().getSelectedItem(); if(h==null){selectedStatusLabel.setText("Select a habit.");weeklyProgressBar.setProgress(0);weeklyLabel.setText("Weekly consistency: —");return;}
        LocalDate d=datePicker.getValue()==null?LocalDate.now():datePicker.getValue();
        boolean done=service.isCompleted(h,d);
        double rate=service.weeklyConsistency(h,d);
        selectedStatusLabel.setText(h.getName()+" on "+d+": "+(done?"Completed":"Not completed"));
        weeklyProgressBar.setProgress(rate); weeklyLabel.setText(String.format("Weekly consistency: %.0f%%",rate*100));
    }
    private Optional<Habit> showDialog(Habit existing){
        boolean edit=existing!=null; Dialog<Habit> dialog=new Dialog<>();dialog.setTitle(edit?"Edit Habit":"New Habit");dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
        TextField name=new TextField(edit?existing.getName():"");name.setPromptText("e.g. Read 20 minutes");
        GridPane grid=new GridPane();grid.setHgap(10);grid.setVgap(10);grid.addRow(0,new Label("Name"),name);dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(b->{if(b!=ButtonType.OK)return null;if(name.getText().isBlank()){AlertUtil.showInfo("Name required","Enter a habit name.");return null;}Habit h=edit?existing:new Habit();h.setUserId(CURRENT_USER_ID);h.setName(name.getText().trim());return h;});
        return dialog.showAndWait();
    }
    private <T> void run(java.util.concurrent.Callable<T> work,java.util.function.Consumer<T> done){
        javafx.concurrent.Task<T> t=new javafx.concurrent.Task<>(){@Override protected T call() throws Exception{return work.call();}};
        t.setOnSucceeded(e->done.accept(t.getValue()));t.setOnFailed(e->AlertUtil.showError("Operation failed","Could not complete the habit operation.",t.getException()));
        TaskExecutor.getInstance().workPool().submit(t);
    }
}

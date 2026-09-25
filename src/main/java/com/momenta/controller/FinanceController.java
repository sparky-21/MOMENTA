package com.momenta.controller;

import com.momenta.model.Expense;
import com.momenta.model.Income;
import com.momenta.service.FinanceService;
import com.momenta.threading.TaskExecutor;
import com.momenta.utility.AlertUtil;
import com.momenta.utility.SceneManager;
import com.momenta.utility.CurrentUser;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.time.LocalDate;
import java.util.Optional;

public class FinanceController {
    @FXML private Label incomeLabel, expenseLabel, balanceLabel, savingsLabel;
    @FXML private TableView<Expense> expenseTable;
    @FXML private TableColumn<Expense,String> expenseDateColumn, expenseCategoryColumn, expenseNoteColumn;
    @FXML private TableColumn<Expense,Number> expenseAmountColumn;
    @FXML private TableView<Income> incomeTable;
    @FXML private TableColumn<Income,String> incomeDateColumn, incomeSourceColumn;
    @FXML private TableColumn<Income,Number> incomeAmountColumn;
    @FXML private PieChart categoryChart;

    private final FinanceService service=new FinanceService();

    @FXML public void initialize(){
        expenseDateColumn.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));expenseCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        expenseNoteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));expenseAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        incomeDateColumn.setCellValueFactory(new PropertyValueFactory<>("incomeDate"));incomeSourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        incomeAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        load();
    }
    private void load(){
        javafx.concurrent.Task<Object[]> t=new javafx.concurrent.Task<>(){
            @Override protected Object[] call(){return new Object[]{service.getExpenses(CurrentUser.getId()),service.getIncome(CurrentUser.getId()),service.expenseByCategory(CurrentUser.getId()),service.totalIncome(CurrentUser.getId()),service.totalExpenses(CurrentUser.getId()),service.savingsRate(CurrentUser.getId())};}
        };
        t.setOnSucceeded(e->{Object[] x=t.getValue();expenseTable.setItems(FXCollections.observableArrayList((java.util.List<Expense>)x[0]));incomeTable.setItems(FXCollections.observableArrayList((java.util.List<Income>)x[1]));double in=(double)x[3],out=(double)x[4];incomeLabel.setText(money(in));expenseLabel.setText(money(out));balanceLabel.setText(money(in-out));savingsLabel.setText(String.format("%.1f%%",(double)x[5]));categoryChart.setData(FXCollections.observableArrayList(((java.util.Map<String,Double>)x[2]).entrySet().stream().map(e2->new PieChart.Data(e2.getKey(),e2.getValue())).toList()));});
        t.setOnFailed(e->AlertUtil.showError("Load failed","Could not load finance data.",t.getException()));TaskExecutor.getInstance().workPool().submit(t);
    }
    @FXML private void onAddExpense(){showExpense(null).ifPresent(x->run(()->service.createExpense(x),saved->load()));}
    @FXML private void onEditExpense(){Expense x=expenseTable.getSelectionModel().getSelectedItem();if(x==null){AlertUtil.showInfo("No expense selected","Select an expense first.");return;}showExpense(x).ifPresent(v->run(()->{service.updateExpense(v);return v;},done->load()));}
    @FXML private void onDeleteExpense(){Expense x=expenseTable.getSelectionModel().getSelectedItem();if(x==null)return;if(!AlertUtil.confirm("Delete expense","Delete this expense?"))return;run(()->{service.deleteExpense(x.getId());return x;},done->load());}
    @FXML private void onAddIncome(){showIncome(null).ifPresent(x->run(()->service.createIncome(x),saved->load()));}
    @FXML private void onEditIncome(){Income x=incomeTable.getSelectionModel().getSelectedItem();if(x==null){AlertUtil.showInfo("No income selected","Select an income first.");return;}showIncome(x).ifPresent(v->run(()->{service.updateIncome(v);return v;},done->load()));}
    @FXML private void onDeleteIncome(){Income x=incomeTable.getSelectionModel().getSelectedItem();if(x==null)return;if(!AlertUtil.confirm("Delete income","Delete this income?"))return;run(()->{service.deleteIncome(x.getId());return x;},done->load());}
    @FXML private void onBackToDashboard(){SceneManager.getInstance().invalidate("Dashboard");SceneManager.getInstance().switchTo("Dashboard");}
    private Optional<Expense> showExpense(Expense old){boolean edit=old!=null;Dialog<Expense>d=new Dialog<>();d.setTitle(edit?"Edit Expense":"Add Expense");d.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);TextField amount=new TextField(edit?String.valueOf(old.getAmount()):"");ComboBox<String>cat=new ComboBox<>(FXCollections.observableArrayList("Food","Transport","Bills","Shopping","Study","Health","Entertainment","Other"));cat.setValue(edit?old.getCategory():"Other");TextField note=new TextField(edit?old.getNote():"");DatePicker date=new DatePicker(edit?LocalDate.parse(old.getExpenseDate()):LocalDate.now());GridPane g=new GridPane();g.setHgap(10);g.setVgap(10);g.addRow(0,new Label("Amount"),amount);g.addRow(1,new Label("Category"),cat);g.addRow(2,new Label("Note"),note);g.addRow(3,new Label("Date"),date);d.getDialogPane().setContent(g);d.setResultConverter(b->{if(b!=ButtonType.OK)return null;try{double a=Double.parseDouble(amount.getText().trim());if(a<0)throw new NumberFormatException();Expense e=edit?old:new Expense();e.setUserId(CurrentUser.getId());e.setAmount(a);e.setCategory(cat.getValue());e.setNote(note.getText());e.setExpenseDate(date.getValue().toString());return e;}catch(Exception ex){AlertUtil.showInfo("Invalid amount","Enter a non-negative number and a date.");return null;}});return d.showAndWait();}
    private Optional<Income> showIncome(Income old){boolean edit=old!=null;Dialog<Income>d=new Dialog<>();d.setTitle(edit?"Edit Income":"Add Income");d.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);TextField amount=new TextField(edit?String.valueOf(old.getAmount()):"");TextField source=new TextField(edit?old.getSource():"");DatePicker date=new DatePicker(edit?LocalDate.parse(old.getIncomeDate()):LocalDate.now());GridPane g=new GridPane();g.setHgap(10);g.setVgap(10);g.addRow(0,new Label("Amount"),amount);g.addRow(1,new Label("Source"),source);g.addRow(2,new Label("Date"),date);d.getDialogPane().setContent(g);d.setResultConverter(b->{if(b!=ButtonType.OK)return null;try{double a=Double.parseDouble(amount.getText().trim());if(a<0||date.getValue()==null)throw new NumberFormatException();Income i=edit?old:new Income();i.setUserId(CurrentUser.getId());i.setAmount(a);i.setSource(source.getText());i.setIncomeDate(date.getValue().toString());return i;}catch(Exception ex){AlertUtil.showInfo("Invalid input","Enter a non-negative amount and a date.");return null;}});return d.showAndWait();}
    private String money(double n){return String.format("৳ %.2f",n);}
    private <T> void run(java.util.concurrent.Callable<T> work,java.util.function.Consumer<T>done){javafx.concurrent.Task<T>t=new javafx.concurrent.Task<>(){@Override protected T call()throws Exception{return work.call();}};t.setOnSucceeded(e->done.accept(t.getValue()));t.setOnFailed(e->AlertUtil.showError("Operation failed","Could not complete the finance operation.",t.getException()));TaskExecutor.getInstance().workPool().submit(t);}
}

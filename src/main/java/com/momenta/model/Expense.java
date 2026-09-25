package com.momenta.model;

import javafx.beans.property.*;
public class Expense {
    private final IntegerProperty id=new SimpleIntegerProperty(this,"id");
    private final IntegerProperty userId=new SimpleIntegerProperty(this,"userId",1);
    private final DoubleProperty amount=new SimpleDoubleProperty(this,"amount");
    private final StringProperty category=new SimpleStringProperty(this,"category","Other");
    private final StringProperty note=new SimpleStringProperty(this,"note","");
    private final StringProperty expenseDate=new SimpleStringProperty(this,"expenseDate","");
    public int getId(){return id.get();} public void setId(int v){id.set(v);} public IntegerProperty idProperty(){return id;}
    public int getUserId(){return userId.get();} public void setUserId(int v){userId.set(v);} public IntegerProperty userIdProperty(){return userId;}
    public double getAmount(){return amount.get();} public void setAmount(double v){amount.set(v);} public DoubleProperty amountProperty(){return amount;}
    public String getCategory(){return category.get();} public void setCategory(String v){category.set(v);} public StringProperty categoryProperty(){return category;}
    public String getNote(){return note.get();} public void setNote(String v){note.set(v);} public StringProperty noteProperty(){return note;}
    public String getExpenseDate(){return expenseDate.get();} public void setExpenseDate(String v){expenseDate.set(v);} public StringProperty expenseDateProperty(){return expenseDate;}
}

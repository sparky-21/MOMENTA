package com.momenta.model;

import javafx.beans.property.*;
public class Income {
    private final IntegerProperty id=new SimpleIntegerProperty(this,"id");
    private final IntegerProperty userId=new SimpleIntegerProperty(this,"userId",1);
    private final DoubleProperty amount=new SimpleDoubleProperty(this,"amount");
    private final StringProperty source=new SimpleStringProperty(this,"source","");
    private final StringProperty incomeDate=new SimpleStringProperty(this,"incomeDate","");
    public int getId(){return id.get();} public void setId(int v){id.set(v);} public IntegerProperty idProperty(){return id;}
    public int getUserId(){return userId.get();} public void setUserId(int v){userId.set(v);} public IntegerProperty userIdProperty(){return userId;}
    public double getAmount(){return amount.get();} public void setAmount(double v){amount.set(v);} public DoubleProperty amountProperty(){return amount;}
    public String getSource(){return source.get();} public void setSource(String v){source.set(v);} public StringProperty sourceProperty(){return source;}
    public String getIncomeDate(){return incomeDate.get();} public void setIncomeDate(String v){incomeDate.set(v);} public StringProperty incomeDateProperty(){return incomeDate;}
}

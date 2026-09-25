package com.momenta.service;

import com.momenta.dao.ExpenseDAO; import com.momenta.dao.IncomeDAO;
import com.momenta.dao.impl.ExpenseDAOImpl; import com.momenta.dao.impl.IncomeDAOImpl;
import com.momenta.model.Expense; import com.momenta.model.Income;
import java.util.*; import java.util.stream.Collectors;

public class FinanceService {
 private final ExpenseDAO expenseDAO=new ExpenseDAOImpl(); private final IncomeDAO incomeDAO=new IncomeDAOImpl();
 public Expense createExpense(Expense e){validateExpense(e);return expenseDAO.save(e);}
 public void updateExpense(Expense e){validateExpense(e);expenseDAO.update(e);}
 public void deleteExpense(int id){expenseDAO.delete(id);}
 public Income createIncome(Income i){validateIncome(i);return incomeDAO.save(i);}
 public void updateIncome(Income i){validateIncome(i);incomeDAO.update(i);}
 public void deleteIncome(int id){incomeDAO.delete(id);}
 public List<Expense> getExpenses(int userId){return expenseDAO.findAll(userId);}
 public List<Income> getIncome(int userId){return incomeDAO.findAll(userId);}
 public double totalExpenses(int userId){return getExpenses(userId).stream().mapToDouble(Expense::getAmount).sum();}
 public double totalIncome(int userId){return getIncome(userId).stream().mapToDouble(Income::getAmount).sum();}
 public double balance(int userId){return totalIncome(userId)-totalExpenses(userId);}
 public Map<String,Double> expenseByCategory(int userId){return getExpenses(userId).stream().collect(Collectors.groupingBy(Expense::getCategory,Collectors.summingDouble(Expense::getAmount)));}
 public double savingsRate(int userId){double in=totalIncome(userId);return in<=0?0:Math.max(0,Math.min(100,(balance(userId)/in)*100));}
 private void validateExpense(Expense e){if(e.getAmount()<0)throw new IllegalArgumentException("Expense cannot be negative.");if(e.getExpenseDate()==null||e.getExpenseDate().isBlank())throw new IllegalArgumentException("Expense date is required.");if(e.getCategory()==null||e.getCategory().isBlank())e.setCategory("Other");}
 private void validateIncome(Income i){if(i.getAmount()<0)throw new IllegalArgumentException("Income cannot be negative.");if(i.getIncomeDate()==null||i.getIncomeDate().isBlank())throw new IllegalArgumentException("Income date is required.");}
}

package com.momenta.dao;
import com.momenta.model.Expense; import java.util.List;
public interface ExpenseDAO { Expense save(Expense e); void update(Expense e); void delete(int id); List<Expense> findAll(int userId); }

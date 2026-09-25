package com.momenta.dao;
import com.momenta.model.Income; import java.util.List;
public interface IncomeDAO { Income save(Income i); void update(Income i); void delete(int id); List<Income> findAll(int userId); }

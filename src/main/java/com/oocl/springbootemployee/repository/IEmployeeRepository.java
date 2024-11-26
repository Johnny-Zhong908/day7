package com.oocl.springbootemployee.repository;

import com.oocl.springbootemployee.model.Employee;
import com.oocl.springbootemployee.model.Gender;

import java.util.List;

public interface IEmployeeRepository {
    List<Employee> getAll();

    Employee getEmployeeById(Integer id);

    List<Employee> getEmployeesByGender(Gender gender);

    Employee addEmployee(Employee employee);

    Employee updateEmployee(Integer id, Employee employee);

    void removeEmployee(Integer id);

    List<Employee> getAllByPageSize(Integer pageIndex, Integer pageSize);
    boolean getIsActive(Employee employee);
}

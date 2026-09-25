package com.example.expenseapp.mapper;

import com.example.expenseapp.entity.Department;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DepartmentMapper {

    Optional<Department> findById(Integer deptId);

    List<Department> findAll();
}

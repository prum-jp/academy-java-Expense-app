package com.example.expenseapp.mapper;

import com.example.expenseapp.entity.ApprovalRoute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ApprovalRouteMapper {

    List<ApprovalRoute> findByDeptIdOrderByStep(Integer deptId);

    Optional<ApprovalRoute> findCurrentStep(
            @Param("deptId") Integer deptId,
            @Param("expenseId") Integer expenseId);
}

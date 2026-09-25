package com.example.expenseapp.mapper;

import com.example.expenseapp.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper {

    int insert(AuditLog auditLog);
}

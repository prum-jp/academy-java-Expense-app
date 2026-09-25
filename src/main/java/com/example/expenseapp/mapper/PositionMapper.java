package com.example.expenseapp.mapper;

import com.example.expenseapp.entity.Position;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PositionMapper {

    Optional<Position> findById(Integer positionId);

    List<Position> findAll();
}

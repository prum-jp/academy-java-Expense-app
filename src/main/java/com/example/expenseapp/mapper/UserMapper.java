package com.example.expenseapp.mapper;

import com.example.expenseapp.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    Optional<User> findById(Integer userId);

    Optional<User> findByUserName(String userName);

    int updatePassword(@Param("userId") Integer userId, @Param("password") String password);
}

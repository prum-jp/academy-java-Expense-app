package com.example.expenseapp.security;

import com.example.expenseapp.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    public DatabaseUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userMapper.findByUserName(username)
                .map(LoginUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません。"));
    }
}

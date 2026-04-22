package com.taskmanagement.app.authservice.service;


import com.taskmanagement.app.authservice.dto.TaskUserDetails;
import com.taskmanagement.app.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.taskmanagement.app.authservice.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaskUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if(user.get() != null){
            return new TaskUserDetails(user.get());
        }
        else{
            System.out.println("UserName Not Found");
            throw new UsernameNotFoundException("UserName not found");
        }
    }
}

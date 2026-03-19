package com.alejandro.users;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public List<String> getAllUsers() {
        return List.of("Alice", "Bob", "Charlie");
    }

    public String getUserByid(Long id){
        return "User with id: " + id;
    }
}

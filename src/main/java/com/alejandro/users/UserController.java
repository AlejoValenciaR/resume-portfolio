package com.alejandro.users;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String getAllUsers(Model model) {
        model.addAttribute("pageTitle", "Users");
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentPath", "/api/users");
        return "users";
    }

    @GetMapping("/{id}")
    public String getUserById(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "User Detail");
        model.addAttribute("userId", id);
        model.addAttribute("userDescription", userService.getUserByid(id));
        model.addAttribute("currentPath", "/api/users/" + id);
        return "user-detail";
    }
}

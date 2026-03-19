package com.alejandro.hello;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/api/hello")
public class HelloController {

    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @GetMapping
    public String hello(Model model) {
        model.addAttribute("pageTitle", "Hello");
        model.addAttribute("message", helloService.getMessage());
        model.addAttribute("currentPath", "/api/hello");
        return "hello";
    }
}

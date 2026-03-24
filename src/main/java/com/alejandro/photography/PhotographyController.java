package com.alejandro.photography;

import com.alejandro.react.ReactPageHtmlService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PhotographyController {

    private final ReactPageHtmlService reactPageHtmlService;

    public PhotographyController(ReactPageHtmlService reactPageHtmlService) {
        this.reactPageHtmlService = reactPageHtmlService;
    }

    @GetMapping({"/photography", "/fotografia", "/fotografia/"})
    public String photographyRoot() {
        return "redirect:/photography/";
    }

    @GetMapping("/photography/")
    public ResponseEntity<String> photographyIndex() {
        return reactPageHtmlService.renderPhotographyPage();
    }
}

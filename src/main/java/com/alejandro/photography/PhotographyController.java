package com.alejandro.photography;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class PhotographyController {

    @ModelAttribute("assetVersion")
    public long assetVersion() {
        return System.currentTimeMillis();
    }

    @GetMapping({"/photography", "/fotografia", "/fotografia/"})
    public String photographyRoot() {
        return "redirect:/photography/";
    }

    @GetMapping("/photography/")
    public String photographyIndex() {
        return "photography/index";
    }
}

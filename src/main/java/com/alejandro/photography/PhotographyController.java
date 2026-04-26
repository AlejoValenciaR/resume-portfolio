package com.alejandro.photography;

import com.alejandro.react.ReactPageHtmlService;
import java.util.Locale;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PhotographyController {

    private final ReactPageHtmlService reactPageHtmlService;

    public PhotographyController(ReactPageHtmlService reactPageHtmlService) {
        this.reactPageHtmlService = reactPageHtmlService;
    }

    @GetMapping({"/photography", "/fotografia", "/fotografia/"})
    public String photographyRoot(@RequestParam(name = "lang", defaultValue = "es") String lang) {
        return "redirect:/photography/?lang=" + resolveLang(lang);
    }

    @GetMapping("/photography/")
    public ResponseEntity<String> photographyIndex(@RequestParam(name = "lang", defaultValue = "es") String lang) {
        Locale locale = resolveLocale(lang);
        LocaleContextHolder.setLocale(locale);
        return reactPageHtmlService.renderPhotographyPage(isSpanish(locale));
    }

    private String resolveLang(String lang) {
        return "en".equalsIgnoreCase(lang) ? "en" : "es";
    }

    private Locale resolveLocale(String lang) {
        return "en".equalsIgnoreCase(lang) ? Locale.ENGLISH : Locale.forLanguageTag("es");
    }

    private boolean isSpanish(Locale locale) {
        return "es".equalsIgnoreCase(locale.getLanguage());
    }
}

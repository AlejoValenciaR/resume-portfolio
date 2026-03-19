package com.alejandro.cv.contact;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactMessageController {

    private final ContactMailService contactMailService;
    private final MessageSource messageSource;

    public ContactMessageController(ContactMailService contactMailService, MessageSource messageSource) {
        this.contactMailService = contactMailService;
        this.messageSource = messageSource;
    }

    @PostMapping(path = "/send", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> sendMessage(
            @Valid @RequestBody ContactMessageRequest request,
            BindingResult bindingResult,
            @org.springframework.web.bind.annotation.RequestParam(name = "lang", defaultValue = "en") String lang) {
        Locale locale = "es".equalsIgnoreCase(lang) ? Locale.forLanguageTag("es") : Locale.ENGLISH;
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(validationErrorResponse(bindingResult, locale));
        }

        try {
            contactMailService.send(request, locale);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", messageSource.getMessage("contact.api.success", null, locale)));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "success", false,
                    "message", ex.getMessage()));
        } catch (MessagingException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "success", false,
                    "message", messageSource.getMessage("contact.api.buildError", null, locale)));
        }
    }

    private Map<String, Object> validationErrorResponse(BindingResult bindingResult, Locale locale) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        return Map.of(
                "success", false,
                "message", messageSource.getMessage("contact.api.validation", null, locale),
                "errors", errors);
    }
}

package com.alejandro.cv;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/portfolio")
public class CvController {

    private final AlejandroResumePdfService alejandroResumePdfService;

    public CvController(AlejandroResumePdfService alejandroResumePdfService) {
        this.alejandroResumePdfService = alejandroResumePdfService;
    }

    @GetMapping("/alejandro")
    public String showAlejandroPortfolio(@RequestParam(name = "lang", defaultValue = "en") String lang) {
        Locale locale = resolveLocale(lang);
        LocaleContextHolder.setLocale(locale);
        return isSpanish(locale) ? "cv/index6-es" : "cv/index6";
    }

    @GetMapping(value = "/alejandro/cv.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadAlejandroCvPdf(@RequestParam(name = "lang", defaultValue = "en") String lang) {
        byte[] pdfBytes = alejandroResumePdfService.generateAlejandroResumePdf(resolveLocale(lang));
        ContentDisposition contentDisposition = ContentDisposition.attachment()
            .filename("Alejandro-Valencia-Rivera-Resume.pdf", StandardCharsets.UTF_8)
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfBytes.length)
            .body(pdfBytes);
    }

    @GetMapping(value = "/alejandro/compact-resume.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadAlejandroCompactResumePdf(@RequestParam(name = "lang", defaultValue = "en") String lang) {
        byte[] pdfBytes = alejandroResumePdfService.generateAlejandroCompactResumePdf(resolveLocale(lang));
        ContentDisposition contentDisposition = ContentDisposition.attachment()
            .filename("Alejandro-Valencia-Rivera-Compact-Resume.pdf", StandardCharsets.UTF_8)
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfBytes.length)
            .body(pdfBytes);
    }

    private Locale resolveLocale(String lang) {
        return "es".equalsIgnoreCase(lang) ? Locale.forLanguageTag("es") : Locale.ENGLISH;
    }

    private boolean isSpanish(Locale locale) {
        return "es".equalsIgnoreCase(locale.getLanguage());
    }
}

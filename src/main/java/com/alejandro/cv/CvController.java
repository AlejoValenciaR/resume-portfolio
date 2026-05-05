package com.alejandro.cv;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CvController {

    private final AlejandroResumePdfService alejandroResumePdfService;

    public CvController(AlejandroResumePdfService alejandroResumePdfService) {
        this.alejandroResumePdfService = alejandroResumePdfService;
    }

    @GetMapping("/portfolio/alejandro")
    public String redirectLegacyPortfolio(@RequestParam(name = "lang", defaultValue = "en") String lang) {
        return "redirect:/developer?lang=" + resolveLang(lang);
    }

    @GetMapping("/developer")
    public String showAlejandroPortfolio(@RequestParam(name = "lang", defaultValue = "en") String lang) {
        Locale locale = resolveLocale(lang);
        LocaleContextHolder.setLocale(locale);
        return isSpanish(locale) ? "cv/index6-es" : "cv/index6";
    }

    @GetMapping(value = "/developer/cv.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadAlejandroCvPdf(@RequestParam(name = "lang", defaultValue = "en") String lang) {
        Locale locale = resolveLocale(lang);
        byte[] pdfBytes = alejandroResumePdfService.generateAlejandroResumePdf(locale);
        String filename = isSpanish(locale)
            ? "Alejandro-Valencia-Rivera-Hoja-de-Vida-Completa.pdf"
            : "Alejandro-Valencia-Rivera-Full-Resume.pdf";
        ContentDisposition contentDisposition = ContentDisposition.attachment()
            .filename(filename, StandardCharsets.UTF_8)
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfBytes.length)
            .body(pdfBytes);
    }

    @GetMapping(value = "/developer/compact-resume.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadAlejandroCompactResumePdf(@RequestParam(name = "lang", defaultValue = "en") String lang) {
        Locale locale = resolveLocale(lang);
        byte[] pdfBytes = alejandroResumePdfService.generateAlejandroCompactResumePdf(locale);
        String filename = isSpanish(locale)
            ? "Alejandro-Valencia-Rivera-Hoja-de-Vida-Corta.pdf"
            : "Alejandro-Valencia-Rivera-Compact-Resume.pdf";
        ContentDisposition contentDisposition = ContentDisposition.attachment()
            .filename(filename, StandardCharsets.UTF_8)
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfBytes.length)
            .body(pdfBytes);
    }

    @GetMapping(value = "/developer/ats-resume.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadAlejandroAtsResumePdf(@RequestParam(name = "lang", defaultValue = "en") String lang) {
        Locale locale = resolveLocale(lang);
        byte[] pdfBytes = alejandroResumePdfService.generateAlejandroAtsResumePdf(locale);
        String filename = isSpanish(locale)
            ? "Alejandro-Valencia-Rivera-Hoja-de-Vida-ATS.pdf"
            : "Alejandro-Valencia-Rivera-ATS-Resume.pdf";
        ContentDisposition contentDisposition = ContentDisposition.attachment()
            .filename(filename, StandardCharsets.UTF_8)
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfBytes.length)
            .body(pdfBytes);
    }

    private String resolveLang(String lang) {
        return "es".equalsIgnoreCase(lang) ? "es" : "en";
    }

    private Locale resolveLocale(String lang) {
        return "es".equalsIgnoreCase(resolveLang(lang)) ? Locale.forLanguageTag("es") : Locale.ENGLISH;
    }

    private boolean isSpanish(Locale locale) {
        return "es".equalsIgnoreCase(locale.getLanguage());
    }
}

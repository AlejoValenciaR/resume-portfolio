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

import com.alejandro.react.ReactPageHtmlService;

@Controller
public class CvController {

    private final AlejandroResumePdfService alejandroResumePdfService;
    private final ReactPageHtmlService reactPageHtmlService;

    public CvController(AlejandroResumePdfService alejandroResumePdfService, ReactPageHtmlService reactPageHtmlService) {
        this.alejandroResumePdfService = alejandroResumePdfService;
        this.reactPageHtmlService = reactPageHtmlService;
    }

    @GetMapping("/portfolio/alejandro")
    public String redirectLegacyPortfolio() {
        return "redirect:/developer";
    }

    @GetMapping("/developer")
    public ResponseEntity<String> showAlejandroPortfolio(@RequestParam(name = "lang", defaultValue = "en") String lang) {
        Locale locale = resolveLocale(lang);
        LocaleContextHolder.setLocale(locale);
        return reactPageHtmlService.renderPortfolioPage(isSpanish(locale));
    }

    @GetMapping(value = "/developer/cv.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
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

    @GetMapping(value = "/developer/compact-resume.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
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

    private Locale resolveLocale(String lang) {
        return "es".equalsIgnoreCase(lang) ? Locale.forLanguageTag("es") : Locale.ENGLISH;
    }

    private boolean isSpanish(Locale locale) {
        return "es".equalsIgnoreCase(locale.getLanguage());
    }
}

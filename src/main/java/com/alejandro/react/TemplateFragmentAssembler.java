package com.alejandro.react;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class TemplateFragmentAssembler {

    private static final Pattern OUTER_TH_BLOCK = Pattern.compile(
            "(?s)^\\s*<th:block[^>]*th:fragment=\"[^\"]+\"[^>]*>(.*)</th:block>\\s*$");
    private static final Pattern THYMELEAF_REPLACE = Pattern.compile(
            "(?s)<th:block[^>]*th:replace=\"~\\{([^}]+)}\"[^>]*>\\s*</th:block>");

    public String assembleFragment(String templatePath) {
        return resolveNestedFragments(loadTemplate(templatePath));
    }

    private String resolveNestedFragments(String templateContent) {
        String content = unwrapOuterThBlock(templateContent);
        Matcher matcher = THYMELEAF_REPLACE.matcher(content);
        StringBuffer assembled = new StringBuffer();

        while (matcher.find()) {
            String reference = matcher.group(1);
            String fragmentPath = extractTemplatePath(reference);
            String replacement = resolveNestedFragments(loadTemplate(fragmentPath));
            matcher.appendReplacement(assembled, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(assembled);
        return assembled.toString();
    }

    private String extractTemplatePath(String reference) {
        String templateReference = reference.split("::")[0].trim();
        return templateReference.endsWith(".html") ? templateReference : templateReference + ".html";
    }

    private String unwrapOuterThBlock(String templateContent) {
        Matcher matcher = OUTER_TH_BLOCK.matcher(templateContent);
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        return templateContent.trim();
    }

    private String loadTemplate(String templatePath) {
        ClassPathResource resource = new ClassPathResource("templates/" + templatePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load template fragment: " + templatePath, exception);
        }
    }
}

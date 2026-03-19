package com.alejandro.cv.model;

import java.util.List;

public record ExperienceSection(
    String eyebrow,
    String titlePrimary,
    String titleSecondary,
    String highlightWord,
    List<Entry> items
) {
    public record Entry(
        String role,
        String company,
        String location,
        String period,
        String summary,
        List<String> tags,
        List<String> bullets
    ) {
    }
}

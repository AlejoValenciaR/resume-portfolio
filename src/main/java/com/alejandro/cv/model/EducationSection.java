package com.alejandro.cv.model;

import java.util.List;

public record EducationSection(
    String eyebrow,
    String titlePrimary,
    String titleSecondary,
    String highlightWord,
    List<Entry> items
) {
    public record Entry(
        String degree,
        String institution,
        String location,
        String period,
        List<String> highlights
    ) {
    }
}

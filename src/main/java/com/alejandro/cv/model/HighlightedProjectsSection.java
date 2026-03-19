package com.alejandro.cv.model;

import java.util.List;

public record HighlightedProjectsSection(
    String eyebrow,
    String title,
    List<Item> items
) {
    public record Item(
        String title,
        String company,
        String period,
        String summary,
        String impact,
        String stack,
        List<String> highlights
    ) {
    }
}

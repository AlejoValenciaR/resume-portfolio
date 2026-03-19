package com.alejandro.cv.model;

import java.util.List;

public record LanguagesSection(
    String eyebrow,
    String titlePrimary,
    String titleSecondary,
    String highlightWord,
    List<Entry> items
) {
    public record Entry(String name, String level) {
    }
}

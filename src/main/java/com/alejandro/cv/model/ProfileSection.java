package com.alejandro.cv.model;

import java.util.List;

public record ProfileSection(
    String eyebrow,
    String titlePrimary,
    String titleSecondary,
    String highlightWord,
    List<String> paragraphs,
    List<Highlight> highlights
) {
    public record Highlight(String title, String description) {
    }
}

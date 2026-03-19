package com.alejandro.cv.model;

import java.util.List;

public record TechnologiesSection(
    String eyebrow,
    String titlePrimary,
    String titleSecondary,
    String highlightWord,
    List<Category> categories
) {
    public record Category(String title, List<String> items) {
    }
}

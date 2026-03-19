package com.alejandro.cv.model;

import java.util.List;

public record ContactSection(
    String eyebrow,
    String titlePrimary,
    String titleSecondary,
    String highlightWord,
    String summary,
    List<Item> items
) {
    public record Item(String label, String value, String url, String iconClass) {
    }
}

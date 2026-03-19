package com.alejandro.cv.model;

import java.util.List;

public record ReferencesSection(
    String eyebrow,
    String titlePrimary,
    String titleSecondary,
    String highlightWord,
    String summary,
    List<Entry> items
) {
    public record Entry(
        String name,
        String role,
        String company,
        String phone
    ) {
    }
}

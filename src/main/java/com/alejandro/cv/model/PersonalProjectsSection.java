package com.alejandro.cv.model;

import java.util.List;

public record PersonalProjectsSection(
    String eyebrow,
    String title,
    List<Item> items
) {
    public record Item(
        String title,
        String summary,
        String stack,
        String repoUrl,
        String liveUrl
    ) {
    }
}

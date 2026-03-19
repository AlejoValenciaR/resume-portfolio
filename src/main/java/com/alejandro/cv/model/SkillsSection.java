package com.alejandro.cv.model;

import java.util.List;

public record SkillsSection(
    String eyebrow,
    String titlePrimary,
    String titleSecondary,
    String highlightWord,
    List<SkillCard> items
) {
    public record SkillCard(String badge, String title, String description, int level) {
    }
}

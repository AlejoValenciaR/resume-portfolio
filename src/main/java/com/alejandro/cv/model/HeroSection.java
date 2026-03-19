package com.alejandro.cv.model;

import java.util.List;

public record HeroSection(
    String eyebrow,
    String titlePrimary,
    String titleSecondary,
    String highlightWord,
    String summary,
    List<Stat> stats,
    Action primaryAction,
    Action secondaryAction
) {
    public record Stat(String value, String label) {
    }

    public record Action(String label, String url) {
    }
}

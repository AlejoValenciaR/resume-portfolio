package com.alejandro.cv.model;

import java.util.List;

public record PersonalInfoSection(
    String fullName,
    String title,
    String shortBio,
    String initials,
    String footerText,
    List<Fact> facts,
    List<Link> primaryActions,
    List<Link> socialLinks
) {
    public record Fact(String label, String value, String url) {
    }

    public record Link(String label, String url, String iconClass) {
    }
}

package com.alejandro.cv.model;

import java.util.List;

public record CompactExperienceSection(
    String title,
    List<CompanyExperience> items
) {
    public record CompanyExperience(
        String company,
        String role,
        String period,
        List<Project> projects
    ) {
    }

    public record Project(
        String name,
        List<String> stack
    ) {
    }
}

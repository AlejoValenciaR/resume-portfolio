package com.alejandro.cv.model;

import java.util.List;

public record ResumePdfDocument(
    PersonalInfoSection personalInfo,
    HeroSection hero,
    ProfileSection profile,
    SkillsSection skills,
    TechnologiesSection technologies,
    ExperienceSection experience,
    HighlightedProjectsSection highlightedProjects,
    PersonalProjectsSection personalProjects,
    EducationSection education,
    LanguagesSection languages,
    ContactSection contact,
    String photoDataUri
) {
}

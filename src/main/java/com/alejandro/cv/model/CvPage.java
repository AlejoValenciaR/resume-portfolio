package com.alejandro.cv.model;

public record CvPage(
    PersonalInfoSection personalInfo,
    NavigationSection navigation,
    HeroSection hero,
    ProfileSection profile,
    SkillsSection skills,
    TechnologiesSection technologies,
    ExperienceSection experience,
    EducationSection education,
    LanguagesSection languages,
    ReferencesSection references,
    ContactSection contact
) {
}

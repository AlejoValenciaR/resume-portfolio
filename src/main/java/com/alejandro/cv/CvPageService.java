package com.alejandro.cv;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.alejandro.cv.model.ContactSection;
import com.alejandro.cv.model.CvPage;
import com.alejandro.cv.model.EducationSection;
import com.alejandro.cv.model.ExperienceSection;
import com.alejandro.cv.model.HeroSection;
import com.alejandro.cv.model.LanguagesSection;
import com.alejandro.cv.model.NavigationSection;
import com.alejandro.cv.model.PersonalInfoSection;
import com.alejandro.cv.model.ProfileSection;
import com.alejandro.cv.model.ReferencesSection;
import com.alejandro.cv.model.SkillsSection;
import com.alejandro.cv.model.TechnologiesSection;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CvPageService {

    private final ObjectMapper objectMapper;

    public CvPageService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CvPage loadAlejandroCv(Locale locale) {
        return new CvPage(
            readSection(localizedResourcePath("cv/personal-info.json", locale), PersonalInfoSection.class),
            readSection("cv/navigation.json", NavigationSection.class),
            readSection(localizedResourcePath("cv/hero.json", locale), HeroSection.class),
            readSection(localizedResourcePath("cv/profile.json", locale), ProfileSection.class),
            readSection(localizedResourcePath("cv/skills.json", locale), SkillsSection.class),
            readSection(localizedResourcePath("cv/technologies.json", locale), TechnologiesSection.class),
            readSection(localizedResourcePath("cv/experience.json", locale), ExperienceSection.class),
            readSection(localizedResourcePath("cv/education.json", locale), EducationSection.class),
            readSection(localizedResourcePath("cv/languages.json", locale), LanguagesSection.class),
            readSection("cv/references.json", ReferencesSection.class),
            readSection(localizedResourcePath("cv/contact.json", locale), ContactSection.class)
        );
    }

    private String localizedResourcePath(String resourcePath, Locale locale) {
        if (locale != null && "es".equalsIgnoreCase(locale.getLanguage())) {
            String localizedPath = resourcePath.replace(".json", "-es.json");
            if (new ClassPathResource(localizedPath).exists()) {
                return localizedPath;
            }
        }
        return resourcePath;
    }

    private <T> T readSection(String resourcePath, Class<T> sectionType) {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, sectionType);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load CV section from " + resourcePath, exception);
        }
    }
}

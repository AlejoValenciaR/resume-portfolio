package com.alejandro.cv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import com.alejandro.cv.model.CvPage;
import com.alejandro.cv.model.CompactExperienceSection;
import com.alejandro.cv.model.ExperienceSection;
import com.alejandro.cv.model.HighlightedProjectsSection;
import com.alejandro.cv.model.PersonalProjectsSection;
import com.alejandro.cv.model.ResumePdfDocument;
import com.alejandro.cv.model.SkillsSection;
import com.alejandro.cv.model.TechnologiesSection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class AlejandroResumePdfService {

    private static final String PHOTO_RESOURCE = "static/ovro-index6-full/assets/img/all-images/hero/Profile5.jpg";

    private final CvPageService cvPageService;
    private final ObjectMapper objectMapper;
    private final SpringTemplateEngine templateEngine;

    public AlejandroResumePdfService(
        CvPageService cvPageService,
        ObjectMapper objectMapper,
        SpringTemplateEngine templateEngine
    ) {
        this.cvPageService = cvPageService;
        this.objectMapper = objectMapper;
        this.templateEngine = templateEngine;
    }

    public byte[] generateAlejandroResumePdf(Locale locale) {
        Locale pdfLocale = resolveLocale(locale);
        ResumePdfDocument resume = buildResumeDocument(pdfLocale);
        Context context = new Context(pdfLocale);
        context.setVariable("resume", resume);
        context.setVariable("coverSkills", sliceSkills(resume.skills().items(), 0, 3));
        context.setVariable("experienceBatchOne", slice(resume.experience().items(), 0, 3));
        context.setVariable("experienceBatchTwo", slice(resume.experience().items(), 3, 5));
        context.setVariable("experienceBatchThree", slice(resume.experience().items(), 5, resume.experience().items().size()));
        context.setVariable("highlightedBatchOne", sliceProjects(resume.highlightedProjects().items(), 0, 3));
        context.setVariable("highlightedBatchTwo", sliceProjects(resume.highlightedProjects().items(), 3, resume.highlightedProjects().items().size()));
        context.setVariable("technologyPrimary", sliceCategories(resume.technologies().categories(), 0, 3));
        context.setVariable("technologySecondary", sliceCategories(resume.technologies().categories(), 3, resume.technologies().categories().size()));

        return renderPdf(fullResumeTemplateName(pdfLocale), context, "Unable to generate Alejandro resume PDF");
    }

    public byte[] generateAlejandroCompactResumePdf(Locale locale) {
        Locale pdfLocale = resolveLocale(locale);
        ResumePdfDocument resume = buildResumeDocument(pdfLocale);
        CompactExperienceSection compactExperience =
            readSection(localizedResourcePath("cv/brief-experience.json", pdfLocale), CompactExperienceSection.class);

        Context context = new Context(pdfLocale);
        context.setVariable("resume", resume);
        context.setVariable("principalTechnologies", principalTechnologies(pdfLocale));
        context.setVariable("compactExperiencePageOne", sliceCompactCompanies(compactExperience.items(), 0, 4));
        context.setVariable("compactExperiencePageTwo", sliceCompactCompanies(compactExperience.items(), 4, compactExperience.items().size()));
        context.setVariable("compactGithubProjects", resume.personalProjects().items());

        return renderPdf(compactResumeTemplateName(pdfLocale), context, "Unable to generate Alejandro compact resume PDF");
    }

    private ResumePdfDocument buildResumeDocument(Locale locale) {
        CvPage cvPage = cvPageService.loadAlejandroCv(locale);

        return new ResumePdfDocument(
            cvPage.personalInfo(),
            cvPage.hero(),
            cvPage.profile(),
            cvPage.skills(),
            cvPage.technologies(),
            cvPage.experience(),
            readSection(localizedResourcePath("cv/highlighted-projects.json", locale), HighlightedProjectsSection.class),
            readSection(localizedResourcePath("cv/personal-projects.json", locale), PersonalProjectsSection.class),
            cvPage.education(),
            cvPage.languages(),
            cvPage.contact(),
            readImageDataUri(PHOTO_RESOURCE)
        );
    }

    private <T> T readSection(String resourcePath, Class<T> sectionType) {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, sectionType);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load PDF resume section from " + resourcePath, exception);
        }
    }

    private String readImageDataUri(String resourcePath) {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] imageBytes = inputStream.readAllBytes();
            String mimeType = "image/jpeg";
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            return "data:" + mimeType + ";base64," + base64;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load resume photo from " + resourcePath, exception);
        }
    }

    private byte[] renderPdf(String templateName, Context context, String errorMessage) {
        String html = templateEngine.process(templateName, context);
        Document parsedHtml = Jsoup.parse(html);
        parsedHtml.outputSettings()
            .syntax(Document.OutputSettings.Syntax.xml)
            .prettyPrint(false);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useDefaultPageSize(210, 297, PdfRendererBuilder.PageSizeUnits.MM);
            builder.withW3cDocument(new W3CDom().fromJsoup(parsedHtml), "/");
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException(errorMessage, exception);
        }
    }

    private List<ExperienceSection.Entry> slice(List<ExperienceSection.Entry> items, int fromIndex, int toIndex) {
        int safeFrom = Math.min(Math.max(fromIndex, 0), items.size());
        int safeTo = Math.min(Math.max(toIndex, safeFrom), items.size());
        return items.subList(safeFrom, safeTo);
    }

    private List<SkillsSection.SkillCard> sliceSkills(List<SkillsSection.SkillCard> items, int fromIndex, int toIndex) {
        int safeFrom = Math.min(Math.max(fromIndex, 0), items.size());
        int safeTo = Math.min(Math.max(toIndex, safeFrom), items.size());
        return items.subList(safeFrom, safeTo);
    }

    private List<CompactExperienceSection.CompanyExperience> sliceCompactCompanies(List<CompactExperienceSection.CompanyExperience> items, int fromIndex, int toIndex) {
        int safeFrom = Math.min(Math.max(fromIndex, 0), items.size());
        int safeTo = Math.min(Math.max(toIndex, safeFrom), items.size());
        return items.subList(safeFrom, safeTo);
    }

    private List<HighlightedProjectsSection.Item> sliceProjects(List<HighlightedProjectsSection.Item> items, int fromIndex, int toIndex) {
        int safeFrom = Math.min(Math.max(fromIndex, 0), items.size());
        int safeTo = Math.min(Math.max(toIndex, safeFrom), items.size());
        return items.subList(safeFrom, safeTo);
    }

    private List<TechnologiesSection.Category> sliceCategories(List<TechnologiesSection.Category> items, int fromIndex, int toIndex) {
        int safeFrom = Math.min(Math.max(fromIndex, 0), items.size());
        int safeTo = Math.min(Math.max(toIndex, safeFrom), items.size());
        return items.subList(safeFrom, safeTo);
    }

    private List<String> principalTechnologies(Locale locale) {
        if (isSpanish(locale)) {
            return List.of(
                "Java",
                "Spring Boot",
                "Python",
                "AWS",
                "Terraform",
                "APIs REST / SOAP",
                "SQL / NoSQL",
                "Docker / Kubernetes",
                "Salesforce Marketing Cloud",
                "Ingenieria de datos"
            );
        }

        return List.of(
            "Java",
            "Spring Boot",
            "Python",
            "AWS",
            "Terraform",
            "REST / SOAP APIs",
            "SQL / NoSQL",
            "Docker / Kubernetes",
            "Salesforce Marketing Cloud",
            "Data Engineering"
        );
    }

    private Locale resolveLocale(Locale locale) {
        return isSpanish(locale) ? Locale.forLanguageTag("es") : Locale.ENGLISH;
    }

    private boolean isSpanish(Locale locale) {
        return locale != null && "es".equalsIgnoreCase(locale.getLanguage());
    }

    private String localizedResourcePath(String resourcePath, Locale locale) {
        if (isSpanish(locale)) {
            String localizedPath = resourcePath.replace(".json", "-es.json");
            if (new ClassPathResource(localizedPath).exists()) {
                return localizedPath;
            }
        }
        return resourcePath;
    }

    private String fullResumeTemplateName(Locale locale) {
        return isSpanish(locale) ? "cv/pdf/alejandro-resume-es" : "cv/pdf/alejandro-resume";
    }

    private String compactResumeTemplateName(Locale locale) {
        return isSpanish(locale) ? "cv/pdf/alejandro-brief-resume-es" : "cv/pdf/alejandro-brief-resume";
    }
}

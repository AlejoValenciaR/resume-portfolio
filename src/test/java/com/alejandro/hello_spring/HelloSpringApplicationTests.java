package com.alejandro.hello_spring;

import org.junit.jupiter.api.Test;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HelloSpringApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void rootRouteRedirectsToPortfolio() throws Exception {
		mockMvc.perform(get("/"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/portfolio/alejandro"));
	}

	@Test
	void legacyHomeRouteRendersHtml() throws Exception {
		mockMvc.perform(get("/home"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith("text/html"))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Atlas Grove")));
	}

	@Test
	void helloRouteRendersHtml() throws Exception {
		mockMvc.perform(get("/api/hello"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith("text/html"))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Hello, World!")));
	}

	@Test
	void usersRouteRendersHtml() throws Exception {
		mockMvc.perform(get("/api/users"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith("text/html"))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Alice")));
	}

	@Test
	void userDetailRouteRendersHtml() throws Exception {
		mockMvc.perform(get("/api/users/2"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith("text/html"))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("User with id: 2")));
	}

	@Test
	void cvRouteRendersHtml() throws Exception {
		mockMvc.perform(get("/portfolio/alejandro"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith("text/html"))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Alejandro Valencia - Senior Backend Developer")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Alejandro Valencia")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Senior Backend Developer")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Java & Python Solutions")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Download my CV:")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Full Resume (8 pages)")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Small Resume (2 pages)")));
	}

	@Test
	void cvSpanishRouteRendersHtml() throws Exception {
		mockMvc.perform(get("/portfolio/alejandro").queryParam("lang", "es"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith("text/html"))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Alejandro Valencia - Desarrollador Backend Senior")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Desarrollador Backend Senior")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Descarga mi hoja de vida:")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Hoja de vida completa (8 páginas)")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Hoja de vida corta (2 páginas)")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("CONTÁCTAME")));
	}

	@Test
	void photographyRouteRedirectsToTrailingSlash() throws Exception {
		mockMvc.perform(get("/photography"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/photography/"));
	}

	@Test
	void fotografiaAliasRedirectsToPhotography() throws Exception {
		mockMvc.perform(get("/fotografia"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/photography/"));
	}

	@Test
	void photographyRouteRendersPhotographyPage() throws Exception {
		mockMvc.perform(get("/photography/"))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.CACHE_CONTROL, org.hamcrest.Matchers.containsString("no-store")))
			.andExpect(content().contentTypeCompatibleWith("text/html"))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Soulframe Studio")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Alejandro Valencia")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Portrait and Product Photographer")));
	}

	@Test
	void photographyStaticAssetsAreServed() throws Exception {
		mockMvc.perform(get("/photography/css/style.css"))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.CACHE_CONTROL, org.hamcrest.Matchers.containsString("no-store")))
			.andExpect(content().contentTypeCompatibleWith("text/css"))
			.andExpect(content().string(org.hamcrest.Matchers.containsString(".menu")));
	}

	@Test
	void cvPdfRouteDownloadsPdf() throws Exception {
		mockMvc.perform(get("/portfolio/alejandro/cv.pdf"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
			.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("Alejandro-Valencia-Rivera-Resume.pdf")))
			.andExpect(result -> {
				byte[] content = result.getResponse().getContentAsByteArray();
				org.junit.jupiter.api.Assertions.assertTrue(content.length > 1000, "PDF should not be empty");
				String pdfHeader = new String(content, 0, 5, java.nio.charset.StandardCharsets.US_ASCII);
				org.junit.jupiter.api.Assertions.assertEquals("%PDF-", pdfHeader);
				String pdfText = normalizePdfText(extractPdfText(content));
				org.junit.jupiter.api.Assertions.assertTrue(pdfText.contains("Jun 2024 - Dec 2025"));
				org.junit.jupiter.api.Assertions.assertTrue(pdfText.contains("Mar 2022 - Aug 2023"));
			});
	}

	@Test
	void cvSpanishPdfRouteDownloadsPdf() throws Exception {
		mockMvc.perform(get("/portfolio/alejandro/cv.pdf").queryParam("lang", "es"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
			.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("Alejandro-Valencia-Rivera-Resume.pdf")))
			.andExpect(result -> {
				byte[] content = result.getResponse().getContentAsByteArray();
				org.junit.jupiter.api.Assertions.assertTrue(content.length > 1000, "Spanish PDF should not be empty");
				String pdfHeader = new String(content, 0, 5, java.nio.charset.StandardCharsets.US_ASCII);
				org.junit.jupiter.api.Assertions.assertEquals("%PDF-", pdfHeader);
				String pdfText = normalizePdfText(extractPdfText(content));
				org.junit.jupiter.api.Assertions.assertTrue(pdfText.contains("jun. 2024 - dic. 2025"));
				org.junit.jupiter.api.Assertions.assertTrue(pdfText.contains("mar. 2022 - ago. 2023"));
			});
	}

	@Test
	void compactCvPdfRouteDownloadsPdf() throws Exception {
		mockMvc.perform(get("/portfolio/alejandro/compact-resume.pdf"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
			.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("Alejandro-Valencia-Rivera-Compact-Resume.pdf")))
			.andExpect(result -> {
				byte[] content = result.getResponse().getContentAsByteArray();
				org.junit.jupiter.api.Assertions.assertTrue(content.length > 1000, "Compact PDF should not be empty");
				String pdfHeader = new String(content, 0, 5, java.nio.charset.StandardCharsets.US_ASCII);
				org.junit.jupiter.api.Assertions.assertEquals("%PDF-", pdfHeader);
				String pdfText = normalizePdfText(extractPdfText(content));
				org.junit.jupiter.api.Assertions.assertTrue(pdfText.contains("Jun 2024 - Dec 2025"));
				org.junit.jupiter.api.Assertions.assertTrue(pdfText.contains("Jan 2024 - Jun 2024"));
			});
	}

	@Test
	void compactSpanishCvPdfRouteDownloadsPdf() throws Exception {
		mockMvc.perform(get("/portfolio/alejandro/compact-resume.pdf").queryParam("lang", "es"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
			.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("Alejandro-Valencia-Rivera-Compact-Resume.pdf")))
			.andExpect(result -> {
				byte[] content = result.getResponse().getContentAsByteArray();
				org.junit.jupiter.api.Assertions.assertTrue(content.length > 1000, "Spanish compact PDF should not be empty");
				String pdfHeader = new String(content, 0, 5, java.nio.charset.StandardCharsets.US_ASCII);
				org.junit.jupiter.api.Assertions.assertEquals("%PDF-", pdfHeader);
				String pdfText = normalizePdfText(extractPdfText(content));
				org.junit.jupiter.api.Assertions.assertTrue(pdfText.contains("jun. 2024 - dic. 2025"));
				org.junit.jupiter.api.Assertions.assertTrue(pdfText.contains("ene. 2024 - jun. 2024"));
			});
	}

	private String extractPdfText(byte[] pdfBytes) throws Exception {
		try (PDDocument document = PDDocument.load(pdfBytes)) {
			return new PDFTextStripper().getText(document);
		}
	}

	private String normalizePdfText(String pdfText) {
		return pdfText
			.replace('\u00A0', ' ')
			.replace('\u00AD', ' ')
			.replaceAll("\\s+", " ")
			.trim();
	}
}

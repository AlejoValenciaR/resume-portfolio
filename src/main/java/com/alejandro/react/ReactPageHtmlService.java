package com.alejandro.react;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ReactPageHtmlService {

    private static final MediaType HTML_UTF8 = MediaType.parseMediaType("text/html;charset=UTF-8");

    private final TemplateFragmentAssembler templateFragmentAssembler;
    private final ObjectMapper objectMapper;

    public ReactPageHtmlService(TemplateFragmentAssembler templateFragmentAssembler, ObjectMapper objectMapper) {
        this.templateFragmentAssembler = templateFragmentAssembler;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<String> renderPortfolioPage(boolean spanish) {
        String title = spanish
                ? "Alejandro Valencia - Desarrollador Backend Senior"
                : "Alejandro Valencia - Senior Backend Developer";
        String lang = spanish ? "es" : "en";
        String markup = wrapForReact(String.join("\n",
                templateFragmentAssembler.assembleFragment("cv/index6-fragments/preloader-progress.html"),
                templateFragmentAssembler.assembleFragment(spanish
                        ? "cv/index6-fragments/sidebar-es.html"
                        : "cv/index6-fragments/sidebar.html"),
                templateFragmentAssembler.assembleFragment(spanish
                        ? "cv/index6-fragments/header-es.html"
                        : "cv/index6-fragments/header.html"),
                templateFragmentAssembler.assembleFragment(spanish
                        ? "cv/index6-fragments/main-area-es.html"
                        : "cv/index6-fragments/main-area.html"),
                templateFragmentAssembler.assembleFragment(spanish
                        ? "cv/index6-fragments/contact-modal-es.html"
                        : "cv/index6-fragments/contact-modal.html")));

        String html = """
                <!DOCTYPE html>
                <html lang="%s">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <link rel="shortcut icon" href="/ovro-index6-full/assets/img/logo/fav-logo-icon2.png" type="image/x-icon">
                    <link rel="stylesheet" href="/ovro-index6-full/assets/css/plugins/bootstrap.min.css">
                    <link rel="stylesheet" href="/ovro-index6-full/assets/css/plugins/aos.css">
                    <link rel="stylesheet" href="/ovro-index6-full/assets/css/plugins/fontawesome.css">
                    <link rel="stylesheet" href="/ovro-index6-full/assets/css/plugins/magnific-popup.css">
                    <link rel="stylesheet" href="/ovro-index6-full/assets/css/plugins/owlcarousel.min.css">
                    <link rel="stylesheet" href="/ovro-index6-full/assets/css/plugins/sidebar.css">
                    <link rel="stylesheet" href="/ovro-index6-full/assets/css/plugins/slick-slider.css">
                    <link rel="stylesheet" href="/ovro-index6-full/assets/css/plugins/nice-select.css">
                    <link rel="stylesheet" href="/ovro-index6-full/assets/css/main.css?v=20260319-sidebar-1">
                    <link rel="stylesheet" href="/ovro-index6-full/assets/css/cv-index6.css?v=20260319-mobile-hero-3">
                    <script src="/ovro-index6-full/assets/js/plugins/jquery-3-7-1.min.js"></script>
                </head>
                <body class="body1">
                    <div id="app-root">%s</div>
                    <script>window.__REACT_PAGE__ = %s;</script>
                    <script src="/webjars/react/18.2.0/umd/react.production.min.js"></script>
                    <script src="/webjars/react-dom/18.2.0/umd/react-dom.production.min.js"></script>
                    <script src="/react/react-shell.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/bootstrap.min.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/fontawesome.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/aos.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/counter.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/sidebar.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/magnific-popup.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/owlcarousel.min.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/nice-select.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/waypoints.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/slick-slider.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/gsap.min.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/ScrollTrigger.min.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/Splitetext.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/SmoothScroll.js"></script>
                    <script src="/ovro-index6-full/assets/js/plugins/parallax.js"></script>
                    <script src="/ovro-index6-full/assets/js/%s?v=20260319-mobile-hero-2"></script>
                </body>
                </html>
                """.formatted(
                lang,
                title,
                markup,
                toInitialPageJson(title, "body1", markup),
                spanish ? "main-es.js" : "main.js");

        return ResponseEntity.ok()
                .contentType(HTML_UTF8)
                .body(html);
    }

    public ResponseEntity<String> renderPhotographyPage() {
        String title = "Soulframe Studio";
        String markup = wrapForReact("""
                <div id="container" class="container">
                    %s
                    <a href="#menu" class="totop-link">Go to the top</a>
                    <div class="content-scroller">
                        <div class="content-wrapper">
                            %s
                            %s
                            %s
                            %s
                            %s
                        </div>
                        %s
                    </div>
                </div>
                """.formatted(
                templateFragmentAssembler.assembleFragment("photography/fragments/menu.html"),
                templateFragmentAssembler.assembleFragment("photography/fragments/introduction.html"),
                templateFragmentAssembler.assembleFragment("photography/fragments/about.html"),
                templateFragmentAssembler.assembleFragment("photography/fragments/portfolio.html"),
                templateFragmentAssembler.assembleFragment("photography/fragments/contact.html"),
                templateFragmentAssembler.assembleFragment("photography/fragments/thankyou.html"),
                templateFragmentAssembler.assembleFragment("photography/fragments/navigation-overlay.html")));

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
                    <meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1">
                    <meta name="robots" content="INDEX, FOLLOW">
                    <meta http-equiv="Cache-Control" content="no-store, no-cache, must-revalidate, max-age=0">
                    <meta http-equiv="Pragma" content="no-cache">
                    <meta http-equiv="Expires" content="0">
                    <title>%s</title>
                    <link rel="shortcut icon" href="favicon.ico">
                    <link href="css/google-fonts.css" rel="stylesheet" type="text/css">
                    <link rel="stylesheet" type="text/css" href="css/scroll.css">
                    <link rel="stylesheet" type="text/css" href="css/style.css">
                    <link rel="stylesheet" type="text/css" href="css/font-awesome.css">
                    <link rel="stylesheet" type="text/css" href="css/portfolio.css">
                    <link rel="stylesheet" type="text/css" href="css/carousel.css">
                    <link rel="stylesheet" type="text/css" href="css/responsive.css">
                    <link rel="stylesheet" type="text/css" href="css/photography-custom.css">
                    <script type="text/javascript" src="js/jquery-2.0.3.min.js"></script>
                    <script type="text/javascript" src="js/sendemail.js"></script>
                    <script type="text/javascript" src="js/progressbar.js"></script>
                    <script src="js/modernizr.custom.js"></script>
                </head>
                <body>
                    <div id="app-root">%s</div>
                    <script>window.__REACT_PAGE__ = %s;</script>
                    <script src="/webjars/react/18.2.0/umd/react.production.min.js"></script>
                    <script src="/webjars/react-dom/18.2.0/umd/react-dom.production.min.js"></script>
                    <script src="/react/react-shell.js"></script>
                    <script type="text/javascript" src="js/jquery.min.js"></script>
                    <script type="text/javascript" src="js/head.min.js"></script>
                    <script type="text/javascript" src="js/carousel.js"></script>
                    <script type="text/javascript" src="js/jquery.easypiechart.js"></script>
                    <script type="text/javascript" src="js/text.rotator.js"></script>
                    <script>
                        head.js(
                            { mousewheel: "/photography/js/jquery.mousewheel.js" },
                            { mwheelIntent: "/photography/js/mwheelIntent.js" },
                            { jScrollPane: "/photography/js/jquery.jscrollpane.min.js" },
                            { history: "/photography/js/jquery.history.js" },
                            { stringLib: "/photography/js/core.string.js" },
                            { easing: "/photography/js/jquery.easing.1.3.js" },
                            { smartresize: "/photography/js/jquery.smartresize.js" },
                            { page: "/photography/js/jquery.page.js" }
                        );
                    </script>
                    <script type="text/javascript" src="js/jquery.fitvids.js"></script>
                    <script src="js/settings.js"></script>
                    <script src="js/gallery-navigation.js"></script>
                    <script src="js/photography-portfolio.js"></script>
                </body>
                </html>
                """.formatted(title, markup, toInitialPageJson(title, "", markup));

        return ResponseEntity.ok()
                .contentType(HTML_UTF8)
                .body(html);
    }

    private String wrapForReact(String pageMarkup) {
        return """
                <div data-react-page style="display: contents;">
                %s
                </div>
                """.formatted(pageMarkup.strip());
    }

    private String toInitialPageJson(String title, String bodyClass, String markup) {
        try {
            return objectMapper.writeValueAsString(new InitialPagePayload(title, bodyClass, markup));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize initial React page payload", exception);
        }
    }

    private record InitialPagePayload(String title, String bodyClass, String markup) {
    }
}

; (function ($) {

    $(document).ready(function () {

        //========== SIDEBAR/SEARCH AREA ============= //
        $(".hamburger_menu").on("click", function (e) {
            e.preventDefault();
            $(".slide-bar").toggleClass("show");
            $("body").addClass("on-side");
            $('.body-overlay').addClass('active');
            $(this).addClass('active');
        });
        $(".close-mobile-menu > a").on("click", function (e) {
            e.preventDefault();
            $(".slide-bar").removeClass("show");
            $("body").removeClass("on-side");
            $('.body-overlay').removeClass('active');
            $('.hamburger_menu').removeClass('active');
        });
        //========== SIDEBAR/SEARCH AREA ============= //

        //========== PAGE PROGRESS STARTS ============= // 
        var progressPath = document.querySelector(".progress-wrap path");
        var pathLength = progressPath.getTotalLength();
        progressPath.style.transition = progressPath.style.WebkitTransition =
            "none";
        progressPath.style.strokeDasharray = pathLength + " " + pathLength;
        progressPath.style.strokeDashoffset = pathLength;
        progressPath.getBoundingClientRect();
        progressPath.style.transition = progressPath.style.WebkitTransition =
            "stroke-dashoffset 10ms linear";
        var updateProgress = function () {
            var scroll = $(window).scrollTop();
            var height = $(document).height() - $(window).height();
            var progress = pathLength - (scroll * pathLength) / height;
            progressPath.style.strokeDashoffset = progress;
        };
        updateProgress();
        $(window).scroll(updateProgress);
        var offset = 50;
        var duration = 550;
        jQuery(window).on("scroll", function () {
            if (jQuery(this).scrollTop() > offset) {
                jQuery(".progress-wrap").addClass("active-progress");
            } else {
                jQuery(".progress-wrap").removeClass("active-progress");
            }
        });
        jQuery(".progress-wrap").on("click", function (event) {
            event.preventDefault();
            jQuery("html, body").animate({ scrollTop: 0 }, duration);
            return false;
        });
        //========== PAGE PROGRESS STARTS ============= // 

        //========== VIDEO POPUP STARTS ============= //
        if ($(".popup-youtube").length > 0) {
            $(".popup-youtube").magnificPopup({
                type: "iframe",
            });
        }
        //========== VIDEO POPUP ENDS ============= //
        AOS.init;
        AOS.init({ disable: 'mobile' });

        //========== NICE SELECT ============= //
        $('select').niceSelect();

    });
    //========== COUNTER UP============= //
    const ucounter = $('.counter');
    if (ucounter.length > 0) {
        ucounter.countUp();
    };

    //========== PRELOADER ============= //
    $(window).on("load", function (event) {
        setTimeout(function () {
            $("#preloader").fadeToggle();
        }, 200);

    });
})(jQuery);

//========== GSAP AREA ============= //
if ($('.reveal').length) { gsap.registerPlugin(ScrollTrigger); let revealContainers = document.querySelectorAll(".reveal"); revealContainers.forEach((container) => { let image = container.querySelector("img"); let tl = gsap.timeline({ scrollTrigger: { trigger: container, toggleActions: "play none none none" } }); tl.set(container, { autoAlpha: 1 }); tl.from(container, 1.5, { xPercent: -100, ease: Power2.out }); tl.from(image, 1.5, { xPercent: 100, scale: 1.3, delay: -1.5, ease: Power2.out }); }); }

// Theme toggle functionality
const toggleButton = document.getElementById('theme-toggle');
if (toggleButton) {
    if (localStorage.getItem('theme') === 'dark') {
        document.body.classList.add('light-mode');
        toggleButton.checked = true;
    }
    toggleButton.addEventListener('change', () => {
        document.body.classList.toggle('light-mode');

        if (document.body.classList.contains('light-mode')) {
            localStorage.setItem('theme', 'light');
        } else {
            localStorage.setItem('theme', 'dark-mode');
        }
    });
}

function renderSkillCards() {
    const skillGrid = document.getElementById('cv-skill-grid');
    if (!skillGrid) {
        return;
    }

    const deviconUrl = (slug, variant = 'original') =>
        `https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/${slug}/${slug}-${variant}.svg`;
    const simpleIconUrl = (slug, color = 'ffffff') =>
        `https://cdn.simpleicons.org/${slug}/${color}`;
    const iconifyUrl = (name, color = 'ffffff') =>
        `https://api.iconify.design/material-symbols/${name}.svg?color=${color}`;

    const skillCatalog = [
        {
            category: 'Lenguajes y runtimes',
            items: [
                { name: 'Java', score: 100, icon: deviconUrl('java'), link: 'https://openjdk.org/' },
                { name: 'Python', score: 90, icon: deviconUrl('python'), link: 'https://www.python.org/' },
                { name: 'Node.js', score: 70, icon: deviconUrl('nodejs'), link: 'https://nodejs.org/' },
                { name: 'Kotlin', score: 50, icon: deviconUrl('kotlin'), link: 'https://kotlinlang.org/' },
                { name: 'Jakarta EE', score: 75, icon: deviconUrl('java'), link: 'https://jakarta.ee/' },
                { name: 'PL/SQL', score: 75, icon: deviconUrl('oracle'), link: 'https://www.oracle.com/database/technologies/appdev/plsql.html' }
            ]
        },
        {
            category: 'Frameworks y stack de APIs',
            items: [
                { name: 'Spring Boot', score: 95, icon: deviconUrl('spring'), link: 'https://spring.io/projects/spring-boot/' },
                { name: 'Spring', score: 95, icon: deviconUrl('spring'), link: 'https://spring.io/' },
                { name: 'Hibernate', score: 70, icon: simpleIconUrl('hibernate'), link: 'https://hibernate.org/' },
                { name: 'FastAPI', score: 75, icon: simpleIconUrl('fastapi'), link: 'https://fastapi.tiangolo.com/' },
                { name: 'Django', score: 80, icon: deviconUrl('django', 'plain'), link: 'https://www.djangoproject.com/' },
                { name: 'Flask', score: 75, icon: simpleIconUrl('flask'), link: 'https://flask.palletsprojects.com/' },
                { name: 'React.js', score: 70, icon: deviconUrl('react'), link: 'https://react.dev/' },
                { name: 'REST', score: 100, icon: iconifyUrl('api'), link: 'https://restfulapi.net/' },
                { name: 'SOAP', score: 85, icon: iconifyUrl('sync-alt'), link: 'https://www.w3.org/TR/soap/' }
            ]
        },
        {
            category: 'Cloud y DevOps',
            items: [
                { name: 'AWS', score: 95, icon: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/amazonwebservices/amazonwebservices-original-wordmark.svg', link: 'https://aws.amazon.com/' },
                { name: 'Azure', score: 75, icon: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/azure/azure-original.svg', link: 'https://azure.microsoft.com/' },
                { name: 'GCP', score: 60, icon: deviconUrl('googlecloud'), link: 'https://cloud.google.com/' },
                { name: 'Terraform', score: 94, icon: deviconUrl('terraform'), link: 'https://developer.hashicorp.com/terraform' },
                { name: 'Docker', score: 93, icon: deviconUrl('docker'), link: 'https://www.docker.com/' },
                { name: 'Kubernetes', score: 92, icon: deviconUrl('kubernetes', 'plain'), link: 'https://kubernetes.io/' },
                { name: 'Jenkins', score: 87, icon: deviconUrl('jenkins'), link: 'https://www.jenkins.io/' },
                { name: 'Git', score: 90, icon: deviconUrl('git'), link: 'https://git-scm.com/' },
                { name: 'Splunk', score: 83, icon: simpleIconUrl('splunk'), link: 'https://www.splunk.com/' },
                { name: 'Dynatrace', score: 78, icon: simpleIconUrl('dynatrace'), link: 'https://www.dynatrace.com/' },
                { name: 'Linux', score: 88, icon: deviconUrl('linux'), link: 'https://www.linux.org/' },
                { name: 'API Gateway', score: 90, icon: iconifyUrl('api'), link: 'https://aws.amazon.com/api-gateway/' },
                { name: 'Kinesis', score: 80, icon: iconifyUrl('sync-alt'), link: 'https://aws.amazon.com/kinesis/' },
            ]
        },
        {
            category: 'Bases de datos y plataformas',
            items: [
                { name: 'MongoDB', score: 88, icon: deviconUrl('mongodb'), link: 'https://www.mongodb.com/' },
                { name: 'Amazon DocumentDB', score: 86, icon: iconifyUrl('database'), link: 'https://aws.amazon.com/documentdb/' },
                { name: 'Oracle', score: 86, icon: deviconUrl('oracle'), link: 'https://www.oracle.com/database/' },
                { name: 'Oracle FLEXCUBE', score: 83, icon: deviconUrl('oracle'), link: 'https://www.oracle.com/financial-services/banking/flexcube/' },
                { name: 'PostgreSQL', score: 84, icon: deviconUrl('postgresql'), link: 'https://www.postgresql.org/' },
                { name: 'Redis', score: 60, icon: deviconUrl('redis'), link: 'https://redis.io/' },
                { name: 'Pentaho', score: 60, icon: 'https://upload.wikimedia.org/wikipedia/commons/8/86/Pentho_logo_-1.jpg', link: 'https://www.hitachivantara.com/en-us/products/data-management-analytics/pentaho-platform.html' },
                { name: 'GoAnywhere', score: 76, icon: iconifyUrl('sync-alt'), link: 'https://www.goanywhere.com/' },
                { name: 'DataStage', score: 82, icon: iconifyUrl('database'), link: 'https://www.ibm.com/products/datastage' },
                { name: 'Power BI', score: 81, icon: 'https://api.iconify.design/logos/microsoft-power-bi.svg', link: 'https://www.microsoft.com/power-platform/products/power-bi' },
                { name: 'Salesforce Marketing Cloud', score: 85, icon: 'https://api.iconify.design/devicon-plain/salesforce.svg', link: 'https://www.salesforce.com/products/marketing-cloud/' }
            ]
        },
        {
            category: 'Prácticas y dominios',
            items: [
                { name: 'DDD', score: 87, icon: iconifyUrl('schema'), link: 'https://martinfowler.com/bliki/DomainDrivenDesign.html' },
                { name: 'Scrum', score: 100, icon: iconifyUrl('autorenew'), link: 'https://www.scrum.org/resources/what-is-scrum' },
                { name: 'Scrum Master', score: 90, icon: iconifyUrl('badge'), link: 'https://www.scrum.org/professional-scrum-product-owner-i-certification' },
                { name: 'Batch Processing', score: 83, icon: iconifyUrl('view-stream'), link: 'https://en.wikipedia.org/wiki/Batch_processing' },
                { name: 'QA / Testing', score: 70, icon: iconifyUrl('fact-check'), link: 'https://en.wikipedia.org/wiki/Software_quality_assurance' },
                { name: 'Core Banking', score: 90, icon: iconifyUrl('account-balance'), link: 'https://en.wikipedia.org/wiki/Core_banking' },
                { name: 'ETL', score: 86, icon: iconifyUrl('sync-alt'), link: 'https://en.wikipedia.org/wiki/Extract,_transform,_load' },
                { name: 'CRM Integration', score: 90, icon: iconifyUrl('hub'), link: 'https://www.salesforce.com/products/platform/integration/' },
                { name: 'Microservices', score: 91, icon: iconifyUrl('account-tree'), link: 'https://microservices.io/' }
            ]
        }
    ];

    const markup = [];
    skillCatalog.forEach((group, groupIndex) => {
        markup.push(`
            <div class="col-12" data-aos="fade-left" data-aos-duration="${900 + (groupIndex * 100)}">
                <h4 class="cv-skill-category-title">${group.category}</h4>
            </div>
        `);

        group.items.forEach((skill, itemIndex) => {
            const duration = 900 + ((itemIndex % 6) * 100);
            markup.push(`
                <div class="col-lg-4 col-md-6" data-aos="zoom-out" data-aos-duration="${duration}">
                    <a class="cv-skill-card-link" href="${skill.link}" target="_blank" rel="noopener noreferrer">
                        <div class="skill-boxarea">
                            <div class="icons">
                                <img class="cv-skill-logo" src="${skill.icon}" alt="${skill.name} logo">
                            </div>
                            <div class="space24"></div>
                            <h3><span class="counter">${skill.score}</span>%</h3>
                            <div class="space16"></div>
                            <p>${skill.name}</p>
                        </div>
                    </a>
                </div>
            `);
        });
    });

    skillGrid.innerHTML = markup.join('');

    if (window.jQuery && typeof window.jQuery.fn.countUp === 'function') {
        window.jQuery(skillGrid).find('.counter').countUp();
    }

    if (window.AOS) {
        window.requestAnimationFrame(() => {
            if (typeof window.AOS.refreshHard === 'function') {
                window.AOS.refreshHard();
            } else if (typeof window.AOS.refresh === 'function') {
                window.AOS.refresh();
            }
        });
    }
}

renderSkillCards();

function initPortfolioLightbox() {
    const lightbox = document.getElementById('cv-portfolio-lightbox');
    const lightboxImage = document.getElementById('cv-portfolio-lightbox-image');
    const lightboxStage = document.getElementById('cv-portfolio-lightbox-stage');
    const closeButton = lightbox?.querySelector('.cv-portfolio-lightbox-close');
    const toolButtons = lightbox?.querySelectorAll('[data-lightbox-action]');
    const triggers = document.querySelectorAll('[data-portfolio-lightbox]');

    if (!lightbox || !lightboxImage || !lightboxStage || !closeButton || !toolButtons.length || !triggers.length) {
        return;
    }

    let scale = 1;
    let translateX = 0;
    let translateY = 0;
    let isDragging = false;
    let dragStartX = 0;
    let dragStartY = 0;
    let previousFocus = null;

    const applyTransform = () => {
        lightboxImage.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scale})`;
        lightboxStage.classList.toggle('is-dragging', isDragging);
    };

    const resetView = () => {
        scale = 1;
        translateX = 0;
        translateY = 0;
        isDragging = false;
        applyTransform();
    };

    const clampZoom = (nextScale) => Math.min(Math.max(nextScale, 1), 4);

    const updateZoom = (delta) => {
        const nextScale = clampZoom(scale + delta);
        scale = nextScale;
        if (scale === 1) {
            translateX = 0;
            translateY = 0;
        }
        applyTransform();
    };

    const openLightbox = (sourceImage) => {
        if (!sourceImage) {
            return;
        }

        previousFocus = document.activeElement;
        lightboxImage.src = sourceImage.currentSrc || sourceImage.src;
        lightboxImage.alt = sourceImage.alt || 'Imagen previa del proyecto';
        resetView();
        lightbox.classList.add('is-open');
        lightbox.setAttribute('aria-hidden', 'false');
        document.body.classList.add('cv-lightbox-open');
        closeButton.focus();
    };

    const closeLightbox = () => {
        lightbox.classList.remove('is-open');
        lightbox.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('cv-lightbox-open');
        lightboxImage.removeAttribute('src');
        lightboxImage.removeAttribute('alt');
        resetView();

        if (previousFocus && typeof previousFocus.focus === 'function') {
            previousFocus.focus();
        }
    };

    triggers.forEach((trigger) => {
        trigger.addEventListener('click', () => {
            const image = trigger.querySelector('img');
            openLightbox(image);
        });
    });

    closeButton.addEventListener('click', closeLightbox);

    toolButtons.forEach((button) => {
        button.addEventListener('click', () => {
            const action = button.getAttribute('data-lightbox-action');
            if (action === 'zoom-in') {
                updateZoom(0.2);
            } else if (action === 'zoom-out') {
                updateZoom(-0.2);
            } else if (action === 'reset') {
                resetView();
            }
        });
    });

    lightboxStage.addEventListener('wheel', (event) => {
        if (!lightbox.classList.contains('is-open')) {
            return;
        }

        event.preventDefault();
        updateZoom(event.deltaY < 0 ? 0.2 : -0.2);
    }, { passive: false });

    lightboxStage.addEventListener('pointerdown', (event) => {
        if (!lightbox.classList.contains('is-open') || scale <= 1) {
            return;
        }

        isDragging = true;
        dragStartX = event.clientX - translateX;
        dragStartY = event.clientY - translateY;
        lightboxStage.setPointerCapture(event.pointerId);
        applyTransform();
    });

    lightboxStage.addEventListener('pointermove', (event) => {
        if (!isDragging) {
            return;
        }

        translateX = event.clientX - dragStartX;
        translateY = event.clientY - dragStartY;
        applyTransform();
    });

    const stopDragging = (event) => {
        if (!isDragging) {
            return;
        }

        isDragging = false;
        if (event && lightboxStage.hasPointerCapture && lightboxStage.hasPointerCapture(event.pointerId)) {
            lightboxStage.releasePointerCapture(event.pointerId);
        }
        applyTransform();
    };

    lightboxStage.addEventListener('pointerup', stopDragging);
    lightboxStage.addEventListener('pointercancel', stopDragging);
    lightboxStage.addEventListener('pointerleave', stopDragging);

    document.addEventListener('keydown', (event) => {
        if (!lightbox.classList.contains('is-open')) {
            return;
        }

        if (event.key === 'Escape') {
            closeLightbox();
        } else if (event.key === '+' || event.key === '=') {
            event.preventDefault();
            updateZoom(0.2);
        } else if (event.key === '-') {
            event.preventDefault();
            updateZoom(-0.2);
        }
    });
}

initPortfolioLightbox();

function initContactModal() {
    const modal = document.getElementById('cv-contact-modal');
    const triggers = document.querySelectorAll('[data-contact-choice-trigger]');
    const choiceView = modal?.querySelector('[data-contact-view="choice"]');
    const formView = modal?.querySelector('[data-contact-view="form"]');
    const closeControls = modal?.querySelectorAll('[data-contact-close]');
    const externalLink = modal?.querySelector('[data-contact-external]');
    const openFormButton = modal?.querySelector('[data-contact-open-form]');
    const backButton = modal?.querySelector('[data-contact-back]');
    const resultView = modal?.querySelector('[data-contact-view="result"]');
    const form = modal?.querySelector('#cv-contact-form');
    const status = modal?.querySelector('[data-contact-form-status]');
    const submitButton = modal?.querySelector('[data-contact-submit]');
    const submitLabel = modal?.querySelector('.cv-contact-submit-label');
    const resultContainer = modal?.querySelector('.cv-contact-result');
    const resultKicker = modal?.querySelector('[data-contact-result-kicker]');
    const resultTitle = modal?.querySelector('[data-contact-result-title]');
    const resultCopy = modal?.querySelector('[data-contact-result-copy]');
    const resultIcon = modal?.querySelector('[data-contact-result-icon]');
    const resultCloseButton = modal?.querySelector('[data-contact-result-close]');
    const resultRetryButton = modal?.querySelector('[data-contact-result-retry]');

    if (!modal || !choiceView || !formView || !resultView || !externalLink || !openFormButton || !backButton || !form || !status || !submitButton || !submitLabel || !resultContainer || !resultKicker || !resultTitle || !resultCopy || !resultIcon || !resultCloseButton || !resultRetryButton || !triggers.length) {
        return;
    }

    const fields = {
        fromEmail: form.elements.namedItem('fromEmail'),
        subject: form.elements.namedItem('subject'),
        message: form.elements.namedItem('message')
    };
    const errorElements = new Map(
        Array.from(modal.querySelectorAll('[data-field-error]')).map((element) => [element.dataset.fieldError, element])
    );
    const apiUrl = modal.dataset.contactApi || '';
    const defaultMailto = modal.dataset.mailto || externalLink.getAttribute('href') || 'mailto:alejo.valenciarivera@gmail.com';
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    let previousFocus = null;

    const setView = (viewName) => {
        const showChoice = viewName === 'choice';
        const showForm = viewName === 'form';
        const showResult = viewName === 'result';
        choiceView.hidden = !showChoice;
        formView.hidden = !showForm;
        resultView.hidden = !showResult;
        choiceView.classList.toggle('is-active', showChoice);
        formView.classList.toggle('is-active', showForm);
        resultView.classList.toggle('is-active', showResult);
    };

    const setStatus = (message, state) => {
        status.textContent = message || '';
        status.classList.remove('is-success', 'is-error');
        if (state) {
            status.classList.add(`is-${state}`);
        }
    };

    const clearErrors = () => {
        Object.values(fields).forEach((field) => field?.classList.remove('is-invalid'));
        errorElements.forEach((element) => {
            element.textContent = '';
        });
    };

    const setFieldError = (fieldName, message) => {
        const field = fields[fieldName];
        const errorElement = errorElements.get(fieldName);
        if (field) {
            field.classList.add('is-invalid');
        }
        if (errorElement) {
            errorElement.textContent = message;
        }
    };

    const validateForm = () => {
        const payload = {
            fromEmail: String(fields.fromEmail?.value || '').trim(),
            subject: String(fields.subject?.value || '').trim(),
            message: String(fields.message?.value || '').trim()
        };
        const errors = {};

        if (!payload.fromEmail) {
            errors.fromEmail = 'Tu correo es obligatorio.';
        } else if (!emailPattern.test(payload.fromEmail)) {
            errors.fromEmail = 'Ingresa un correo válido.';
        }

        if (!payload.subject) {
            errors.subject = 'El asunto es obligatorio.';
        }

        if (!payload.message) {
            errors.message = 'El mensaje es obligatorio.';
        }

        return { payload, errors };
    };

    const setLoading = (isLoading) => {
        submitButton.disabled = isLoading;
        submitButton.classList.toggle('is-loading', isLoading);
        submitLabel.textContent = isLoading
            ? 'Enviando...'
            : 'Enviar mensaje';
    };

    const setResultState = (state, message) => {
        const success = state === 'success';
        resultContainer.setAttribute('data-contact-result-state', state);
        resultIcon.innerHTML = success
            ? '<i class="fa-solid fa-check"></i>'
            : '<i class="fa-solid fa-triangle-exclamation"></i>';
        resultKicker.textContent = success
            ? 'Mensaje enviado'
            : 'Mensaje no enviado';
        resultTitle.textContent = success
            ? 'Tu mensaje fue enviado correctamente.'
            : 'No fue posible enviar tu mensaje.';
        resultCopy.textContent = message || (success
            ? 'Alejandro ya recibió tu mensaje y puede responderte directamente a tu correo.'
            : 'Inténtalo nuevamente en un momento o usa la opción de correo externo.');
        resultRetryButton.hidden = success;
    };

    const openModal = (trigger) => {
        previousFocus = document.activeElement;
        externalLink.setAttribute('href', trigger?.getAttribute('href') || defaultMailto);
        clearErrors();
        setStatus('', '');
        setLoading(false);
        setView('choice');
        modal.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
        document.body.classList.add('cv-contact-modal-open');
        openFormButton.focus();
    };

    const closeModal = () => {
        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('cv-contact-modal-open');
        setView('choice');
        clearErrors();
        setStatus('', '');
        setLoading(false);
        form.reset();

        if (previousFocus && typeof previousFocus.focus === 'function') {
            previousFocus.focus();
        }
    };

    triggers.forEach((trigger) => {
        trigger.addEventListener('click', (event) => {
            event.preventDefault();
            openModal(trigger);
        });
    });

    closeControls.forEach((control) => {
        control.addEventListener('click', closeModal);
    });

    openFormButton.addEventListener('click', () => {
        clearErrors();
        setStatus('', '');
        setView('form');
        fields.fromEmail?.focus();
    });

    backButton.addEventListener('click', () => {
        clearErrors();
        setStatus('', '');
        setView('choice');
        openFormButton.focus();
    });

    resultCloseButton.addEventListener('click', closeModal);

    resultRetryButton.addEventListener('click', () => {
        clearErrors();
        setStatus('', '');
        setView('form');
        fields.fromEmail?.focus();
    });

    externalLink.addEventListener('click', () => {
        closeModal();
    });

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        clearErrors();
        setStatus('', '');

        const { payload, errors } = validateForm();
        if (Object.keys(errors).length > 0) {
            Object.entries(errors).forEach(([fieldName, message]) => {
                setFieldError(fieldName, message);
            });
            setStatus('Corrige los campos marcados e inténtalo nuevamente.', 'error');
            return;
        }

        if (!apiUrl) {
            setStatus('Falta la URL del API de contacto en la configuración de la página.', 'error');
            return;
        }

        setLoading(true);

        try {
            const response = await fetch(apiUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const result = await response.json().catch(() => ({}));

            if (!response.ok) {
                if (result.errors && typeof result.errors === 'object') {
                    Object.entries(result.errors).forEach(([fieldName, message]) => {
                        setFieldError(fieldName, String(message));
                    });
                }
                setResultState('error', result.message || 'No fue posible enviar tu mensaje.');
                setView('result');
                return;
            }

            form.reset();
            setResultState('success', result.message || 'Tu mensaje fue enviado correctamente.');
            setView('result');
        } catch (error) {
            setResultState('error', 'La solicitud no pudo comunicarse con el servidor. Inténtalo nuevamente.');
            setView('result');
        } finally {
            setLoading(false);
        }
    });

    document.addEventListener('keydown', (event) => {
        if (!modal.classList.contains('is-open')) {
            return;
        }

        if (event.key === 'Escape') {
            closeModal();
        }
    });
}

initContactModal();

function fitHeroTitleLine(element, maxWidth, minFontPx, allowScaleFallback) {
    if (!element) {
        return;
    }

    element.style.fontSize = '';
    element.style.transform = '';
    element.style.transformOrigin = '';

    let currentFontSize = parseFloat(window.getComputedStyle(element).fontSize);
    while (element.scrollWidth > maxWidth && currentFontSize > minFontPx) {
        currentFontSize -= 0.5;
        element.style.fontSize = `${currentFontSize}px`;
    }

    if (allowScaleFallback && element.scrollWidth > maxWidth) {
        const scaleX = Math.max(maxWidth / element.scrollWidth, 0.82);
        element.style.transform = `scaleX(${scaleX})`;
        element.style.transformOrigin = 'right center';
    }
}

function fitHeroTitleRow(heading, mainLine, accentLine, minMainFontPx, minAccentFontPx) {
    if (!heading || !mainLine || !accentLine) {
        return;
    }

    let mainFontSize = parseFloat(window.getComputedStyle(mainLine).fontSize);
    let accentFontSize = parseFloat(window.getComputedStyle(accentLine).fontSize);

    while (heading.scrollWidth > heading.clientWidth && accentFontSize > minAccentFontPx) {
        accentFontSize -= 0.5;
        accentLine.style.fontSize = `${accentFontSize}px`;
    }

    while (heading.scrollWidth > heading.clientWidth && mainFontSize > minMainFontPx) {
        mainFontSize -= 0.5;
        mainLine.style.fontSize = `${mainFontSize}px`;
    }

    if (heading.scrollWidth > heading.clientWidth) {
        const headingStyles = window.getComputedStyle(heading);
        const gap = parseFloat(headingStyles.columnGap || headingStyles.gap || '0');
        const availableAccentWidth = Math.max(
            heading.clientWidth - mainLine.getBoundingClientRect().width - gap,
            140
        );
        fitHeroTitleLine(accentLine, availableAccentWidth, minAccentFontPx, true);
    }
}

function updateHeroTitleFit() {
    const heading = document.querySelector('.cv-hero-title-secondary');
    const mainLine = heading?.querySelector('.cv-hero-title-secondary-main');
    const accentLine = heading?.querySelector('.cv-hero-title-secondary-accent');

    if (!heading || !mainLine || !accentLine) {
        return;
    }

    mainLine.style.fontSize = '';
    mainLine.style.transform = '';
    mainLine.style.transformOrigin = '';
    accentLine.style.fontSize = '';
    accentLine.style.transform = '';
    accentLine.style.transformOrigin = '';

    if (window.innerWidth > 1199) {
        fitHeroTitleRow(heading, mainLine, accentLine, 34, 28);
        return;
    }

    const gutterAllowance = window.innerWidth <= 767 ? 28 : 20;
    const maxWidth = Math.max(heading.clientWidth - gutterAllowance, 140);

    fitHeroTitleLine(mainLine, maxWidth, window.innerWidth <= 575 ? 16 : 18, false);
    fitHeroTitleLine(accentLine, maxWidth, window.innerWidth <= 575 ? 11 : 12, true);
}

function fitExperienceRoleTitle(roleElement) {
    if (!roleElement) {
        return;
    }

    roleElement.style.fontSize = '';
    roleElement.style.transform = '';
    roleElement.style.transformOrigin = '';

    const maxWidth = roleElement.clientWidth;
    if (!maxWidth) {
        return;
    }

    const getMaxHeight = () => {
        const styles = window.getComputedStyle(roleElement);
        const lineHeight = parseFloat(styles.lineHeight) || parseFloat(styles.fontSize) * 1.16;
        return lineHeight * 2 + 2;
    };

    let currentFontSize = parseFloat(window.getComputedStyle(roleElement).fontSize);
    let maxHeight = getMaxHeight();
    while ((roleElement.scrollWidth > maxWidth || roleElement.scrollHeight > maxHeight) && currentFontSize > 10) {
        currentFontSize -= 0.5;
        roleElement.style.fontSize = `${currentFontSize}px`;
        maxHeight = getMaxHeight();
    }

    if (roleElement.scrollWidth > maxWidth || roleElement.scrollHeight > maxHeight) {
        const scaleX = Math.max(maxWidth / roleElement.scrollWidth, 0.82);
        roleElement.style.transform = `scaleX(${scaleX})`;
        roleElement.style.transformOrigin = 'left center';
    }
}

function updateExperienceRoleFit() {
    document.querySelectorAll('.cv-featured-role').forEach(fitExperienceRoleTitle);
}

const scheduleHeroTitleFit = () => {
    window.requestAnimationFrame(updateHeroTitleFit);
};

scheduleHeroTitleFit();
window.addEventListener('resize', scheduleHeroTitleFit, { passive: true });

const scheduleExperienceRoleFit = () => {
    window.requestAnimationFrame(updateExperienceRoleFit);
};

scheduleExperienceRoleFit();
window.addEventListener('resize', scheduleExperienceRoleFit, { passive: true });

if (document.fonts && document.fonts.ready) {
    document.fonts.ready.then(() => {
        scheduleHeroTitleFit();
        scheduleExperienceRoleFit();
    });
}

if (window.ResizeObserver) {
    const heroResizeTarget = document.querySelector('.main-hero-area5 .hero-heading-area');
    if (heroResizeTarget) {
        const heroTitleObserver = new ResizeObserver(scheduleHeroTitleFit);
        heroTitleObserver.observe(heroResizeTarget);
    }

    document.querySelectorAll('.cv-experience-top').forEach((experienceTop) => {
        const experienceObserver = new ResizeObserver(scheduleExperienceRoleFit);
        experienceObserver.observe(experienceTop);
    });
}

// UPDATE: I was able to get this working again... Enjoy!
var cursor = document.querySelector('.procus-cursor');
var cursorinner = document.querySelector('.procus-cursor2');
var a = document.querySelectorAll('a');

if (cursor && cursorinner) {
    var cursorContrastSelector = [
        '.main-hero-area5 .list-group a',
        '.main-hero-area5 .hero-heading-area .main-btn-area a',
        '.vl-btn1',
        '.progress-wrap'
    ].join(', ');

    function updateCursorContrast(target) {
        var contrastTarget = target && target.closest ? target.closest(cursorContrastSelector) : null;
        cursor.classList.toggle('cursor-contrast', Boolean(contrastTarget));
        cursorinner.classList.toggle('cursor-contrast', Boolean(contrastTarget));
    }

    document.addEventListener('mousemove', function (e) {
        var hoverTarget = document.elementFromPoint(e.clientX, e.clientY);
        cursor.style.transform = `translate3d(calc(${e.clientX}px - 50%), calc(${e.clientY}px - 50%), 0)`;
        updateCursorContrast(hoverTarget);
    });

    document.addEventListener('mousemove', function (e) {
        var x = e.clientX;
        var y = e.clientY;
        cursorinner.style.left = x + 'px';
        cursorinner.style.top = y + 'px';
    });

    document.addEventListener('mousedown', function () {
        cursor.classList.add('click');
        cursorinner.classList.add('cursorinnerhover');
    });

    document.addEventListener('mouseup', function () {
        cursor.classList.remove('click');
        cursorinner.classList.remove('cursorinnerhover');
    });

    a.forEach(item => {
        item.addEventListener('mouseover', () => {
            cursor.classList.add('hover');
        });
        item.addEventListener('mouseleave', () => {
            cursor.classList.remove('hover');
        });
    });
}

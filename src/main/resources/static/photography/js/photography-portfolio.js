(function () {
    'use strict';

    var projectsPanel = document.getElementById('pf-projects-panel');
    var photosPanel   = document.getElementById('pf-photos-panel');
    var photosTitle   = document.getElementById('pf-photos-title');
    var openBtn       = document.getElementById('pf-open-projects');
    var carousel      = document.querySelector('.pf-carousel');
    var mainImg       = document.getElementById('pf-main-img');
    var filmstrip     = document.getElementById('pf-filmstrip');
    var prevBtn       = document.getElementById('pf-carousel-prev');
    var nextBtn       = document.getElementById('pf-carousel-next');
    var lightbox      = document.getElementById('pf-lightbox');
    var lbImg         = document.getElementById('pf-lb-img');
    var lbStage       = document.getElementById('pf-lb-stage');
    var lbZoomInBtn   = document.getElementById('pf-lb-zoomin');
    var lbZoomOutBtn  = document.getElementById('pf-lb-zoomout');
    var lbCloseBtn    = document.getElementById('pf-lb-close');
    var lbZoomLabel   = document.getElementById('pf-lb-zoom-label');

    if (!projectsPanel || !photosPanel || !openBtn || !carousel || !mainImg || !filmstrip || !prevBtn || !nextBtn || !lightbox || !lbImg || !lbStage || !lbZoomInBtn || !lbZoomOutBtn || !lbCloseBtn || !lbZoomLabel) return;

    // Move all overlay panels to body to escape jScrollPane's overflow:hidden
    if (projectsPanel.parentNode !== document.body) document.body.appendChild(projectsPanel);
    if (photosPanel.parentNode !== document.body) document.body.appendChild(photosPanel);
    if (lightbox.parentNode !== document.body) document.body.appendChild(lightbox);

    // ── Carousel state ───────────────────────────────────────────────────
    var currentPhotos = [];
    var currentIndex  = 0;

    function isOpen(element, openClass) {
        return element.classList.contains(openClass);
    }

    function syncModalState() {
        document.body.classList.toggle(
            'pf-modal-open',
            isOpen(projectsPanel, 'pf-panel--open') ||
            isOpen(photosPanel, 'pf-panel--open') ||
            isOpen(lightbox, 'pf-lightbox--open')
        );
    }

    function openPanel(panel) {
        panel.classList.add('pf-panel--open');
        panel.setAttribute('aria-hidden', 'false');
        syncModalState();
    }

    function closePanel(panel) {
        panel.classList.remove('pf-panel--open');
        panel.setAttribute('aria-hidden', 'true');
        syncModalState();
    }

    function goTo(index) {
        if (currentPhotos.length === 0) return;
        currentIndex = index;

        mainImg.style.transition = 'opacity 0.22s ease';
        mainImg.style.opacity    = '0';
        setTimeout(function () {
            mainImg.src           = currentPhotos[currentIndex];
            mainImg.alt           = photosTitle.textContent;
            mainImg.style.opacity = '1';
        }, 180);

        prevBtn.disabled = currentIndex === 0;
        nextBtn.disabled = currentIndex === currentPhotos.length - 1;

        // Filmstrip: slots [i-2, i-1, i, i+1, i+2]
        filmstrip.innerHTML = '';
        for (var offset = -2; offset <= 2; offset++) {
            var slotIndex = currentIndex + offset;
            var slot = document.createElement('div');
            if (slotIndex >= 0 && slotIndex < currentPhotos.length) {
                slot.className = 'pf-film-thumb' + (offset === 0 ? ' pf-film-thumb--active' : '');
                var thumb = document.createElement('img');
                thumb.src     = currentPhotos[slotIndex];
                thumb.alt     = photosTitle.textContent;
                thumb.loading = 'lazy';
                (function (idx) {
                    slot.addEventListener('click', function () { goTo(idx); });
                }(slotIndex));
                slot.appendChild(thumb);
            } else {
                slot.className = 'pf-film-empty';
            }
            filmstrip.appendChild(slot);
        }
    }

    // ── Carousel controls ────────────────────────────────────────────────
    openBtn.addEventListener('click', function () { openPanel(projectsPanel); });
    openBtn.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            openPanel(projectsPanel);
        }
    });

    document.getElementById('pf-close-projects').addEventListener('click', function () {
        closePanel(projectsPanel);
    });

    document.getElementById('pf-close-photos').addEventListener('click', function () {
        closePanel(photosPanel);
    });

    prevBtn.addEventListener('click', function () {
        if (currentIndex > 0) goTo(currentIndex - 1);
    });

    nextBtn.addEventListener('click', function () {
        if (currentIndex < currentPhotos.length - 1) goTo(currentIndex + 1);
    });

    var swipeStartX = 0;
    var swipeStartY = 0;
    var swipeActive = false;

    carousel.addEventListener('touchstart', function (e) {
        if (e.touches.length !== 1 || currentPhotos.length < 2) return;
        swipeActive = true;
        swipeStartX = e.touches[0].clientX;
        swipeStartY = e.touches[0].clientY;
    }, { passive: true });

    carousel.addEventListener('touchend', function (e) {
        if (!swipeActive || e.changedTouches.length !== 1) return;
        swipeActive = false;

        var dx = e.changedTouches[0].clientX - swipeStartX;
        var dy = e.changedTouches[0].clientY - swipeStartY;
        if (Math.abs(dx) < 44 || Math.abs(dx) < Math.abs(dy) * 1.4) return;

        if (dx < 0 && currentIndex < currentPhotos.length - 1) {
            goTo(currentIndex + 1);
        } else if (dx > 0 && currentIndex > 0) {
            goTo(currentIndex - 1);
        }
    }, { passive: true });

    function openProject(card) {
        var name   = card.getAttribute('data-name');
        var photos = JSON.parse(card.getAttribute('data-photos'));
        photosTitle.textContent = name;
        currentPhotos = photos;
        goTo(0);
        openPanel(photosPanel);
    }

    document.querySelectorAll('.pf-project').forEach(function (card) {
        card.setAttribute('role', 'button');
        card.setAttribute('tabindex', '0');
        card.addEventListener('click', function () { openProject(card); });
        card.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                openProject(card);
            }
        });
    });

    // Click main carousel photo → open lightbox
    mainImg.addEventListener('click', function () {
        if (currentPhotos.length > 0) {
            openLightbox(currentPhotos[currentIndex], photosTitle.textContent);
        }
    });

    // ── Lightbox ─────────────────────────────────────────────────────────
    var lbZoom = 1.0;
    var lbTx   = 0;
    var lbTy   = 0;
    var ZOOM_MIN  = 1.0;
    var ZOOM_MAX  = 2.0;
    var ZOOM_STEP = 0.25;

    var lbDrag        = false;
    var lbDragStartX  = 0;
    var lbDragStartY  = 0;
    var lbDragOriginX = 0;
    var lbDragOriginY = 0;

    function lbApplyTransform(animated) {
        lbImg.style.transition = animated
            ? 'transform 0.25s ease'
            : 'none';
        lbImg.style.transform =
            'translate(' + lbTx + 'px, ' + lbTy + 'px) scale(' + lbZoom + ')';
        lbZoomLabel.textContent = Math.round(lbZoom * 100) + '%';
        lbZoomInBtn.disabled  = lbZoom >= ZOOM_MAX;
        lbZoomOutBtn.disabled = lbZoom <= ZOOM_MIN;
        lbStage.classList.toggle('pf-lightbox__stage--grab', lbZoom > 1 && !lbDrag);
    }

    function openLightbox(src, alt) {
        lbImg.src = src;
        lbImg.alt = alt || '';
        lbZoom    = 1.0;
        lbTx      = 0;
        lbTy      = 0;
        lbApplyTransform(false);
        lightbox.classList.add('pf-lightbox--open');
        lightbox.setAttribute('aria-hidden', 'false');
        syncModalState();
    }

    function closeLightbox() {
        lightbox.classList.remove('pf-lightbox--open');
        lightbox.setAttribute('aria-hidden', 'true');
        syncModalState();
    }

    lbCloseBtn.addEventListener('click', closeLightbox);

    lbZoomInBtn.addEventListener('click', function () {
        if (lbZoom < ZOOM_MAX) {
            lbZoom = Math.min(ZOOM_MAX, Math.round((lbZoom + ZOOM_STEP) * 100) / 100);
            lbApplyTransform(true);
        }
    });

    lbZoomOutBtn.addEventListener('click', function () {
        if (lbZoom > ZOOM_MIN) {
            lbZoom = Math.max(ZOOM_MIN, Math.round((lbZoom - ZOOM_STEP) * 100) / 100);
            if (lbZoom <= ZOOM_MIN) { lbTx = 0; lbTy = 0; }
            lbApplyTransform(true);
        }
    });

    // Mouse drag to pan (only when zoomed in)
    lbStage.addEventListener('mousedown', function (e) {
        if (lbZoom <= 1) return;
        lbDrag        = true;
        lbDragStartX  = e.clientX;
        lbDragStartY  = e.clientY;
        lbDragOriginX = lbTx;
        lbDragOriginY = lbTy;
        lbStage.classList.remove('pf-lightbox__stage--grab');
        lbStage.classList.add('pf-lightbox__stage--grabbing');
        e.preventDefault();
    });

    document.addEventListener('mousemove', function (e) {
        if (!lbDrag) return;
        lbTx = lbDragOriginX + (e.clientX - lbDragStartX);
        lbTy = lbDragOriginY + (e.clientY - lbDragStartY);
        lbImg.style.transition = 'none';
        lbImg.style.transform  =
            'translate(' + lbTx + 'px, ' + lbTy + 'px) scale(' + lbZoom + ')';
    });

    document.addEventListener('mouseup', function () {
        if (!lbDrag) return;
        lbDrag = false;
        lbStage.classList.remove('pf-lightbox__stage--grabbing');
        lbStage.classList.toggle('pf-lightbox__stage--grab', lbZoom > 1);
    });

    // Touch drag to pan
    lbStage.addEventListener('touchstart', function (e) {
        if (lbZoom <= 1 || e.touches.length !== 1) return;
        lbDrag        = true;
        lbDragStartX  = e.touches[0].clientX;
        lbDragStartY  = e.touches[0].clientY;
        lbDragOriginX = lbTx;
        lbDragOriginY = lbTy;
    }, { passive: true });

    lbStage.addEventListener('touchmove', function (e) {
        if (!lbDrag || e.touches.length !== 1) return;
        lbTx = lbDragOriginX + (e.touches[0].clientX - lbDragStartX);
        lbTy = lbDragOriginY + (e.touches[0].clientY - lbDragStartY);
        lbImg.style.transition = 'none';
        lbImg.style.transform  =
            'translate(' + lbTx + 'px, ' + lbTy + 'px) scale(' + lbZoom + ')';
    }, { passive: true });

    lbStage.addEventListener('touchend', function () { lbDrag = false; });

    // ── Keyboard shortcuts ───────────────────────────────────────────────
    document.addEventListener('keydown', function (e) {
        if (lightbox.classList.contains('pf-lightbox--open')) {
            if (e.key === 'Escape')            { closeLightbox(); }
            else if (e.key === '+' || e.key === '=') { lbZoomInBtn.click(); }
            else if (e.key === '-')            { lbZoomOutBtn.click(); }
            return;
        }
        if (photosPanel.classList.contains('pf-panel--open')) {
            if      (e.key === 'ArrowLeft')  { if (currentIndex > 0) goTo(currentIndex - 1); }
            else if (e.key === 'ArrowRight') { if (currentIndex < currentPhotos.length - 1) goTo(currentIndex + 1); }
            else if (e.key === 'Escape')     { closePanel(photosPanel); }
        } else if (projectsPanel.classList.contains('pf-panel--open')) {
            if (e.key === 'Escape') closePanel(projectsPanel);
        }
    });
}());

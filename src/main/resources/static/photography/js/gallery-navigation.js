(function (window, $) {
    "use strict";

    function getChapterOrder() {
        return $("#container .content-wrapper > article[id^='chapter']").map(function () {
            return this.id.replace(/^chapter/, "");
        }).get();
    }

    function getCurrentChapter(chapters) {
        var chapter = new URLSearchParams(window.location.search).get("page");

        if (!chapter || $.inArray(chapter, chapters) === -1) {
            return "introduction";
        }

        return chapter;
    }

    function updateButtons($prev, $next) {
        var chapters = getChapterOrder();
        var current = getCurrentChapter(chapters);
        var currentIndex = $.inArray(current, chapters);

        $prev.toggleClass("is-disabled", currentIndex <= 0);
        $next.toggleClass("is-disabled", currentIndex === -1 || currentIndex >= chapters.length - 1);
    }

    function moveChapter(offset) {
        var chapters = getChapterOrder();
        var current = getCurrentChapter(chapters);
        var currentIndex = $.inArray(current, chapters);
        var targetIndex = currentIndex + offset;

        if (currentIndex === -1 || targetIndex < 0 || targetIndex >= chapters.length || !window.History) {
            return;
        }

        var params = new URLSearchParams(window.location.search);
        params.set("page", chapters[targetIndex]);
        window.History.pushState(null, null, "?" + params.toString());
    }

    function bindNavigation() {
        var $prev = $(".content-nav-prev");
        var $next = $(".content-nav-next");

        if (!$prev.length || !$next.length) {
            return;
        }

        updateButtons($prev, $next);

        $prev.on("click", function (event) {
            event.preventDefault();
            moveChapter(-1);
        });

        $next.on("click", function (event) {
            event.preventDefault();
            moveChapter(1);
        });

        $(window).on("statechange.photographyNav resize.photographyNav", function () {
            updateButtons($prev, $next);
        });
    }

    $(function () {
        if (window.head && typeof window.head.ready === "function") {
            window.head.ready("page", bindNavigation);
            return;
        }

        bindNavigation();
    });
})(window, jQuery);

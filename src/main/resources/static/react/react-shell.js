(function () {
    "use strict";

    if (!window.React || !window.ReactDOM) {
        return;
    }

    var page = window.__REACT_PAGE__;
    var root = document.getElementById("app-root");

    if (!page || !root) {
        return;
    }

    if (page.title) {
        document.title = page.title;
    }

    document.body.className = page.bodyClass || "";

    var React = window.React;
    var ReactDOM = window.ReactDOM;

    function MarkupView() {
        return React.createElement("div", {
            "data-react-page": "",
            style: { display: "contents" },
            dangerouslySetInnerHTML: { __html: page.markup }
        });
    }

    if (typeof ReactDOM.createRoot === "function") {
        ReactDOM.createRoot(root).render(React.createElement(MarkupView));
        return;
    }

    if (typeof ReactDOM.render === "function") {
        ReactDOM.render(React.createElement(MarkupView), root);
    }
})();

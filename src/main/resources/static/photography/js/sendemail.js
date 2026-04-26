function validateForm() {
    "use strict";

    var isValid = true;
    var name = $.trim($("#name").val());
    var email = $.trim($("#email").val());
    var subject = $.trim($("#subject").val());
    var message = $.trim($("#message").val());
    var payloadMessage = buildMessageBody(name, email, message);

    clearContactFeedback();

    if (!name) {
        markInvalid("#name");
        isValid = false;
    }

    if (!isValidEmail(email)) {
        markInvalid("#email");
        isValid = false;
    }

    if (!subject) {
        markInvalid("#subject");
        isValid = false;
    }

    if (!message || payloadMessage.length > 5000) {
        markInvalid("#message");
        isValid = false;
    }

    if (!isValid) {
        showContactStatus(getContactText("validationMessage", "Please complete the highlighted fields with a valid email address."), false);
    }

    return isValid;
}

function isValidEmail(email) {
    "use strict";
    return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email);
}

function markInvalid(selector) {
    "use strict";
    $(selector).addClass("validation");
}

function clearContactFeedback() {
    "use strict";
    $("#name, #email, #subject, #message").removeClass("validation");
    $("#successmsg").removeClass("is-success is-error").empty();
}

function showContactStatus(message, success) {
    "use strict";
    $("#successmsg")
        .removeClass("is-success is-error")
        .addClass(success ? "is-success" : "is-error")
        .text(message);
}

function buildMessageBody(name, email, message) {
    "use strict";
    var nameLabel = getContactText("messageNameLabel", "Name");
    var emailLabel = getContactText("messageEmailLabel", "Email");
    return nameLabel + ": " + name + "\n" + emailLabel + ": " + email + "\n\n" + message;
}

function getContactEndpoint() {
    "use strict";
    var form = $("#form1");
    return form.data("contact-api") || form.attr("action") || "/api/contact/send?lang=en";
}

function getContactText(key, fallback) {
    "use strict";
    var value = $("#form1").data(key);
    return value || fallback;
}

$(document).ready(function () {
    "use strict";

    $("#form1").on("submit", function (e) {
        e.preventDefault();

        if (!validateForm()) {
            return false;
        }

        var submitButton = $("#button");
        var originalLabel = submitButton.val();
        var name = $.trim($("#name").val());
        var email = $.trim($("#email").val());
        var subject = $.trim($("#subject").val());
        var message = $.trim($("#message").val());

        submitButton.prop("disabled", true).val(getContactText("sendingLabel", "sending..."));

        $.ajax({
            type: "POST",
            url: getContactEndpoint(),
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify({
                fromEmail: email,
                subject: subject,
                message: buildMessageBody(name, email, message)
            }),
            success: function (response) {
                var successMessage = response && response.message
                    ? response.message
                    : getContactText("successMessage", "Thanks. Your message was sent successfully.");

                showContactStatus(successMessage, true);
                $("#form1")[0].reset();
            },
            error: function (xhr) {
                var response = xhr.responseJSON || {};
                var errors = response.errors || {};
                var errorMessage = response.message
                    || getContactText("errorMessage", "Sorry, your message could not be sent right now. Please try again later.");

                if (errors.fromEmail) {
                    markInvalid("#email");
                }
                if (errors.subject) {
                    markInvalid("#subject");
                }
                if (errors.message) {
                    markInvalid("#message");
                }

                showContactStatus(errorMessage, false);
            },
            complete: function () {
                submitButton.prop("disabled", false).val(originalLabel);
            }
        });

        return false;
    });
});

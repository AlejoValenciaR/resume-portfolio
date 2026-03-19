package com.alejandro.cv.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactMessageRequest(
        @NotBlank(message = "{contact.validation.fromEmail.required}")
        @Email(message = "{contact.validation.fromEmail.invalid}")
        @Size(max = 320, message = "{contact.validation.fromEmail.max}")
        String fromEmail,

        @NotBlank(message = "{contact.validation.subject.required}")
        @Size(max = 160, message = "{contact.validation.subject.max}")
        String subject,

        @NotBlank(message = "{contact.validation.message.required}")
        @Size(max = 5000, message = "{contact.validation.message.max}")
        String message
) {
}

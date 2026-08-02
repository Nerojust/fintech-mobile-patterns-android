package com.nerojust.vela.core.security

import timber.log.Timber

class RedactingTree : Timber.DebugTree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        super.log(priority, tag, redact(message), t)
    }

    companion object {
        private val CARD_PATTERN = Regex("""\b\d{12,19}\b|\b\d{4}[ -]\d{4}[ -]\d{4}[ -]\d{1,7}\b""")
        private val LABELED_FIELD_PATTERN = Regex("""(?i)\b(amount|balance|token|authtoken)\s*[:=]\s*\S+""")

        fun redact(message: String): String =
            message
                .replace(CARD_PATTERN, "[REDACTED_CARD]")
                .replace(LABELED_FIELD_PATTERN) { match -> "${match.groupValues[1]}=[REDACTED]" }
    }
}

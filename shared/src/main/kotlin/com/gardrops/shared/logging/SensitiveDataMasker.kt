package com.gardrops.shared.logging

object SensitiveDataMasker {

    private val threeGroupPatterns = listOf(
        // JSON/headers: "authorization": "Bearer abc...", Authorization: "..."
        Regex("""(?i)("?\s*authorization\s*"?\s*:\s*")([^"]+)(")"""),
        // token: "token": "...."
        Regex("""(?i)("?\s*token\s*"?\s*:\s*")([^"]+)(")"""),
        // password/passwd: "password": "...."  (BUGFIX: (?:word|wd) non-capturing)
        Regex("""(?i)("?\s*pass(?:word|wd)\s*"?\s*:\s*")([^"]+)(")""")
    )

    private val twoGroupPatterns = listOf(
        // Header: Bearer xxxxx
        Regex("""(?i)(Bearer\s+)([A-Za-z0-9._~+\-=/]+)"""),
        // Basic base64
        Regex("""(?i)(Basic\s+)([A-Za-z0-9+/=]+)"""),
        // Email; mask local-part
        Regex("""(?i)([A-Z0-9._%+\-]+)@([A-Z0-9.\-]+\.[A-Z]{2,})""", RegexOption.IGNORE_CASE),
        // IBAN (TR\d{2}\s?\d{4}...) → mask middle
        Regex("""(?i)\b([A-Z]{2}\d{2})([A-Z0-9\s]{8,})([A-Z0-9]{4})\b""")
    )

    fun mask(input: String?): String {
        if (input.isNullOrEmpty()) return input ?: ""
        var out: CharSequence = input

        threeGroupPatterns.forEach { r ->
            out = r.replace(out) { mr -> mr.groupValues[1] + "***" + mr.groupValues[3] }
        }

        twoGroupPatterns.forEach { r ->
            out = r.replace(out) { mr ->
                when (r) {
                    twoGroupPatterns[2] -> "***@" + mr.groupValues[2]            // email
                    twoGroupPatterns[3] -> mr.groupValues[1] + "***" + mr.groupValues[3] // IBAN
                    else -> mr.groupValues[1] + "***"
                }
            }
        }
        return out as String
    }
}

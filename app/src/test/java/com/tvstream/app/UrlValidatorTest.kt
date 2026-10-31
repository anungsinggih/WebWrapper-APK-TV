package com.tvstream.app

import org.junit.Assert.*
import org.junit.Test

class UrlValidatorTest {
    @Test fun acceptsHttpsAndAddsScheme() {
        assertEquals("https://example.com", UrlValidator.validate("  example.com "))
        assertEquals("https://example.com/path", UrlValidator.validate("example.com/path"))
        assertEquals("https://example.com", UrlValidator.validate("  https://example.com "))
    }
    @Test fun rejectsUnsafeUrls() { listOf("http://example.com", "javascript:alert(1)", "file:///x", "https://user:pass@example.com", "https://").forEach { assertNull(UrlValidator.validate(it)) } }
}

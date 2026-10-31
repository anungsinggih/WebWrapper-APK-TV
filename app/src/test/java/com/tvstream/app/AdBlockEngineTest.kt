package com.tvstream.app

import org.junit.Assert.*
import org.junit.Test

class AdBlockEngineTest {
    @Test fun blocksBuiltInAndAllowsWhitelist() {
        val engine = AdBlockEngine.create(AdBlockConfig(true, setOf("easylist"), "", "", "trusted.example"))
        assertNotNull(engine.intercept("https://doubleclick.net/ad.js"))
        assertNull(engine.intercept("https://trusted.example/doubleclick.js"))
        assertNull(engine.intercept("https://content.example/video.js"))
    }
    @Test fun parsesCustomUboAndHostsRules() {
        val engine = AdBlockEngine.create(AdBlockConfig(true, emptySet(), "||tracker.example^\nads.example", "", ""))
        assertNotNull(engine.intercept("https://tracker.example/pixel"))
        assertNotNull(engine.intercept("https://ads.example/banner"))
    }
}

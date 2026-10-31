package com.tvstream.app

import org.junit.Assert.*
import org.junit.Test

class AdBlockRepositoryTest {
    @Test fun supportedRuleClassifierRejectsCosmeticAndComments() {
        assertTrue(AdBlockRepository.isSupportedForTest("||ads.example^"))
        assertFalse(AdBlockRepository.isSupportedForTest("! comment"))
        assertFalse(AdBlockRepository.isSupportedForTest("example##.ad"))
        assertFalse(AdBlockRepository.isSupportedForTest("@@||allowed.example^"))
    }
}

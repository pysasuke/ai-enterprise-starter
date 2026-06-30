package com.aistarter.common.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConstantsTest {

    @Test
    void apiPrefixShouldBeApi() {
        assertEquals("/api", AppConstants.API_PREFIX);
    }
}

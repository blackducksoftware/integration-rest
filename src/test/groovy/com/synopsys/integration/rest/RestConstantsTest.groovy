package com.synopsys.integration.rest

import org.junit.jupiter.api.Test

class RestConstantsTest {
    @Test
    void testParsingDate() {
        String dateString = '2017-03-02T03:35:23.456Z'
        Date date = RestConstants.parseDateString(dateString)
        assert dateString.equals(RestConstants.formatDate(date))
    }
}

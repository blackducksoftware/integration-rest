package com.synopsys.integration.rest.support;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class UrlSupportTest {
    private UrlSupport urlSupport = new UrlSupport();

    @Test
    public void testAttemptAuthentication() {
        assertUrlPiecesCombineAsExpected("http://www.google.com/pathpiece", "login", "http://www.google.com/pathpiece/login");
        assertUrlPiecesCombineAsExpected("http://www.google.com/pathpiece/", "login", "http://www.google.com/pathpiece/login");
        assertUrlPiecesCombineAsExpected("http://www.google.com/pathpiece", "/login", "http://www.google.com/pathpiece/login");
        assertUrlPiecesCombineAsExpected("http://www.google.com/pathpiece/", "/login", "http://www.google.com/pathpiece/login");
        assertUrlPiecesCombineAsExpected("http://www.google.com/pathpiece", "./login", "http://www.google.com/pathpiece/login");
        assertUrlPiecesCombineAsExpected("http://www.google.com/pathpiece/", "./login", "http://www.google.com/pathpiece/login");
    }

    private void assertUrlPiecesCombineAsExpected(String baseUrl, String relativeUrl, String expected) {
        try {
            HttpUrl httpUrl = urlSupport.appendRelativeUrl(baseUrl, relativeUrl);
            assertEquals(expected, httpUrl.string());
        } catch (IntegrationException e) {
            fail(e);
        }
    }

}

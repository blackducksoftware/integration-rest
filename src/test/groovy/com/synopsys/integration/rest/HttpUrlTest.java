package com.synopsys.integration.rest;

import com.synopsys.integration.exception.IntegrationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class HttpUrlTest {
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
            HttpUrl base = new HttpUrl(baseUrl);
            HttpUrl httpUrl = base.appendRelativeUrl(relativeUrl);
            assertEquals(expected, httpUrl.string());
        } catch (IntegrationException e) {
            fail(e);
        }
    }

}

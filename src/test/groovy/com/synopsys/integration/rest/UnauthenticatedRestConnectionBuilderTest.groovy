package com.synopsys.integration.rest

import com.synopsys.integration.log.BufferedIntLogger
import com.synopsys.integration.rest.connection.UnauthenticatedRestConnectionBuilder
import org.junit.jupiter.api.Test

class UnauthenticatedRestConnectionBuilderTest {
    @Test
    public void testMinimumValid() {
        UnauthenticatedRestConnectionBuilder builder = createValid()
        assert builder.isValid()
    }

    @Test
    public void testInvalidTimeout() {
        UnauthenticatedRestConnectionBuilder builder = createValid()
        builder.timeout = -1
        assert !builder.isValid()

        builder.timeout = 120
        assert builder.isValid()
    }

    @Test
    public void testNoBaseUrlInvalid() {
        UnauthenticatedRestConnectionBuilder builder = createValid()
        builder.baseUrl = null
        assert !builder.isValid()

        builder.baseUrl = "http://www.google.com"
        assert builder.isValid()
    }

    @Test
    public void testBaseUrlInvalid() {
        UnauthenticatedRestConnectionBuilder builder = createValid()
        builder.baseUrl = "htp:/a.bad.domain"
        assert !builder.isValid()

        builder.baseUrl = "http://www.google.com"
        assert builder.isValid()
    }

    @Test
    public void testLogger() {
        UnauthenticatedRestConnectionBuilder builder = createValid()
        builder.logger = null
        assert !builder.isValid()

        builder.logger = new BufferedIntLogger();
        assert builder.isValid()
    }

    @Test
    public void testHeadersValid() {
        UnauthenticatedRestConnectionBuilder builder = createValid()
        builder.commonRequestHeaders = null
        assert !builder.isValid()

        builder.commonRequestHeaders = new HashMap<>();
        assert builder.isValid()
    }

    private UnauthenticatedRestConnectionBuilder createValid() {
        UnauthenticatedRestConnectionBuilder builder = new UnauthenticatedRestConnectionBuilder();
        builder.baseUrl = "http://www.google.com"
        builder.logger = new BufferedIntLogger()
        return builder
    }

}

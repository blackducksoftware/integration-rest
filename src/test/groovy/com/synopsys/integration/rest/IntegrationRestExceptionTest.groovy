package com.synopsys.integration.rest

import com.synopsys.integration.rest.exception.IntegrationRestException
import org.junit.jupiter.api.Test

class IntegrationRestExceptionTest {
    @Test
    void testConstruction() {
        int errorStatusCode = 404
        String errorStatusMessage = 'Four Oh Four'
        String errorMessage = 'Could not find the site'
        String errorContent = "error content"

        String expectedGetMessage = 'Could not find the site: 404: Four Oh Four'

        Exception error = new Exception(errorMessage)

        IntegrationRestException restException = new IntegrationRestException(errorStatusCode, errorStatusMessage, errorContent, errorMessage)
        assert errorStatusCode == restException.httpStatusCode
        assert errorStatusMessage.equals(restException.httpStatusMessage)
        assert errorContent.equals(restException.httpResponseContent)
        assert expectedGetMessage.equals(restException.message)
        assert error != restException.cause

        restException = new IntegrationRestException(errorStatusCode, errorStatusMessage, errorContent, error)
        assert errorStatusCode == restException.httpStatusCode
        assert errorStatusMessage.equals(restException.httpStatusMessage)
        assert errorContent.equals(restException.httpResponseContent)
        assert !expectedGetMessage.equals(restException.message)
        assert error == restException.cause

        restException = new IntegrationRestException(errorStatusCode, errorStatusMessage, errorContent, errorMessage, error)
        assert errorStatusCode == restException.httpStatusCode
        assert errorStatusMessage.equals(restException.httpStatusMessage)
        assert errorContent.equals(restException.httpResponseContent)
        assert expectedGetMessage.equals(restException.message)
        assert error == restException.cause

        restException = new IntegrationRestException(errorStatusCode, errorStatusMessage, errorContent, errorMessage, error, true, true)
        assert errorStatusCode == restException.httpStatusCode
        assert errorStatusMessage.equals(restException.httpStatusMessage)
        assert errorContent.equals(restException.httpResponseContent)
        assert expectedGetMessage.equals(restException.message)
        assert error == restException.cause
    }
}

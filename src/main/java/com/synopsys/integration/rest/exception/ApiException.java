/*
 * integration-rest
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.exception;

import com.synopsys.integration.exception.IntegrationException;

public class ApiException extends IntegrationException {
    private final IntegrationRestException originalIntegrationRestException;
    private final String errorCode;

    public ApiException(final IntegrationRestException originalIntegrationRestException, final String errorMessage, final String errorCode) {
        super(errorMessage);
        this.originalIntegrationRestException = originalIntegrationRestException;
        this.errorCode = errorCode;
    }

    public IntegrationRestException getOriginalIntegrationRestException() {
        return originalIntegrationRestException;
    }

    public String getErrorCode() {
        return errorCode;
    }

}

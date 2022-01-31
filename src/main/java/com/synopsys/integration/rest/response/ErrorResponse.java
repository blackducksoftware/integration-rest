/*
 * integration-rest
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.response;

import com.synopsys.integration.util.Stringable;

public class ErrorResponse extends Stringable {
    private final String errorMessage;
    private final String errorCode;

    public ErrorResponse(final String errorMessage, final String errorCode) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

}

/*
 * integration-rest
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.client;

import java.util.Optional;

public class ConnectionResult {
    private final int httpStatusCode;
    private final String failureMessage;
    private final Exception exception;

    public static final ConnectionResult SUCCESS(int httpStatusCode) {
        return new ConnectionResult(httpStatusCode, null, null);
    }

    public static final ConnectionResult FAILURE(int httpStatusCode, String failureMessage, Exception exception) {
        return new ConnectionResult(httpStatusCode, failureMessage, exception);
    }

    public ConnectionResult(int httpStatusCode, String failureMessage, Exception exception) {
        this.httpStatusCode = httpStatusCode;
        this.failureMessage = failureMessage;
        this.exception = exception;
    }

    public boolean isSuccess() {
        return !isFailure();
    }

    public boolean isFailure() {
        return getFailureMessage().isPresent();
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public Optional<String> getFailureMessage() {
        return Optional.ofNullable(failureMessage);
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

}

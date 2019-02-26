package com.synopsys.integration.rest.client;

import java.util.Optional;

public class ConnectionResult {
    private final int httpStatusCode;
    private final String failureMessage;

    public static final ConnectionResult SUCCESS(int httpStatusCode) {
        return new ConnectionResult(httpStatusCode, null);
    }

    public static final ConnectionResult FAILURE(int httpStatusCode, String failureMessage) {
        return new ConnectionResult(httpStatusCode, failureMessage);
    }

    private ConnectionResult(int httpStatusCode, String failureMessage) {
        this.httpStatusCode = httpStatusCode;
        this.failureMessage = failureMessage;
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

}

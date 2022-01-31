/*
 * integration-rest
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.exception;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;

public class IntegrationRestException extends IntegrationException {
    private static final long serialVersionUID = 1L;

    private final HttpMethod httpMethod;
    private final HttpUrl httpUrl;
    private final int httpStatusCode;
    private final String httpStatusMessage;
    private final String httpResponseContent;

    public IntegrationRestException(HttpMethod httpMethod, HttpUrl httpUrl, int httpStatusCode, String httpStatusMessage, String httpResponseContent, String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.httpMethod = httpMethod;
        this.httpUrl = httpUrl;
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
        this.httpResponseContent = httpResponseContent;
    }

    public IntegrationRestException(HttpMethod httpMethod, HttpUrl httpUrl, int httpStatusCode, String httpStatusMessage, String httpResponseContent, String message, Throwable cause) {
        super(message, cause);
        this.httpMethod = httpMethod;
        this.httpUrl = httpUrl;
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
        this.httpResponseContent = httpResponseContent;
    }

    public IntegrationRestException(HttpMethod httpMethod, HttpUrl httpUrl, int httpStatusCode, String httpStatusMessage, String httpResponseContent, String message) {
        super(message);
        this.httpMethod = httpMethod;
        this.httpUrl = httpUrl;
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
        this.httpResponseContent = httpResponseContent;
    }

    public IntegrationRestException(HttpMethod httpMethod, HttpUrl httpUrl, int httpStatusCode, String httpStatusMessage, String httpResponseContent, Throwable cause) {
        super(cause);
        this.httpMethod = httpMethod;
        this.httpUrl = httpUrl;
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
        this.httpResponseContent = httpResponseContent;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public HttpUrl getHttpUrl() {
        return httpUrl;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getHttpStatusMessage() {
        return httpStatusMessage;
    }

    public String getHttpResponseContent() {
        return httpResponseContent;
    }

}

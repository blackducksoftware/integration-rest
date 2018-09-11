/**
 * integration-rest
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.rest.exception;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.exception.IntegrationException;

public class IntegrationRestException extends IntegrationException {
    private static final long serialVersionUID = 1L;

    private final int httpStatusCode;
    private final String httpStatusMessage;
    private final String httpResponseContent;

    public IntegrationRestException(final int httpStatusCode, final String httpStatusMessage, final String httpResponseContent, final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
        this.httpResponseContent = httpResponseContent;
    }

    public IntegrationRestException(final int httpStatusCode, final String httpStatusMessage, final String httpResponseContent, final String message, final Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
        this.httpResponseContent = httpResponseContent;
    }

    public IntegrationRestException(final int httpStatusCode, final String httpStatusMessage, final String httpResponseContent, final String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
        this.httpResponseContent = httpResponseContent;
    }

    public IntegrationRestException(final int httpStatusCode, final String httpStatusMessage, final String httpResponseContent, final Throwable cause) {
        super(cause);
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
        this.httpResponseContent = httpResponseContent;
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

    @Override
    public String getMessage() {
        String message = "";
        if (StringUtils.isNotBlank(super.getMessage())) {
            message = super.getMessage();
        }
        if (httpStatusCode > 0) {
            message = message + ": " + httpStatusCode;
        }
        if (StringUtils.isNotBlank(httpStatusMessage)) {
            message = message + ": " + httpStatusMessage;
        }

        return message;
    }

}

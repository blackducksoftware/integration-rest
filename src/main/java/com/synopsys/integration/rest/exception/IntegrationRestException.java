/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

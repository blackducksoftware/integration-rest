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
package com.synopsys.integration.rest.client;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.response.Response;

public abstract class AuthenticatingIntHttpClient extends IntHttpClient {
    public AuthenticatingIntHttpClient(IntLogger logger, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo) {
        super(logger, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo);
    }

    public AuthenticatingIntHttpClient(IntLogger logger, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, CredentialsProvider credentialsProvider, HttpClientBuilder clientBuilder,
        RequestConfig.Builder defaultRequestConfigBuilder, Map<String, String> commonRequestHeaders) {
        super(logger, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo, credentialsProvider, clientBuilder, defaultRequestConfigBuilder, commonRequestHeaders);
    }

    public abstract boolean isAlreadyAuthenticated(HttpUriRequest request);

    public abstract Response attemptAuthentication() throws IntegrationException;

    public final void authenticateRequest(HttpUriRequest request) throws IntegrationException {
        try (Response response = attemptAuthentication()) {
            completeAuthenticationRequest(request, response);
        } catch (IOException e) {
            throw new IntegrationException("The request could not be authenticated with the provided credentials: " + e.getMessage(), e);
        }
    }

    @Override
    public Response execute(HttpUriRequest request) throws IntegrationException {
        return execute(request, new BasicHttpContext());
    }

    @Override
    public Response execute(HttpUriRequest request, HttpContext httpContext) throws IntegrationException {
        return retryExecute(request, httpContext, 0);
    }

    public final boolean isUnauthorizedOrForbidden(Response response) {
        Integer statusCode = response.getStatusCode();
        return null == statusCode || statusCode == RestConstants.UNAUTHORIZED_401 || statusCode == RestConstants.FORBIDDEN_403;
    }

    public boolean canConnect() {
        ConnectionResult connectionResult = attemptConnection();
        return connectionResult.isSuccess();
    }

    public ConnectionResult attemptConnection() {
        String errorMessage = null;
        Exception exception = null;
        int httpStatusCode = 0;

        try {
            try (Response response = attemptAuthentication()) {
                // if you get an error response, you know that a connection could not be made
                httpStatusCode = response.getStatusCode();
                if (response.isStatusCodeError()) {
                    errorMessage = response.getContentString();
                }
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
            exception = e;
        }

        if (null != errorMessage) {
            logger.error(errorMessage);
            return ConnectionResult.FAILURE(httpStatusCode, errorMessage, exception);
        }

        logger.info("A successful connection was made.");
        return ConnectionResult.SUCCESS(httpStatusCode);
    }

    protected abstract void completeAuthenticationRequest(HttpUriRequest request, Response response) throws IntegrationException;

    private Response retryExecute(HttpUriRequest request, HttpContext httpContext, int retryCount) throws IntegrationException {
        if (!isAlreadyAuthenticated(request)) {
            authenticateRequest(request);
        }
        Response response = super.execute(request, httpContext);

        boolean notOkay = isUnauthorizedOrForbidden(response);

        if (notOkay && retryCount < 2) {
            authenticateRequest(request);
            return retryExecute(request, httpContext, retryCount + 1);
        } else if (notOkay) {
            response.throwExceptionForError();
        }

        return response;
    }

}

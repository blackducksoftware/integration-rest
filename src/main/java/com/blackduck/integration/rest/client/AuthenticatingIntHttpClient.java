/*
 * integration-rest
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.client;

import java.io.IOException;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.RestConstants;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.response.Response;
import com.google.gson.Gson;

public abstract class AuthenticatingIntHttpClient extends IntHttpClient {
    public AuthenticatingIntHttpClient(IntLogger logger, Gson gson, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo) {
        super(logger, gson, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo);
    }

    public AuthenticatingIntHttpClient(IntLogger logger, Gson gson, int timeoutInSeconds, ProxyInfo proxyInfo, SSLContext sslContext) {
        super(logger, gson, timeoutInSeconds, proxyInfo, sslContext);
    }

    public AuthenticatingIntHttpClient(IntLogger logger, Gson gson, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, CredentialsProvider credentialsProvider, HttpClientBuilder clientBuilder,
        RequestConfig.Builder defaultRequestConfigBuilder, Map<String, String> commonRequestHeaders) {
        super(logger, gson, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo, credentialsProvider, clientBuilder, defaultRequestConfigBuilder, commonRequestHeaders);
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

    public final boolean isUnauthorized(Response response) {
        Integer statusCode = response.getStatusCode();
        return null == statusCode || statusCode == RestConstants.UNAUTHORIZED_401;
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

        boolean notOkay = isUnauthorized(response);

        if (notOkay && retryCount < 2) {
            authenticateRequest(request);
            return retryExecute(request, httpContext, retryCount + 1);
        } else if (notOkay) {
            response.throwExceptionForError();
        }

        return response;
    }

}

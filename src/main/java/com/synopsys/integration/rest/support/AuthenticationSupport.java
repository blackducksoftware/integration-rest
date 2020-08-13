/**
 * integration-rest
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.rest.support;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.client.AuthenticatingIntHttpClient;
import com.synopsys.integration.rest.response.Response;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class AuthenticationSupport {
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public Response attemptAuthentication(AuthenticatingIntHttpClient authenticatingIntHttpClient, HttpUrl baseUrl, String authenticationSuffix, Map<String, String> requestHeaders) throws IntegrationException {
        HttpUrl authenticationUrl = baseUrl.appendRelativeUrl(authenticationSuffix);
        RequestBuilder requestBuilder = authenticatingIntHttpClient.createRequestBuilder(HttpMethod.POST, requestHeaders);
        return attemptAuthentication(authenticatingIntHttpClient, authenticationUrl, requestBuilder);
    }

    public Response attemptAuthentication(AuthenticatingIntHttpClient authenticatingIntHttpClient, HttpUrl baseUrl, String authenticationSuffix, HttpEntity httpEntity) throws IntegrationException {
        HttpUrl authenticationUrl = baseUrl.appendRelativeUrl(authenticationSuffix);
        RequestBuilder requestBuilder = authenticatingIntHttpClient.createRequestBuilder(HttpMethod.POST);
        requestBuilder.setEntity(httpEntity);
        return attemptAuthentication(authenticatingIntHttpClient, authenticationUrl, requestBuilder);
    }

    public Response attemptAuthentication(AuthenticatingIntHttpClient authenticatingIntHttpClient, HttpUrl authenticationUrl, RequestBuilder requestBuilder) throws IntegrationException {
        requestBuilder.setCharset(StandardCharsets.UTF_8);
        requestBuilder.setUri(authenticationUrl.string());
        HttpUriRequest request = requestBuilder.build();
        authenticatingIntHttpClient.logRequestHeaders(request);

        CloseableHttpClient closeableHttpClient = authenticatingIntHttpClient.getClientBuilder().build();
        CloseableHttpResponse closeableHttpResponse;
        try {
            closeableHttpResponse = closeableHttpClient.execute(request);
        } catch (IOException e) {
            throw new IntegrationException("Could not perform the authorization request: " + e.getMessage(), e);
        }
        authenticatingIntHttpClient.logResponseHeaders(closeableHttpResponse);
        return new Response(request, closeableHttpClient, closeableHttpResponse);
    }

    public void handleErrorResponse(AuthenticatingIntHttpClient authenticatingIntHttpClient, HttpUriRequest request, Response response, String authorizationHeader) {
        if (authenticatingIntHttpClient.isUnauthorizedOrForbidden(response) && request.containsHeader(authorizationHeader)) {
            request.removeHeaders(authorizationHeader);
            authenticatingIntHttpClient.removeCommonRequestHeader(authorizationHeader);
        }
    }

    public Optional<String> retrieveBearerToken(IntLogger logger, Gson gson, AuthenticatingIntHttpClient authenticatingIntHttpClient, String bearerTokenKey) {
        try (Response response = authenticatingIntHttpClient.attemptAuthentication()) {
            if (response.isStatusCodeSuccess()) {
                String bodyContent;
                try (InputStream inputStream = response.getContent()) {
                    bodyContent = IOUtils.toString(inputStream, Charsets.UTF_8);
                }
                JsonObject jsonResponse = gson.fromJson(bodyContent, JsonObject.class);
                String bearerToken = jsonResponse.get(bearerTokenKey).getAsString();
                return Optional.of(bearerToken);
            }
        } catch (IntegrationException | IOException e) {
            logger.error("Could not retrieve the bearer token", e);
        }
        return Optional.empty();
    }

    public void handleTokenErrorResponse(AuthenticatingIntHttpClient authenticatingIntHttpClient, HttpUriRequest request, Response response) {
        handleErrorResponse(authenticatingIntHttpClient, request, response, AuthenticationSupport.AUTHORIZATION_HEADER);
    }

    public boolean isTokenAlreadyAuthenticated(HttpUriRequest request) {
        return request.containsHeader(AuthenticationSupport.AUTHORIZATION_HEADER);
    }

    public void completeTokenAuthenticationRequest(HttpUriRequest request, Response response, IntLogger logger, Gson gson, AuthenticatingIntHttpClient authenticatingIntHttpClient, String bearerTokenResponseKey) {
        if (response.isStatusCodeSuccess()) {
            Optional<String> bearerToken = retrieveBearerToken(logger, gson, authenticatingIntHttpClient, bearerTokenResponseKey);
            addBearerToken(logger, request, authenticatingIntHttpClient, bearerToken);
        }
    }

    public void addBearerToken(IntLogger logger, HttpUriRequest request, AuthenticatingIntHttpClient authenticatingIntHttpClient, Optional<String> bearerToken) {
        if (bearerToken.isPresent()) {
            String headerValue = "Bearer " + bearerToken.get();
            addAuthenticationHeader(authenticatingIntHttpClient, request, AuthenticationSupport.AUTHORIZATION_HEADER, headerValue);
        } else {
            logger.error("No Bearer token found when authenticating.");
        }
    }

    public void addAuthenticationHeader(AuthenticatingIntHttpClient authenticatingIntHttpClient, HttpUriRequest request, String headerName, String headerValue) {
        authenticatingIntHttpClient.addCommonRequestHeader(headerName, headerValue);
        request.addHeader(headerName, headerValue);
    }

}

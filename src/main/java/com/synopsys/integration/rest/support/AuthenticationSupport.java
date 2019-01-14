/**
 * integration-rest
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.rest.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.client.AuthenticatingIntHttpClient;
import com.synopsys.integration.rest.request.Response;

public class AuthenticationSupport {
    public Response attemptAuthentication(AuthenticatingIntHttpClient restConnection, String baseUrl, String authenticationUrl, Map<String, String> requestHeaders) throws IntegrationException {
        RequestBuilder requestBuilder = restConnection.createRequestBuilder(HttpMethod.POST, requestHeaders);
        return attemptAuthentication(restConnection, baseUrl, authenticationUrl, requestBuilder);
    }

    public Response attemptAuthentication(AuthenticatingIntHttpClient restConnection, String baseUrl, String authenticationUrl, HttpEntity httpEntity) throws IntegrationException {
        RequestBuilder requestBuilder = restConnection.createRequestBuilder(HttpMethod.POST);
        requestBuilder.setEntity(httpEntity);
        return attemptAuthentication(restConnection, baseUrl, authenticationUrl, requestBuilder);
    }

    public Response attemptAuthentication(AuthenticatingIntHttpClient restConnection, String baseUrl, String authenticationUrl, RequestBuilder requestBuilder) throws IntegrationException {
        URL authenticationURL;
        try {
            URL baseURL = new URL(baseUrl);
            authenticationURL = new URL(baseURL, authenticationUrl);
        } catch (MalformedURLException e) {
            throw new IntegrationException("Error constructing the authentication URL: " + e.getMessage(), e);
        }

        requestBuilder.setCharset(Charsets.UTF_8);
        requestBuilder.setUri(authenticationURL.toString());
        HttpUriRequest request = requestBuilder.build();
        restConnection.logRequestHeaders(request);

        CloseableHttpClient closeableHttpClient = restConnection.getClientBuilder().build();
        CloseableHttpResponse closeableHttpResponse;
        try {
            closeableHttpResponse = closeableHttpClient.execute(request);
        } catch (IOException e) {
            throw new IntegrationException("Could not perform the authorization request: " + e.getMessage(), e);
        }
        restConnection.logResponseHeaders(closeableHttpResponse);
        return new Response(request, closeableHttpClient, closeableHttpResponse);
    }

    public void handleErrorResponse(AuthenticatingIntHttpClient restConnection, HttpUriRequest request, Response response, String authorizationHeader) {
        if (restConnection.isUnauthorizedOrForbidden(response) && request.containsHeader(authorizationHeader)) {
            request.removeHeaders(authorizationHeader);
            restConnection.removeCommonRequestHeader(authorizationHeader);
        }
    }

    public Optional<String> retrieveBearerToken(IntLogger logger, Gson gson, AuthenticatingIntHttpClient restConnection, String bearerTokenKey) {
        try (Response response = restConnection.attemptAuthentication()) {
            if (response.isStatusCodeOkay()) {
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

    public void resolveBearerToken(IntLogger logger, AuthenticatingIntHttpClient restConnection, HttpUriRequest request, String authorizationHeader, Optional<String> bearerToken) {
        Optional<String> headerValue = bearerToken.map(token -> "Bearer " + token);
        resolveToken(logger, restConnection, request, authorizationHeader, headerValue, "No Bearer token found when authenticating");
    }

    public void resolveToken(IntLogger logger, AuthenticatingIntHttpClient restConnection, HttpUriRequest request, String headerName, Optional<String> headerValue, String logMessage) {
        if (headerValue.isPresent()) {
            restConnection.addCommonRequestHeader(headerName, headerValue.get());
            request.addHeader(headerName, headerValue.get());
        } else {
            logger.error(logMessage);
        }
    }

}

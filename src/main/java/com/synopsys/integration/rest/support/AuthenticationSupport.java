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
import com.synopsys.integration.rest.response.Response;

public class AuthenticationSupport {
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public Response attemptAuthentication(AuthenticatingIntHttpClient authenticatingIntHttpClient, String baseUrl, String authenticationUrl, Map<String, String> requestHeaders) throws IntegrationException {
        RequestBuilder requestBuilder = authenticatingIntHttpClient.createRequestBuilder(HttpMethod.POST, requestHeaders);
        return attemptAuthentication(authenticatingIntHttpClient, baseUrl, authenticationUrl, requestBuilder);
    }

    public Response attemptAuthentication(AuthenticatingIntHttpClient authenticatingIntHttpClient, String baseUrl, String authenticationUrl, HttpEntity httpEntity) throws IntegrationException {
        RequestBuilder requestBuilder = authenticatingIntHttpClient.createRequestBuilder(HttpMethod.POST);
        requestBuilder.setEntity(httpEntity);
        return attemptAuthentication(authenticatingIntHttpClient, baseUrl, authenticationUrl, requestBuilder);
    }

    public Response attemptAuthentication(AuthenticatingIntHttpClient authenticatingIntHttpClient, String baseUrl, String authenticationUrl, RequestBuilder requestBuilder) throws IntegrationException {
        URL authenticationURL;
        try {
            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "/";
            }
            URL baseURL = new URL(baseUrl);
            if (authenticationUrl.startsWith("/")) {
                authenticationUrl = authenticationUrl.substring(1);
            }
            authenticationURL = new URL(baseURL, authenticationUrl);
        } catch (MalformedURLException e) {
            throw new IntegrationException("Error constructing the authentication URL: " + e.getMessage(), e);
        }

        requestBuilder.setCharset(Charsets.UTF_8);
        requestBuilder.setUri(authenticationURL.toString());
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

    public void handleTokenErrorResponse(AuthenticatingIntHttpClient authenticatingIntHttpClient, HttpUriRequest request, Response response) {
        handleErrorResponse(authenticatingIntHttpClient, request, response, AuthenticationSupport.AUTHORIZATION_HEADER);
    }

    public boolean isTokenAlreadyAuthenticated(HttpUriRequest request) {
        return request.containsHeader(AuthenticationSupport.AUTHORIZATION_HEADER);
    }

    public void completeTokenAuthenticationRequest(HttpUriRequest request, Response response, IntLogger logger, Gson gson, AuthenticatingIntHttpClient authenticatingIntHttpClient, String bearerTokenResponseKey) {
        if (response.isStatusCodeOkay()) {
            Optional<String> bearerToken = retrieveBearerToken(logger, gson, authenticatingIntHttpClient, bearerTokenResponseKey);
            if (bearerToken.isPresent()) {
                String headerValue = "Bearer " + bearerToken.get();
                addAuthenticationHeader(authenticatingIntHttpClient, request, AuthenticationSupport.AUTHORIZATION_HEADER, headerValue);
            } else {
                logger.error("No Bearer token found when authenticating.");
            }
        }
    }

    public void addAuthenticationHeader(AuthenticatingIntHttpClient authenticatingIntHttpClient, HttpUriRequest request, String headerName, String headerValue) {
        authenticatingIntHttpClient.addCommonRequestHeader(headerName, headerValue);
        request.addHeader(headerName, headerValue);
    }

}

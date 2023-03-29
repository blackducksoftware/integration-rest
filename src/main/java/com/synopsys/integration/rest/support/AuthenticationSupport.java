/*
 * integration-rest
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.client.AuthenticatingIntHttpClient;
import com.synopsys.integration.rest.response.DefaultResponse;
import com.synopsys.integration.rest.response.Response;

public class AuthenticationSupport {
    public static final List<String> NEED_TO_MANAGE_CONTENT_LENGTH = Arrays.asList(HttpMethod.POST.name(), HttpMethod.PUT.name());
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
        if (NEED_TO_MANAGE_CONTENT_LENGTH.contains(requestBuilder.getMethod().toUpperCase()) && null == requestBuilder.getEntity()) {
            //https://github.com/blackducksoftware/blackduck-common/issues/268
            //https://stackoverflow.com/questions/15619562/getting-411-length-required-after-a-put-request-from-http-client
            //https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/411
            /*
            ekerwin - when using RequestBuilder, a default content length is not assigned
            and without a content length, certain proxies will deny the POST/PUT with a 411
             */
            requestBuilder.addHeader(HttpHeaders.CONTENT_LENGTH, "0");
        }
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
        return new DefaultResponse(request, closeableHttpClient, closeableHttpResponse);
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

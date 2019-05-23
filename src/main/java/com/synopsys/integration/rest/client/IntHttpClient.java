/**
 * integration-rest
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import com.jayway.jsonpath.JsonPath;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.exception.ApiException;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.rest.response.ErrorResponse;

/**
 * A basic, extendable http client.
 */
public class IntHttpClient {
    public static final String ERROR_MSG_PROXY_INFO_NULL = "A IntHttpClient's proxy information cannot be null.";
    public static final int DEFAULT_TIMEOUT = 120;

    protected final IntLogger logger;
    private final ProxyInfo proxyInfo;

    private final int timeoutInSeconds;
    private final boolean alwaysTrustServerCertificate;

    private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    private final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    private final RequestConfig.Builder defaultRequestConfigBuilder = RequestConfig.custom();
    private final Map<String, String> commonRequestHeaders = new HashMap<>();

    public IntHttpClient(final IntLogger logger, final int timeoutInSeconds, final boolean alwaysTrustServerCertificate, final ProxyInfo proxyInfo) {
        this.logger = logger;
        this.proxyInfo = proxyInfo;
        this.timeoutInSeconds = timeoutInSeconds;
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;

        if (0 >= timeoutInSeconds) {
            throw new IllegalArgumentException("The timeout must be greater than 0.");
        }

        if (null == logger) {
            throw new IllegalArgumentException("The logger instance may not be null.");
        }

        if (null == proxyInfo) {
            throw new IllegalArgumentException(IntHttpClient.ERROR_MSG_PROXY_INFO_NULL);
        }

        addBuilderConnectionTimes();
        addBuilderProxyInformation();
        populateHttpClientBuilder(clientBuilder, defaultRequestConfigBuilder);
        addBuilderCredentialsProvider();
        addBuilderSSLContext();
    }

    /**
     * Subclasses can add to the builders any additional fields they need to successfully initialize
     */
    public void populateHttpClientBuilder(final HttpClientBuilder httpClientBuilder, final RequestConfig.Builder defaultRequestConfigBuilder) {
    }

    /**
     * Subclasses might need to handle an error response and modify the request
     */
    public void handleErrorResponse(final HttpUriRequest request, final Response response) {
    }

    public final RequestBuilder createRequestBuilder(final HttpMethod method) throws IntegrationException {
        return createRequestBuilder(method, null);
    }

    public final RequestBuilder createRequestBuilder(final HttpMethod method, final Map<String, String> additionalHeaders) throws IntegrationException {
        if (method == null) {
            throw new IntegrationException("Missing field 'method'");
        }
        final RequestBuilder requestBuilder = RequestBuilder.create(method.name());

        final Map<String, String> requestHeaders = new HashMap<>(commonRequestHeaders);
        if (additionalHeaders != null && !additionalHeaders.isEmpty()) {
            requestHeaders.putAll(additionalHeaders);
        }
        for (final Entry<String, String> header : requestHeaders.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }

        return requestBuilder;
    }

    public Response execute(final Request request) throws IntegrationException {
        final HttpUriRequest httpUriRequest = request.createHttpUriRequest(commonRequestHeaders);
        return execute(httpUriRequest);
    }

    public Response execute(final HttpUriRequest request) throws IntegrationException {
        final long start = System.currentTimeMillis();
        logger.trace("starting request: " + request.getURI().toString());
        try {
            return handleClientExecution(request);
        } finally {
            final long end = System.currentTimeMillis();
            logger.trace(String.format("completed request: %s (%d ms)", request.getURI().toString(), end - start));
        }
    }

    public Optional<Response> executeGetRequestIfModifiedSince(final Request getRequest, final long timeToCheck) throws IntegrationException, IOException {
        final Request headRequest = new Request.Builder(getRequest).method(HttpMethod.HEAD).build();

        long lastModifiedOnServer = 0L;
        try (final Response headResponse = execute(headRequest.createHttpUriRequest(commonRequestHeaders))) {
            lastModifiedOnServer = headResponse.getLastModified();
            logger.debug(String.format("Last modified on server: %d", lastModifiedOnServer));
        } catch (final IntegrationException e) {
            logger.error("Couldn't get the Last-Modified header from the server.");
            throw e;
        }

        if (lastModifiedOnServer == timeToCheck) {
            logger.debug("The request has not been modified since it was last checked - skipping.");
            return Optional.empty();
        }

        return Optional.of(execute(getRequest.createHttpUriRequest(commonRequestHeaders)));
    }

    public final void logRequestHeaders(final HttpUriRequest request) {
        final String requestName = request.getClass().getSimpleName();
        logger.trace(requestName + " : " + request.toString());
        logHeaders(requestName, request.getAllHeaders());
    }

    public final void logResponseHeaders(final HttpResponse response) {
        final String responseName = response.getClass().getSimpleName();
        logger.trace(responseName + " : " + response.toString());
        logHeaders(responseName, response.getAllHeaders());
    }

    private void addBuilderConnectionTimes() {
        defaultRequestConfigBuilder.setConnectTimeout(timeoutInSeconds * 1000);
        defaultRequestConfigBuilder.setSocketTimeout(timeoutInSeconds * 1000);
        defaultRequestConfigBuilder.setConnectionRequestTimeout(timeoutInSeconds * 1000);
    }

    private void addBuilderCredentialsProvider() {
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        clientBuilder.setDefaultRequestConfig(defaultRequestConfigBuilder.build());
    }

    private void addBuilderSSLContext() {
        try {
            final SSLContext sslContext;
            final HostnameVerifier hostnameVerifier;
            if (alwaysTrustServerCertificate) {
                logger.warn("Automatically trusting server certificates - not recommended for production use.");
                sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustAllStrategy()).build();
                hostnameVerifier = new NoopHostnameVerifier();
            } else {
                sslContext = SSLContexts.createDefault();
                hostnameVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier();
            }
            final SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            clientBuilder.setSSLSocketFactory(connectionFactory);
        } catch (final KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private void addBuilderProxyInformation() {
        if (proxyInfo.shouldUseProxy()) {
            defaultRequestConfigBuilder.setProxy(new HttpHost(proxyInfo.getHost().orElse(null), proxyInfo.getPort()));
            if (proxyInfo.hasAuthenticatedProxySettings()) {
                final org.apache.http.auth.Credentials credentials = new NTCredentials(proxyInfo.getUsername().orElse(null), proxyInfo.getPassword().orElse(null), proxyInfo.getNtlmWorkstation().orElse(null),
                    proxyInfo.getNtlmDomain().orElse(null));
                credentialsProvider.setCredentials(new AuthScope(proxyInfo.getHost().orElse(null), proxyInfo.getPort()), credentials);
            }
        }
    }

    private Response handleClientExecution(final HttpUriRequest request) throws IntegrationException {
        try {
            final CloseableHttpClient client = clientBuilder.build();
            logRequestHeaders(request);
            final CloseableHttpResponse closeableHttpResponse = client.execute(request);
            final Response response = new Response(request, client, closeableHttpResponse);
            logResponseHeaders(closeableHttpResponse);
            if (response.isStatusCodeError()) {
                handleErrorResponse(request, response);
            }
            return response;
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private void logHeaders(final String requestOrResponseName, final Header[] headers) {
        if (headers != null && headers.length > 0) {
            logger.trace(requestOrResponseName + " headers : ");
            for (final Header header : headers) {
                logger.trace(String.format("Header %s : %s", header.getName(), header.getValue()));
            }
        } else {
            logger.trace(requestOrResponseName + " does not have any headers.");
        }
    }

    public void throwExceptionForError(final Response response) throws IntegrationException {
        try {
            response.throwExceptionForError();
        } catch (final IntegrationRestException e) {
            throw transformException(e);
        }
    }

    private IntegrationException transformException(final IntegrationRestException e) {
        final String httpResponseContent = e.getHttpResponseContent();
        final Optional<ErrorResponse> optionalErrorResponse = extractErrorResponse(httpResponseContent);
        if (optionalErrorResponse.isPresent()) {
            final ErrorResponse errorResponse = optionalErrorResponse.get();
            final String apiExceptionErrorMessage = String.format("%s [HTTP Error]: %s", errorResponse.getErrorMessage(), e.getMessage());
            return new ApiException(e, apiExceptionErrorMessage, errorResponse.getErrorCode());
        } else {
            return e;
        }
    }

    public Optional<ErrorResponse> extractErrorResponse(final String responseContent) {
        if (StringUtils.isNotBlank(responseContent)) {
            try {
                final String errorMessage = JsonPath.read(responseContent, "$.errorMessage");
                final String errorCode = JsonPath.read(responseContent, "$.errorCode");
                if (!StringUtils.isAllBlank(errorMessage, errorCode)) {
                    return Optional.of(new ErrorResponse(errorMessage, errorCode));
                }
            } catch (final Exception ignored) {
                //ignored
            }
        }
        return Optional.empty();
    }

    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public boolean isAlwaysTrustServerCertificate() {
        return alwaysTrustServerCertificate;
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public HttpClientBuilder getClientBuilder() {
        return clientBuilder;
    }

    public RequestConfig.Builder getDefaultRequestConfigBuilder() {
        return defaultRequestConfigBuilder;
    }

    public Map<String, String> getCommonRequestHeaders() {
        return commonRequestHeaders;
    }

    public void addCommonRequestHeader(final String key, final String value) {
        commonRequestHeaders.put(key, value);
    }

    public void addCommonRequestHeaders(final Map<String, String> commonRequestHeaders) {
        this.commonRequestHeaders.putAll(commonRequestHeaders);
    }

    public String removeCommonRequestHeader(final String key) {
        return commonRequestHeaders.remove(key);
    }

    public IntLogger getLogger() {
        return logger;
    }

}

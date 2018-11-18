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
package com.synopsys.integration.rest.connection;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

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

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

/**
 * The parent class of all rest connections.
 */
public class RestConnection {
    public static final String ERROR_MSG_PROXY_INFO_NULL = "A RestConnection's proxy information cannot be null.";
    public static final int DEFAULT_TIMEOUT = 120;

    protected final IntLogger logger;
    private final ProxyInfo proxyInfo;

    private int timeout;
    private boolean alwaysTrustServerCertificate;

    private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    private final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    private final RequestConfig.Builder defaultRequestConfigBuilder = RequestConfig.custom();
    private final Map<String, String> commonRequestHeaders = new HashMap<>();

    private boolean initialized = false;

    public RestConnection(final IntLogger logger, final int timeout, final boolean alwaysTrustServerCertificate, final ProxyInfo proxyInfo) {
        this.logger = logger;
        this.proxyInfo = proxyInfo;
        this.timeout = timeout;
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;

        if (0 >= timeout) {
            throw new IllegalArgumentException("The timeout must be greater than 0.");
        }

        if (null == logger) {
            throw new IllegalArgumentException("The logger instance may not be null.");
        }

        if (null == proxyInfo) {
            throw new IllegalArgumentException(ERROR_MSG_PROXY_INFO_NULL);
        }
    }

    public final void initialize() throws IntegrationException {
        addBuilderConnectionTimes();
        addBuilderProxyInformation();
        populateHttpClientBuilder(clientBuilder, defaultRequestConfigBuilder);
        addBuilderCredentialsProvider();
        addBuilderSSLContext();
        completeConnection();
        initialized = true;
    }

    /**
     * Subclasses can add to the builders any additional fields they need to successfully initialize
     */
    public void populateHttpClientBuilder(final HttpClientBuilder httpClientBuilder, final RequestConfig.Builder defaultRequestConfigBuilder) throws IntegrationException {

    }

    /**
     * Subclasses might need to do final processing to the http client (usually authentication)
     */
    public void completeConnection() throws IntegrationException {

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

    public final HttpUriRequest copyHttpRequest(final HttpUriRequest request) {
        final RequestBuilder requestBuilder = RequestBuilder.copy(request);
        if (!commonRequestHeaders.isEmpty()) {
            for (final Entry<String, String> header : commonRequestHeaders.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }
        return requestBuilder.build();
    }

    public Response executeRequest(final Request request) throws IntegrationException {
        final HttpUriRequest httpUriRequest = request.createHttpUriRequest(commonRequestHeaders);
        return executeRequest(httpUriRequest);
    }

    public Response executeRequest(final HttpUriRequest request) throws IntegrationException {
        final long start = System.currentTimeMillis();
        logger.trace("starting request: " + request.getURI().toString());
        try {
            return handleClientExecution(request);
        } finally {
            final long end = System.currentTimeMillis();
            logger.trace(String.format("completed request: %s (%d ms)", request.getURI().toString(), end - start));
        }
    }

    /**
     * Will throw an exception if the status code is an error code
     */
    public Response executeRequestWithException(final HttpUriRequest request) throws IntegrationException {
        final Response response = executeRequest(request);

        if (response.isStatusCodeError()) {
            final Integer statusCode = response.getStatusCode();
            final String statusMessage = response.getStatusMessage();
            String httpResponseContent;
            try {
                httpResponseContent = response.getContentString();
            } catch (final IntegrationException e) {
                httpResponseContent = e.getMessage();
            }

            throw new IntegrationRestException(statusCode, statusMessage, httpResponseContent,
                String.format("There was a problem trying to %s this item: %s. Error: %s %s", request.getMethod(), request.getURI(), statusCode, statusMessage));
        }

        return response;
    }

    /**
     * Will throw an exception if the status code is an error code
     */
    public Response executeRequestWithException(final Request request) throws IntegrationException {
        final HttpUriRequest httpUriRequest = request.createHttpUriRequest(commonRequestHeaders);
        return executeRequestWithException(httpUriRequest);
    }

    public Optional<Response> executeGetRequestIfModifiedSince(final Request getRequest, final long timeToCheck) throws IntegrationException, IOException {
        final Request headRequest = new Request.Builder(getRequest).method(HttpMethod.HEAD).build();

        long lastModifiedOnServer = 0L;
        try (final Response headResponse = executeRequest(headRequest.createHttpUriRequest(commonRequestHeaders))) {
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

        return Optional.of(executeRequest(getRequest.createHttpUriRequest(commonRequestHeaders)));
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
        defaultRequestConfigBuilder.setConnectTimeout(timeout * 1000);
        defaultRequestConfigBuilder.setSocketTimeout(timeout * 1000);
        defaultRequestConfigBuilder.setConnectionRequestTimeout(timeout * 1000);
    }

    private void addBuilderCredentialsProvider() {
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        clientBuilder.setDefaultRequestConfig(defaultRequestConfigBuilder.build());
    }

    private void addBuilderSSLContext() throws IntegrationException {
        try {
            final SSLContext sslContext;
            if (alwaysTrustServerCertificate) {
                sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustAllStrategy()).build();
            } else {
                sslContext = SSLContexts.createDefault();
            }
            final HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
            final SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
            clientBuilder.setSSLSocketFactory(connectionFactory);
        } catch (final KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private void addBuilderProxyInformation() throws IntegrationException {
        if (!proxyInfo.equals(ProxyInfo.NO_PROXY_INFO)) {
            defaultRequestConfigBuilder.setProxy(getProxyHttpHost());
            try {
                addProxyCredentials();
            } catch (final IllegalArgumentException ex) {
                throw new IntegrationException(ex);
            }
        }
    }

    private HttpHost getProxyHttpHost() {
        return new HttpHost(proxyInfo.getHost(), proxyInfo.getPort());
    }

    private void addProxyCredentials() {
        if (proxyInfo.hasAuthenticatedProxySettings()) {
            final org.apache.http.auth.Credentials credentials = new NTCredentials(proxyInfo.getUsername(), proxyInfo.getPassword(), proxyInfo.getNtlmWorkstation(), proxyInfo.getNtlmDomain());
            credentialsProvider.setCredentials(new AuthScope(proxyInfo.getHost(), proxyInfo.getPort()), credentials);
        }
    }

    private Response handleClientExecution(final HttpUriRequest request) throws IntegrationException {
        if (!initialized) {
            initialize();
        }

        try {
            final CloseableHttpClient client = clientBuilder.build();
            final URI uri = request.getURI();
            final String urlString = request.getURI().toString();
            if (alwaysTrustServerCertificate && uri.getScheme().equalsIgnoreCase("https")) {
                logger.debug("Automatically trusting the certificate for " + urlString);
            }
            logRequestHeaders(request);
            final CloseableHttpResponse closeableHttpResponse = client.execute(request);
            final Response response = new Response(client, closeableHttpResponse);
            logResponseHeaders(closeableHttpResponse);
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

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public boolean isAlwaysTrustServerCertificate() {
        return alwaysTrustServerCertificate;
    }

    public void setAlwaysTrustServerCertificate(final boolean alwaysTrustServerCertificate) {
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
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

    public IntLogger getLogger() {
        return logger;
    }
}

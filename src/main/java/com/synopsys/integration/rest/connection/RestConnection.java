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

import java.io.Closeable;
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
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

/**
 * The parent class of all rest connections.
 */
public abstract class RestConnection implements Closeable {
    public static final String ERROR_MSG_PROXY_INFO_NULL = "A RestConnection's proxy information cannot be null";

    protected final IntLogger logger;

    private int timeout = 120;
    private final ProxyInfo proxyInfo;
    private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    private final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    private final RequestConfig.Builder defaultRequestConfigBuilder = RequestConfig.custom();
    private final Map<String, String> commonRequestHeaders = new HashMap<>();
    private boolean alwaysTrustServerCertificate;
    private CloseableHttpClient client;

    public RestConnection(final IntLogger logger, final ProxyInfo proxyInfo) {
        this.logger = logger;
        this.proxyInfo = proxyInfo;
    }

    public RestConnection(final IntLogger logger, final int timeout, final ProxyInfo proxyInfo) {
        this(logger, proxyInfo);
        this.timeout = timeout;
    }

    public void connect() throws IntegrationException {
        addBuilderConnectionTimes();
        addBuilderProxyInformation();
        populateHttpClientBuilder(clientBuilder, defaultRequestConfigBuilder);
        assembleClient();
        setClient(clientBuilder.build());
        completeConnection();
    }

    /**
     * Subclasses can add to the builders any additional fields they need to successfully connect
     */
    public abstract void populateHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) throws IntegrationException;

    /**
     * Subclasses might need to do final processing to the http client (usually authentication)
     */
    public abstract void completeConnection() throws IntegrationException;

    public RequestBuilder createRequestBuilder(final HttpMethod method) throws IntegrationException {
        return createRequestBuilder(method, null);
    }

    public RequestBuilder createRequestBuilder(final HttpMethod method, final Map<String, String> additionalHeaders) throws IntegrationException {
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

    public HttpUriRequest copyHttpRequest(final HttpUriRequest request) throws IntegrationException {
        final RequestBuilder requestBuilder = RequestBuilder.copy(request);
        if (!commonRequestHeaders.isEmpty()) {
            for (final Entry<String, String> header : commonRequestHeaders.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }
        return requestBuilder.build();
    }

    public Response executeRequestWithoutException(final HttpUriRequest request) throws IntegrationException {
        final long start = System.currentTimeMillis();
        logMessage(LogLevel.TRACE, "starting request: " + request.getURI().toString());
        try {
            return handleClientExecution(request);
        } finally {
            final long end = System.currentTimeMillis();
            logMessage(LogLevel.TRACE, String.format("completed request: %s (%d ms)", request.getURI().toString(), end - start));
        }
    }

    /**
     * Will throw an exception if the status code is an error code
     */
    public Response executeRequest(final HttpUriRequest request) throws IntegrationException {
        final Response response = executeRequestWithoutException(request);

        if (response.isStatusCodeError()) {
            final Integer statusCode = response.getStatusCode();
            final String statusMessage = response.getStatusMessage();
            final String httpResponseContent = response.getContentString();
            throw new IntegrationRestException(statusCode, statusMessage, httpResponseContent,
                String.format("There was a problem trying to %s this item: %s. Error: %s %s", request.getMethod(), request.getURI(), statusCode, statusMessage));
        }

        return response;
    }

    public Optional<Response> executeGetRequestIfModifiedSince(final Request getRequest, final long timeToCheck) throws IntegrationException, IOException {
        final Request headRequest = new Request.Builder(getRequest).method(HttpMethod.HEAD).build();

        long lastModifiedOnServer = 0L;
        try (final Response headResponse = executeRequest(headRequest.createHttpRequest(commonRequestHeaders))) {
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

        return Optional.of(executeRequest(getRequest.createHttpRequest(commonRequestHeaders)));
    }

    @Override
    public void close() throws IOException {
        if (null != client) {
            client.close();
        }
    }

    protected void logRequestHeaders(final HttpUriRequest request) {
        if (isDebugLogging()) {
            final String requestName = request.getClass().getSimpleName();
            logMessage(LogLevel.TRACE, requestName + " : " + request.toString());
            logHeaders(requestName, request.getAllHeaders());
        }
    }

    protected void logResponseHeaders(final HttpResponse response) {
        if (isDebugLogging()) {
            final String responseName = response.getClass().getSimpleName();
            logMessage(LogLevel.TRACE, responseName + " : " + response.toString());
            logHeaders(responseName, response.getAllHeaders());
        }
    }

    private void addBuilderConnectionTimes() {
        defaultRequestConfigBuilder.setConnectTimeout(timeout * 1000);
        defaultRequestConfigBuilder.setSocketTimeout(timeout * 1000);
        defaultRequestConfigBuilder.setConnectionRequestTimeout(timeout * 1000);
    }

    private void assembleClient() throws IntegrationException {
        try {
            clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            clientBuilder.setDefaultRequestConfig(defaultRequestConfigBuilder.build());

            SSLContext sslContext = null;
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
        if (proxyInfo == null) {
            throw new IllegalStateException(ERROR_MSG_PROXY_INFO_NULL);
        }

        if (proxyInfo.equals(ProxyInfo.NO_PROXY_INFO)) {
            defaultRequestConfigBuilder.setProxy(getProxyHttpHost());
            try {
                addProxyCredentials();
            } catch (final IllegalArgumentException ex) {
                throw new IntegrationException(ex);
            }
        }
    }

    private HttpHost getProxyHttpHost() {
        final HttpHost httpHost = new HttpHost(proxyInfo.getHost(), proxyInfo.getPort());
        return httpHost;
    }

    private void addProxyCredentials() {
        if (proxyInfo.hasAuthenticatedProxySettings()) {
            final org.apache.http.auth.Credentials creds = new NTCredentials(proxyInfo.getUsername(), proxyInfo.getPassword(), proxyInfo.getNtlmWorkstation(), proxyInfo.getNtlmDomain());
            credentialsProvider.setCredentials(new AuthScope(proxyInfo.getHost(), proxyInfo.getPort()), creds);
        }
    }

    private Response handleClientExecution(final HttpUriRequest request) throws IntegrationException {
        if (client != null) {
            try {
                final URI uri = request.getURI();
                final String urlString = request.getURI().toString();
                if (alwaysTrustServerCertificate && uri.getScheme().equalsIgnoreCase("https") && logger != null) {
                    logger.debug("Automatically trusting the certificate for " + urlString);
                }
                logRequestHeaders(request);
                final CloseableHttpResponse closeableHttpResponse = client.execute(request);
                final Response response = new Response(closeableHttpResponse);
                logResponseHeaders(closeableHttpResponse);
                return response;
            } catch (final IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        } else {
            connect();
            final HttpUriRequest newRequest = copyHttpRequest(request);
            return handleClientExecution(newRequest);
        }
    }

    private void logMessage(final LogLevel level, final String txt) {
        if (logger != null) {
            if (level == LogLevel.ERROR) {
                logger.error(txt);
            } else if (level == LogLevel.WARN) {
                logger.warn(txt);
            } else if (level == LogLevel.INFO) {
                logger.info(txt);
            } else if (level == LogLevel.DEBUG) {
                logger.debug(txt);
            } else if (level == LogLevel.TRACE) {
                logger.trace(txt);
            }
        }
    }

    private boolean isDebugLogging() {
        return logger != null && logger.getLogLevel() == LogLevel.TRACE;
    }

    private void logHeaders(final String requestOrResponseName, final Header[] headers) {
        if (headers != null && headers.length > 0) {
            logMessage(LogLevel.TRACE, requestOrResponseName + " headers : ");
            for (final Header header : headers) {
                logMessage(LogLevel.TRACE, String.format("Header %s : %s", header.getName(), header.getValue()));
            }
        } else {
            logMessage(LogLevel.TRACE, requestOrResponseName + " does not have any headers.");
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

    public CloseableHttpClient getClient() {
        return client;
    }

    public void setClient(final CloseableHttpClient client) {
        this.client = client;
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

}

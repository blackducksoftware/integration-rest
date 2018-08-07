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
package com.blackducksoftware.integration.rest.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.rest.HttpMethod;
import com.blackducksoftware.integration.rest.RestConstants;
import com.blackducksoftware.integration.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;
import com.blackducksoftware.integration.rest.request.Request;
import com.blackducksoftware.integration.rest.request.Response;

/**
 * The parent class of all Hub connections.
 */
public abstract class RestConnection implements Closeable {
    public static final String ERROR_MSG_PROXY_INFO_NULL = "A RestConnection's proxy information cannot be null";

    protected final IntLogger logger;

    private final URL baseUrl;
    private int timeout = 120;
    private final ProxyInfo proxyInfo;
    private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    private final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    private final RequestConfig.Builder defaultRequestConfigBuilder = RequestConfig.custom();
    private final Map<String, String> commonRequestHeaders = new HashMap<>();
    private boolean alwaysTrustServerCertificate;
    private CloseableHttpClient client;

    public RestConnection(IntLogger logger, URL baseUrl, ProxyInfo proxyInfo) {
        this.logger = logger;
        this.baseUrl = baseUrl;
        this.proxyInfo = proxyInfo;
    }

    public RestConnection(IntLogger logger, URL baseUrl, int timeout, ProxyInfo proxyInfo) {
        this(logger, baseUrl, proxyInfo);
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

    public RequestBuilder createRequestBuilder(HttpMethod method) throws IntegrationException {
        return createRequestBuilder(method, null);
    }

    public RequestBuilder createRequestBuilder(HttpMethod method, Map<String, String> additionalHeaders) throws IntegrationException {
        if (method == null) {
            throw new IntegrationException("Missing field 'method'");
        }
        RequestBuilder requestBuilder = RequestBuilder.create(method.name());

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.putAll(commonRequestHeaders);
        if (additionalHeaders != null && !additionalHeaders.isEmpty()) {
            requestHeaders.putAll(additionalHeaders);
        }
        for (Entry<String, String> header : requestHeaders.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }
        if (baseUrl != null) {
            try {
                requestBuilder.setUri(baseUrl.toURI());
            } catch (URISyntaxException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        }
        return requestBuilder;
    }

    public HttpUriRequest createHttpRequest(Request request) throws IntegrationException {
        if (request == null) {
            throw new IntegrationException("Missing the Request");
        }
        if (request.getMethod() == null) {
            throw new IntegrationException("Missing the HttpMethod");
        }
        try {
            URIBuilder uriBuilder;
            if (StringUtils.isNotBlank(request.getUri())) {
                uriBuilder = new URIBuilder(request.getUri());
            } else if (baseUrl != null) {
                uriBuilder = new URIBuilder(baseUrl.toURI());
            } else {
                throw new IntegrationException("Missing the URI");
            }
            String mimeType = ContentType.APPLICATION_JSON.getMimeType();
            Charset bodyEncoding = Charsets.UTF_8;
            if (StringUtils.isNotBlank(request.getMimeType())) {
                mimeType = request.getMimeType();
            }
            if (request.getBodyEncoding() != null) {
                bodyEncoding = request.getBodyEncoding();
            }
            RequestBuilder requestBuilder = RequestBuilder.create(request.getMethod().name());
            if (HttpMethod.GET == request.getMethod() && (request.getAdditionalHeaders() == null || request.getAdditionalHeaders().isEmpty() || !request.getAdditionalHeaders().containsKey(HttpHeaders.ACCEPT))) {
                requestBuilder.addHeader(HttpHeaders.ACCEPT, mimeType);
            }
            requestBuilder.setCharset(bodyEncoding);
            if (request.getAdditionalHeaders() != null && !request.getAdditionalHeaders().isEmpty()) {
                for (Entry<String, String> header : request.getAdditionalHeaders().entrySet()) {
                    requestBuilder.addHeader(header.getKey(), header.getValue());
                }
            }
            if (commonRequestHeaders != null && !commonRequestHeaders.isEmpty()) {
                for (Entry<String, String> header : commonRequestHeaders.entrySet()) {
                    requestBuilder.addHeader(header.getKey(), header.getValue());
                }
            }
            Map<String, Set<String>> populatedQueryParameters = request.getPopulatedQueryParameters();
            if (!populatedQueryParameters.isEmpty()) {
                populatedQueryParameters.forEach((paramKey, paramValues) -> {
                    paramValues.forEach((paramValue) -> {
                        uriBuilder.addParameter(paramKey, paramValue);
                    });
                });
            }
            requestBuilder.setUri(uriBuilder.build());
            HttpEntity entity = request.createHttpEntity();
            if (entity != null) {
                requestBuilder.setEntity(entity);
            }
            return requestBuilder.build();
        } catch (URISyntaxException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public HttpUriRequest copyHttpRequest(HttpUriRequest request) throws IntegrationException {
        RequestBuilder requestBuilder = RequestBuilder.copy(request);
        if (commonRequestHeaders != null && !commonRequestHeaders.isEmpty()) {
            for (Entry<String, String> header : commonRequestHeaders.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }
        return requestBuilder.build();
    }

    public Response executeRequest(Request request) throws IntegrationException {
        return executeRequest(createHttpRequest(request));
    }

    public Response executeRequest(HttpUriRequest request) throws IntegrationException {
        long start = System.currentTimeMillis();
        logMessage(LogLevel.TRACE, "starting request: " + request.getURI().toString());
        try {
            return handleClientExecution(request, 0);
        } finally {
            long end = System.currentTimeMillis();
            logMessage(LogLevel.TRACE, String.format("completed request: %s (%d ms)", request.getURI().toString(), end - start));
        }
    }

    public Optional<Response> executeGetRequestIfModifiedSince(Request getRequest, long timeToCheck) throws IntegrationException, IOException {
        Request headRequest = new Request.Builder(getRequest).method(HttpMethod.HEAD).build();

        long lastModifiedOnServer = 0L;
        try (Response headResponse = executeRequest(headRequest)) {
            lastModifiedOnServer = headResponse.getLastModified();
            logger.debug(String.format("Last modified on server: %d", lastModifiedOnServer));
        } catch (IntegrationException e) {
            logger.error("Couldn't get the Last-Modified header from the server.");
            throw e;
        }

        if (lastModifiedOnServer == timeToCheck) {
            logger.debug("The request has not been modified since it was last checked - skipping.");
            return Optional.empty();
        }

        return Optional.of(executeRequest(getRequest));
    }

    @Override
    public void close() throws IOException {
        if (null != client) {
            client.close();
        }
    }

    protected void logRequestHeaders(HttpUriRequest request) {
        if (isDebugLogging()) {
            String requestName = request.getClass().getSimpleName();
            logMessage(LogLevel.TRACE, requestName + " : " + request.toString());
            logHeaders(requestName, request.getAllHeaders());
        }
    }

    protected void logResponseHeaders(HttpResponse response) {
        if (isDebugLogging()) {
            String responseName = response.getClass().getSimpleName();
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
            HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
            SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
            clientBuilder.setSSLSocketFactory(connectionFactory);
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private void addBuilderProxyInformation() throws IntegrationException {
        if (proxyInfo == null) {
            throw new IllegalStateException(ERROR_MSG_PROXY_INFO_NULL);
        }

        if (proxyInfo.shouldUseProxyForUrl(baseUrl)) {
            defaultRequestConfigBuilder.setProxy(getProxyHttpHost());
            try {
                addProxyCredentials();
            } catch (IllegalArgumentException | EncryptionException ex) {
                throw new IntegrationException(ex);
            }
        }
    }

    private HttpHost getProxyHttpHost() {
        HttpHost httpHost = new HttpHost(proxyInfo.getHost(), proxyInfo.getPort());
        return httpHost;
    }

    private void addProxyCredentials() throws IntegrationException {
        if (proxyInfo.hasAuthenticatedProxySettings()) {
            org.apache.http.auth.Credentials creds = new NTCredentials(proxyInfo.getUsername(), proxyInfo.getDecryptedPassword(), proxyInfo.getNtlmWorkstation(), proxyInfo.getNtlmDomain());
            credentialsProvider.setCredentials(new AuthScope(proxyInfo.getHost(), proxyInfo.getPort()), creds);
        }
    }

    private Response handleClientExecution(HttpUriRequest request, int retryCount) throws IntegrationException {
        if (client != null) {
            try {
                URI uri = request.getURI();
                String urlString = request.getURI().toString();
                if (alwaysTrustServerCertificate && uri.getScheme().equalsIgnoreCase("https") && logger != null) {
                    logger.debug("Automatically trusting the certificate for " + urlString);
                }
                logRequestHeaders(request);
                CloseableHttpResponse response = client.execute(request);
                int statusCode = response.getStatusLine().getStatusCode();
                String statusMessage = response.getStatusLine().getReasonPhrase();
                if (statusCode < RestConstants.OK_200 || statusCode >= RestConstants.MULT_CHOICE_300) {
                    try {
                        if (statusCode == RestConstants.UNAUTHORIZED_401 && retryCount < 2) {
                            connect();
                            HttpUriRequest newRequest = copyHttpRequest(request);
                            return handleClientExecution(newRequest, retryCount + 1);
                        } else {
                            throw new IntegrationRestException(statusCode, statusMessage, String.format("There was a problem trying to %s this item: %s. Error: %s %s", request.getMethod(), urlString, statusCode, statusMessage));
                        }
                    } finally {
                        response.close();
                    }
                }
                logResponseHeaders(response);
                return new Response(response);
            } catch (IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        } else {
            connect();
            HttpUriRequest newRequest = copyHttpRequest(request);
            return handleClientExecution(newRequest, retryCount);
        }
    }

    private void logMessage(LogLevel level, String txt) {
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

    private void logHeaders(String requestOrResponseName, Header[] headers) {
        if (headers != null && headers.length > 0) {
            logMessage(LogLevel.TRACE, requestOrResponseName + " headers : ");
            for (Header header : headers) {
                logMessage(LogLevel.TRACE, String.format("Header %s : %s", header.getName(), header.getValue()));
            }
        } else {
            logMessage(LogLevel.TRACE, requestOrResponseName + " does not have any headers.");
        }
    }

    @Override
    public String toString() {
        return "RestConnection [baseUrl=" + baseUrl + "]";
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isAlwaysTrustServerCertificate() {
        return alwaysTrustServerCertificate;
    }

    public void setAlwaysTrustServerCertificate(boolean alwaysTrustServerCertificate) {
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
    }

    public CloseableHttpClient getClient() {
        return client;
    }

    public void setClient(CloseableHttpClient client) {
        this.client = client;
    }

    public URL getBaseUrl() {
        return baseUrl;
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

    public void addCommonRequestHeader(String key, String value) {
        commonRequestHeaders.put(key, value);
    }

    public void addCommonRequestHeaders(Map<String, String> commonRequestHeaders) {
        commonRequestHeaders.putAll(commonRequestHeaders);
    }

}

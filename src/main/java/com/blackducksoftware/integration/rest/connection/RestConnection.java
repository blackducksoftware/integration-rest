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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

/**
 * The parent class of all Hub connections.
 */
public abstract class RestConnection implements Closeable {
    public static final String ERROR_MSG_PROXY_INFO_NULL = "A RestConnection's proxy information cannot be null";

    public final Gson gson = new GsonBuilder().setDateFormat(RestConstants.JSON_DATE_FORMAT).create();
    public final JsonParser jsonParser = new JsonParser();
    public final Map<String, String> commonRequestHeaders = new HashMap<>();
    public final URL baseUrl;
    private final ProxyInfo proxyInfo;
    private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    private final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    private final RequestConfig.Builder defaultRequestConfigBuilder = RequestConfig.custom();
    public int timeout = 120;
    public boolean alwaysTrustServerCertificate;
    public IntLogger logger;
    private CloseableHttpClient client;

    public RestConnection(final IntLogger logger, final URL baseUrl, final int timeout, final ProxyInfo proxyInfo) {
        this.logger = logger;
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.proxyInfo = proxyInfo;
    }

    public static Date parseDateString(final String dateString) throws ParseException {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.parse(dateString);
    }

    public static String formatDate(final Date date) {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public void connect() throws IntegrationException {
        addBuilderConnectionTimes();
        addBuilderProxyInformation();
        addBuilderAuthentication();
        assembleClient();
        setClient(clientBuilder.build());
        clientAuthenticate();
    }

    public abstract void addBuilderAuthentication() throws IntegrationException;

    public abstract void clientAuthenticate() throws IntegrationException;

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
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private void addBuilderProxyInformation() throws IntegrationException {
        if (this.proxyInfo == null) {
            throw new IllegalStateException(ERROR_MSG_PROXY_INFO_NULL);
        }

        if (this.proxyInfo.shouldUseProxyForUrl(baseUrl)) {
            defaultRequestConfigBuilder.setProxy(getProxyHttpHost());
            try {
                addProxyCredentials();
            } catch (IllegalArgumentException | EncryptionException ex) {
                throw new IntegrationException(ex);
            }
        }
    }

    private HttpHost getProxyHttpHost() {
        final HttpHost httpHost = new HttpHost(this.proxyInfo.getHost(), this.proxyInfo.getPort());
        return httpHost;
    }

    private void addProxyCredentials() throws IntegrationException {
        if (this.proxyInfo.hasAuthenticatedProxySettings()) {
            final org.apache.http.auth.Credentials creds = new NTCredentials(this.proxyInfo.getUsername(), this.proxyInfo.getDecryptedPassword(), this.proxyInfo.getNtlmWorkstation(), this.proxyInfo.getNtlmDomain());
            credentialsProvider.setCredentials(new AuthScope(this.proxyInfo.getHost(), this.proxyInfo.getPort()), creds);
        }
    }

    public RequestBuilder createRequestBuilder(final HttpMethod method) throws IntegrationException {
        return createRequestBuilder(method, null);
    }

    public RequestBuilder createRequestBuilder(final HttpMethod method, final Map<String, String> additionalHeaders) throws IntegrationException {
        if (method == null) {
            throw new IntegrationException("Missing field 'method'");
        }
        final RequestBuilder requestBuilder = RequestBuilder.create(method.name());

        final Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.putAll(commonRequestHeaders);
        if (additionalHeaders != null && !additionalHeaders.isEmpty()) {
            requestHeaders.putAll(additionalHeaders);
        }
        for (final Entry<String, String> header : requestHeaders.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }
        if (baseUrl != null) {
            try {
                requestBuilder.setUri(baseUrl.toURI());
            } catch (final URISyntaxException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        }
        return requestBuilder;
    }

    public HttpUriRequest createHttpRequest(final Request request) throws IntegrationException {
        if (request == null) {
            throw new IntegrationException("Missing the Request");
        }
        if (request.getMethod() == null) {
            throw new IntegrationException("Missing the HttpMethod");
        }
        try {
            URIBuilder uriBuilder = null;
            if (StringUtils.isNotBlank(request.getUri())) {
                uriBuilder = new URIBuilder(request.getUri());
            } else if (baseUrl != null) {
                uriBuilder = new URIBuilder(baseUrl.toURI());
            }
            if (uriBuilder == null) {
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
            final RequestBuilder requestBuilder = RequestBuilder.create(request.getMethod().name());
            if (HttpMethod.GET == request.getMethod() && (request.getAdditionalHeaders() == null || request.getAdditionalHeaders().isEmpty() || !request.getAdditionalHeaders().containsKey(HttpHeaders.ACCEPT))) {
                requestBuilder.addHeader(HttpHeaders.ACCEPT, mimeType);
            }
            requestBuilder.setCharset(bodyEncoding);
            if (request.getAdditionalHeaders() != null && !request.getAdditionalHeaders().isEmpty()) {
                for (final Entry<String, String> header : request.getAdditionalHeaders().entrySet()) {
                    requestBuilder.addHeader(header.getKey(), header.getValue());
                }
            }
            if (commonRequestHeaders != null && !commonRequestHeaders.isEmpty()) {
                for (final Entry<String, String> header : commonRequestHeaders.entrySet()) {
                    requestBuilder.addHeader(header.getKey(), header.getValue());
                }
            }
            final Map<String, String> populatedQueryParameters = request.getPopulatedQueryParameters();
            if (!populatedQueryParameters.isEmpty()) {
                for (final Entry<String, String> queryParameter : populatedQueryParameters.entrySet()) {
                    uriBuilder.addParameter(queryParameter.getKey(), queryParameter.getValue());
                }
            }
            requestBuilder.setUri(uriBuilder.build());
            final HttpEntity entity = request.createHttpEntity(gson);
            if (entity != null) {
                requestBuilder.setEntity(entity);
            }
            return requestBuilder.build();
        } catch (final URISyntaxException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public HttpUriRequest copyHttpRequest(final HttpUriRequest request) throws IntegrationException {
        final RequestBuilder requestBuilder = RequestBuilder.copy(request);
        if (commonRequestHeaders != null && !commonRequestHeaders.isEmpty()) {
            for (final Entry<String, String> header : commonRequestHeaders.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }
        return requestBuilder.build();
    }

    public Response executeRequest(final Request request) throws IntegrationException {
        return executeRequest(createHttpRequest(request));
    }

    public Response executeRequest(final HttpUriRequest request) throws IntegrationException {
        final long start = System.currentTimeMillis();
        logMessage(LogLevel.TRACE, "starting request: " + request.getURI().toString());
        try {
            return handleClientExecution(request, 0);
        } finally {
            final long end = System.currentTimeMillis();
            logMessage(LogLevel.TRACE, String.format("completed request: %s (%d ms)", request.getURI().toString(), end - start));
        }
    }

    private Response handleClientExecution(final HttpUriRequest request, final int retryCount) throws IntegrationException {
        if (client != null) {
            try {
                final URI uri = request.getURI();
                final String urlString = request.getURI().toString();
                if (alwaysTrustServerCertificate && uri.getScheme().equalsIgnoreCase("https") && logger != null) {
                    logger.debug("Automatically trusting the certificate for " + urlString);
                }
                logRequestHeaders(request);
                final CloseableHttpResponse response = client.execute(request);
                final int statusCode = response.getStatusLine().getStatusCode();
                final String statusMessage = response.getStatusLine().getReasonPhrase();
                if (statusCode < RestConstants.OK_200 || statusCode >= RestConstants.MULT_CHOICE_300) {
                    try {
                        if (statusCode == RestConstants.UNAUTHORIZED_401 && retryCount < 2) {
                            connect();
                            final HttpUriRequest newRequest = copyHttpRequest(request);
                            return handleClientExecution(newRequest, retryCount + 1);
                        } else {
                            throw new IntegrationRestException(statusCode, statusMessage,
                                    String.format("There was a problem trying to %s this item: %s. Error: %s %s", request.getMethod(), urlString, statusCode, statusMessage));
                        }
                    } finally {
                        response.close();
                    }
                }
                logResponseHeaders(response);
                return new Response(response);
            } catch (final IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        } else {
            connect();
            final HttpUriRequest newRequest = copyHttpRequest(request);
            return handleClientExecution(newRequest, retryCount);
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

    @Override
    public String toString() {
        return "RestConnection [baseUrl=" + baseUrl + "]";
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

    @Override
    public void close() throws IOException {
        if (null != client) {
            client.close();
        }
    }
}

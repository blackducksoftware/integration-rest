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
package com.synopsys.integration.rest.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
import com.synopsys.integration.rest.response.DefaultResponse;
import com.synopsys.integration.rest.response.ErrorResponse;
import com.synopsys.integration.rest.response.Response;

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

    private final CredentialsProvider credentialsProvider;
    private final HttpClientBuilder clientBuilder;
    private final RequestConfig.Builder defaultRequestConfigBuilder;
    private final Map<String, String> commonRequestHeaders;

    public IntHttpClient(IntLogger logger, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo) {
        this(logger, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo, new BasicCredentialsProvider(), HttpClientBuilder.create(), RequestConfig.custom(), new HashMap<>());
    }

    public IntHttpClient(IntLogger logger, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, CredentialsProvider credentialsProvider, HttpClientBuilder clientBuilder,
        RequestConfig.Builder defaultRequestConfigBuilder, Map<String, String> commonRequestHeaders) {
        this.logger = logger;
        this.proxyInfo = proxyInfo;
        this.timeoutInSeconds = timeoutInSeconds;
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
        this.credentialsProvider = credentialsProvider;
        this.clientBuilder = clientBuilder;
        this.defaultRequestConfigBuilder = defaultRequestConfigBuilder;
        this.commonRequestHeaders = commonRequestHeaders;

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
        addToHttpClientBuilder(clientBuilder, defaultRequestConfigBuilder);
        addBuilderCredentialsProvider();
        addBuilderSSLContext();
    }

    public final RequestBuilder createRequestBuilder(HttpMethod method) throws IntegrationException {
        return createRequestBuilder(method, null);
    }

    public final RequestBuilder createRequestBuilder(HttpMethod method, Map<String, String> additionalHeaders) throws IntegrationException {
        if (method == null) {
            throw new IntegrationException("Missing field 'method'");
        }
        RequestBuilder requestBuilder = RequestBuilder.create(method.name());

        Map<String, String> requestHeaders = new HashMap<>(commonRequestHeaders);
        if (additionalHeaders != null && !additionalHeaders.isEmpty()) {
            requestHeaders.putAll(additionalHeaders);
        }
        for (Entry<String, String> header : requestHeaders.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }

        return requestBuilder;
    }

    public HttpUriRequest createHttpUriRequest(Request request) throws IntegrationException {
        if (request.getMethod() == null) {
            throw new IntegrationException("Missing the HttpMethod");
        }
        if (request.getUrl() == null) {
            throw new IntegrationException("Missing the HttpUrl");
        }

        RequestBuilder requestBuilder = RequestBuilder.create(request.getMethod().name());

        URIBuilder uriBuilder = new URIBuilder(request.getUrl().uri());
        Map<String, Set<String>> populatedQueryParameters = request.getPopulatedQueryParameters();
        populatedQueryParameters.forEach((paramKey, paramValues) -> {
            paramValues.forEach((paramValue) -> {
                uriBuilder.addParameter(paramKey, paramValue);
            });
        });
        try {
            requestBuilder.setUri(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new IntegrationException("Invalid url with parameters: " + uriBuilder.toString());
        }

        String acceptMimeType = Request.DEFAULT_ACCEPT_MIME_TYPE;
        Charset bodyEncoding = StandardCharsets.UTF_8;
        if (StringUtils.isNotBlank(request.getAcceptMimeType())) {
            acceptMimeType = request.getAcceptMimeType();
        }

        if (request.getBodyEncoding() != null) {
            bodyEncoding = request.getBodyEncoding();
        }

        if (HttpMethod.GET == request.getMethod() && (request.getHeaders() == null || request.getHeaders().isEmpty() || !request.getHeaders().containsKey(HttpHeaders.ACCEPT))) {
            requestBuilder.addHeader(HttpHeaders.ACCEPT, acceptMimeType);
        }
        requestBuilder.setCharset(bodyEncoding);
        for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }
        for (Map.Entry<String, String> header : commonRequestHeaders.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }

        HttpEntity entity = request.createHttpEntity();
        if (entity != null) {
            requestBuilder.setEntity(entity);
        }
        return requestBuilder.build();
    }

    public Response execute(Request request) throws IntegrationException {
        HttpUriRequest httpUriRequest = createHttpUriRequest(request);
        return execute(httpUriRequest);
    }

    public Response execute(HttpUriRequest request) throws IntegrationException {
        long start = System.currentTimeMillis();
        logger.trace("starting request: " + request.getURI().toString());
        try {
            return handleClientExecution(request);
        } finally {
            long end = System.currentTimeMillis();
            logger.trace(String.format("completed request: %s (%d ms)", request.getURI().toString(), end - start));
        }
    }

    public Optional<Response> executeGetRequestIfModifiedSince(Request getRequest, long timeToCheck) throws IntegrationException, IOException {
        Request headRequest = new Request.Builder(getRequest).method(HttpMethod.HEAD).build();

        long lastModifiedOnServer = 0L;
        try (Response headResponse = execute(createHttpUriRequest(headRequest))) {
            //ejk 2020-09-01 - If a successful HEAD request can't be made, we can't make any reasonable decision about what the lastModified time is, so an Exception is warranted.
            headResponse.throwExceptionForError();

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

        return Optional.of(execute(createHttpUriRequest(getRequest)));
    }

    public final void logRequestHeaders(HttpUriRequest request) {
        String requestName = request.getClass().getSimpleName();
        logger.trace(requestName + " : " + request.toString());
        logHeaders(requestName, request.getAllHeaders());
    }

    public final void logResponseHeaders(HttpResponse response) {
        String responseName = response.getClass().getSimpleName();
        logger.trace(responseName + " : " + response.toString());
        logHeaders(responseName, response.getAllHeaders());
    }

    protected void addToHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) {
        // Subclasses can optionally add to the builders any additional fields they need to successfully initialize
    }

    protected void handleErrorResponse(HttpUriRequest request, Response response) {
        // Subclasses can optionally handle an error response and modify the request
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
            SSLContext sslContext;
            HostnameVerifier hostnameVerifier;
            if (alwaysTrustServerCertificate) {
                logger.warn("Automatically trusting server certificates - not recommended for production use.");
                sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustAllStrategy()).build();
                hostnameVerifier = new NoopHostnameVerifier();
            } else {
                sslContext = SSLContexts.createDefault();
                hostnameVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier();
            }
            SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            clientBuilder.setSSLSocketFactory(connectionFactory);
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private void addBuilderProxyInformation() {
        if (proxyInfo.shouldUseProxy()) {
            defaultRequestConfigBuilder.setProxy(new HttpHost(proxyInfo.getHost().orElse(null), proxyInfo.getPort()));
            if (proxyInfo.hasAuthenticatedProxySettings()) {
                org.apache.http.auth.Credentials credentials = new NTCredentials(proxyInfo.getUsername().orElse(null), proxyInfo.getPassword().orElse(null), proxyInfo.getNtlmWorkstation().orElse(null),
                    proxyInfo.getNtlmDomain().orElse(null));
                credentialsProvider.setCredentials(new AuthScope(proxyInfo.getHost().orElse(null), proxyInfo.getPort()), credentials);
            }
        }
    }

    private Response handleClientExecution(HttpUriRequest request) throws IntegrationException {
        try {
            CloseableHttpClient client = clientBuilder.build();
            logRequestHeaders(request);
            CloseableHttpResponse closeableHttpResponse = client.execute(request);
            Response response = new DefaultResponse(request, client, closeableHttpResponse);
            logResponseHeaders(closeableHttpResponse);
            if (response.isStatusCodeError()) {
                handleErrorResponse(request, response);
            }
            return response;
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private void logHeaders(String requestOrResponseName, Header[] headers) {
        if (headers != null && headers.length > 0) {
            logger.trace(requestOrResponseName + " headers : ");
            for (Header header : headers) {
                logger.trace(String.format("Header %s : %s", header.getName(), header.getValue()));
            }
        } else {
            logger.trace(requestOrResponseName + " does not have any headers.");
        }
    }

    public void throwExceptionForError(Response response) throws IntegrationException {
        try {
            response.throwExceptionForError();
        } catch (IntegrationRestException e) {
            throw transformException(e);
        }
    }

    private IntegrationException transformException(IntegrationRestException e) {
        String httpResponseContent = e.getHttpResponseContent();
        Optional<ErrorResponse> optionalErrorResponse = extractErrorResponse(httpResponseContent);
        if (optionalErrorResponse.isPresent()) {
            ErrorResponse errorResponse = optionalErrorResponse.get();
            String apiExceptionErrorMessage = String.format("%s [HTTP Error]: %s", errorResponse.getErrorMessage(), e.getMessage());
            return new ApiException(e, apiExceptionErrorMessage, errorResponse.getErrorCode());
        } else {
            return e;
        }
    }

    public Optional<ErrorResponse> extractErrorResponse(String responseContent) {
        if (StringUtils.isNotBlank(responseContent)) {
            try {
                String errorMessage = JsonPath.read(responseContent, "$.errorMessage");
                String errorCode = JsonPath.read(responseContent, "$.errorCode");
                if (!StringUtils.isAllBlank(errorMessage, errorCode)) {
                    return Optional.of(new ErrorResponse(errorMessage, errorCode));
                }
            } catch (Exception ignored) {
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

    public void addCommonRequestHeader(String key, String value) {
        commonRequestHeaders.put(key, value);
    }

    public void addCommonRequestHeaders(Map<String, String> commonRequestHeaders) {
        this.commonRequestHeaders.putAll(commonRequestHeaders);
    }

    public String removeCommonRequestHeader(String key) {
        return commonRequestHeaders.remove(key);
    }

    public IntLogger getLogger() {
        return logger;
    }

}

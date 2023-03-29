/*
 * integration-rest
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
import java.util.function.Supplier;

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
import org.apache.http.client.config.CookieSpecs;
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
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.body.BodyContent;
import com.synopsys.integration.rest.body.BodyContentConverter;
import com.synopsys.integration.rest.exception.ApiException;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.DefaultResponse;
import com.synopsys.integration.rest.response.ErrorResponse;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.MaskedStringFieldToStringBuilder;

/**
 * A basic, extendable http client.
 */
public class IntHttpClient {
    public static final Supplier<SSLContext> SSL_CONTEXT_SUPPLIER = SSLContexts::createDefault;
    public static final String ERROR_MSG_PROXY_INFO_NULL = "A IntHttpClient's proxy information cannot be null.";
    public static final int DEFAULT_TIMEOUT = 120;

    protected final IntLogger logger;
    private final Gson gson;
    private final ProxyInfo proxyInfo;

    private final int timeoutInSeconds;
    private final boolean alwaysTrustServerCertificate;

    private final CredentialsProvider credentialsProvider;
    private final HttpClientBuilder clientBuilder;
    private final RequestConfig.Builder defaultRequestConfigBuilder;
    private final Map<String, String> commonRequestHeaders;

    private SSLContext sslContext;

    public IntHttpClient(IntLogger logger, Gson gson, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo) {
        this(logger, gson, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo, SSL_CONTEXT_SUPPLIER.get());
    }

    public IntHttpClient(IntLogger logger, Gson gson, int timeoutInSeconds, ProxyInfo proxyInfo, SSLContext sslContext) {
        this(logger, gson, timeoutInSeconds, false, proxyInfo, sslContext);
    }

    public IntHttpClient(IntLogger logger, Gson gson, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, CredentialsProvider credentialsProvider, HttpClientBuilder clientBuilder,
        RequestConfig.Builder defaultRequestConfigBuilder, Map<String, String> commonRequestHeaders) {
        this(logger, gson, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo, credentialsProvider, clientBuilder, defaultRequestConfigBuilder, commonRequestHeaders, SSL_CONTEXT_SUPPLIER.get());
    }

    public IntHttpClient(IntLogger logger, Gson gson, int timeoutInSeconds, ProxyInfo proxyInfo, CredentialsProvider credentialsProvider, HttpClientBuilder clientBuilder,
        RequestConfig.Builder defaultRequestConfigBuilder, Map<String, String> commonRequestHeaders, SSLContext sslContext) {
        this(logger, gson, timeoutInSeconds, false, proxyInfo, credentialsProvider, clientBuilder, defaultRequestConfigBuilder, commonRequestHeaders, sslContext);
    }

    private IntHttpClient(IntLogger logger, Gson gson, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, SSLContext sslContext) {
        this(logger, gson, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo, new BasicCredentialsProvider(), HttpClientBuilder.create(), RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD), new HashMap<>(), sslContext);
    }

    private IntHttpClient(IntLogger logger, Gson gson, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, CredentialsProvider credentialsProvider, HttpClientBuilder clientBuilder,
        RequestConfig.Builder defaultRequestConfigBuilder, Map<String, String> commonRequestHeaders, SSLContext sslContext) {
        this.logger = logger;
        this.gson = gson;
        this.proxyInfo = proxyInfo;
        this.timeoutInSeconds = timeoutInSeconds;
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
        this.credentialsProvider = credentialsProvider;
        this.clientBuilder = clientBuilder;
        this.defaultRequestConfigBuilder = defaultRequestConfigBuilder;
        this.commonRequestHeaders = commonRequestHeaders;
        this.sslContext = sslContext;

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

    public HttpUriRequest createHttpUriRequest(Request integrationRequest) throws IntegrationException {
        if (integrationRequest.getMethod() == null) {
            throw new IntegrationException("Missing the HttpMethod");
        }
        if (integrationRequest.getUrl() == null) {
            throw new IntegrationException("Missing the HttpUrl");
        }

        RequestBuilder requestBuilder = RequestBuilder.create(integrationRequest.getMethod().name());

        URIBuilder uriBuilder = new URIBuilder(integrationRequest.getUrl().uri());
        Map<String, Set<String>> populatedQueryParameters = integrationRequest.getPopulatedQueryParameters();
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

        Charset bodyEncoding = StandardCharsets.UTF_8;
        if (integrationRequest.getBodyEncoding() != null) {
            bodyEncoding = integrationRequest.getBodyEncoding();
        }
        requestBuilder.setCharset(bodyEncoding);

        for (Map.Entry<String, String> header : integrationRequest.getHeaders().entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }
        for (Map.Entry<String, String> header : commonRequestHeaders.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }

        BodyContent bodyContent = integrationRequest.getBodyContent();
        if (null != bodyContent) {
            BodyContentConverter bodyContentConverter = new BodyContentConverter(gson);
            HttpEntity httpEntity = bodyContent.createEntity(bodyContentConverter);
            requestBuilder.setEntity(httpEntity);
        }

        return requestBuilder.build();
    }

    public Response execute(Request request) throws IntegrationException {
        return execute(request, new BasicHttpContext());
    }

    public Response execute(Request request, HttpContext httpContext) throws IntegrationException {
        HttpUriRequest httpUriRequest = createHttpUriRequest(request);
        return execute(httpUriRequest, httpContext);
    }

    public Response execute(HttpUriRequest request) throws IntegrationException {
        return execute(request, new BasicHttpContext());
    }

    public Response execute(HttpUriRequest request, HttpContext httpContext) throws IntegrationException {
        long start = System.currentTimeMillis();
        logger.trace("starting request: " + request.getURI().toString());
        try {
            return handleClientExecution(request, httpContext);
        } finally {
            long end = System.currentTimeMillis();
            logger.trace(String.format("completed request: %s (%d ms)", request.getURI().toString(), end - start));
        }
    }

    public Optional<Response> executeGetRequestIfModifiedSince(Request getRequest, long timeToCheck) throws IntegrationException, IOException {
        return executeGetRequestIfModifiedSince(getRequest, timeToCheck, new BasicHttpContext());
    }

    public Optional<Response> executeGetRequestIfModifiedSince(Request getRequest, long timeToCheck, HttpContext httpContext) throws IntegrationException, IOException {
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

        return Optional.of(execute(createHttpUriRequest(getRequest), httpContext));
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
            HostnameVerifier hostnameVerifier;
            if (alwaysTrustServerCertificate) {
                logger.error("Automatically trusting server certificates - not recommended for production use.");
                sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustAllStrategy()).build();
                hostnameVerifier = new NoopHostnameVerifier();
            } else {
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

    private Response handleClientExecution(HttpUriRequest request, HttpContext httpContext) throws IntegrationException {
        try {
            CloseableHttpClient client = clientBuilder.build();
            logRequestHeaders(request);

            CloseableHttpResponse closeableHttpResponse = client.execute(request, httpContext);
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
                String name = header.getName();
                String value = getLoggableValue(header);
                logger.trace(String.format("Header %s : %s", name, value));
            }
        } else {
            logger.trace(requestOrResponseName + " does not have any headers.");
        }
    }

    private String getLoggableValue(Header header) {
        if (HttpHeaders.AUTHORIZATION.equals(header.getName())) {
            return MaskedStringFieldToStringBuilder.MASKED_VALUE;
        }

        return header.getValue();
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

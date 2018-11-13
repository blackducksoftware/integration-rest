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
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.util.BuilderStatus;

public abstract class RestConnectionDecorator implements RestConnection {
    private final RestConnection restConnection;

    public RestConnectionDecorator(final RestConnection restConnection) {
        this.restConnection = restConnection;
    }

    @Override
    public void initialize() throws IntegrationException {
        restConnection.initialize();
    }

    @Override
    public void populateHttpClientBuilder(final HttpClientBuilder httpClientBuilder, final RequestConfig.Builder defaultRequestConfigBuilder) throws IntegrationException {
        restConnection.populateHttpClientBuilder(httpClientBuilder, defaultRequestConfigBuilder);
    }

    @Override
    public void completeConnection() throws IntegrationException {
        restConnection.completeConnection();
    }

    @Override
    public RequestBuilder createRequestBuilder(final HttpMethod method) throws IntegrationException {
        return restConnection.createRequestBuilder(method);
    }

    @Override
    public RequestBuilder createRequestBuilder(final HttpMethod method, final Map<String, String> additionalHeaders) throws IntegrationException {
        return restConnection.createRequestBuilder(method, additionalHeaders);
    }

    @Override
    public HttpUriRequest copyHttpRequest(final HttpUriRequest request) throws IntegrationException {
        return restConnection.copyHttpRequest(request);
    }

    @Override
    public Response executeRequestWithoutException(final HttpUriRequest request) throws IntegrationException {
        return restConnection.executeRequestWithoutException(request);
    }

    @Override
    public Response executeRequest(final HttpUriRequest request) throws IntegrationException {
        return restConnection.executeRequest(request);
    }

    @Override
    public Response executeRequest(final Request request) throws IntegrationException {
        return restConnection.executeRequest(request);
    }

    @Override
    public Optional<Response> executeGetRequestIfModifiedSince(final Request getRequest, final long timeToCheck) throws IntegrationException, IOException {
        return restConnection.executeGetRequestIfModifiedSince(getRequest, timeToCheck);
    }

    @Override
    public void logRequestHeaders(final HttpUriRequest request) {
        restConnection.logRequestHeaders(request);
    }

    @Override
    public void logResponseHeaders(final HttpResponse response) {
        restConnection.logResponseHeaders(response);
    }

    @Override
    public void addBuilderConnectionTimes() {
        restConnection.addBuilderConnectionTimes();
    }

    @Override
    public int getTimeout() {
        return restConnection.getTimeout();
    }

    @Override
    public void setTimeout(final int timeout) {
        restConnection.setTimeout(timeout);
    }

    @Override
    public boolean isAlwaysTrustServerCertificate() {
        return restConnection.isAlwaysTrustServerCertificate();
    }

    @Override
    public void setAlwaysTrustServerCertificate(final boolean alwaysTrustServerCertificate) {
        restConnection.setAlwaysTrustServerCertificate(alwaysTrustServerCertificate);
    }

    @Override
    public CloseableHttpClient getClient() {
        return restConnection.getClient();
    }

    @Override
    public void setClient(final CloseableHttpClient client) {
        restConnection.setClient(client);
    }

    @Override
    public ProxyInfo getProxyInfo() {
        return restConnection.getProxyInfo();
    }

    @Override
    public CredentialsProvider getCredentialsProvider() {
        return restConnection.getCredentialsProvider();
    }

    @Override
    public HttpClientBuilder getClientBuilder() {
        return restConnection.getClientBuilder();
    }

    @Override
    public RequestConfig.Builder getDefaultRequestConfigBuilder() {
        return restConnection.getDefaultRequestConfigBuilder();
    }

    @Override
    public Map<String, String> getCommonRequestHeaders() {
        return restConnection.getCommonRequestHeaders();
    }

    @Override
    public void addCommonRequestHeader(final String key, final String value) {
        restConnection.addCommonRequestHeader(key, value);
    }

    @Override
    public void addCommonRequestHeaders(final Map<String, String> commonRequestHeaders) {
        restConnection.addCommonRequestHeaders(commonRequestHeaders);
    }

    @Override
    public void close() throws IOException {
        restConnection.close();
    }

    @Override
    public IntLogger getLogger() {
        return restConnection.getLogger();
    }

    @Override
    public void validate(final BuilderStatus builderStatus) {
        restConnection.validate(builderStatus);
    }
}

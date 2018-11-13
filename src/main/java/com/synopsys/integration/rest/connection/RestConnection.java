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

/**
 * The parent class of all rest connections.
 */
public interface RestConnection extends Closeable {
    String ERROR_MSG_PROXY_INFO_NULL = "A RestConnection's proxy information cannot be null";

    void initialize() throws IntegrationException;

    /**
     * Subclasses can add to the builders any additional fields they need to successfully initialize
     */
    void populateHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) throws IntegrationException;

    /**
     * Subclasses might need to do final processing to the http client (usually authentication)
     */
    void completeConnection() throws IntegrationException;

    RequestBuilder createRequestBuilder(final HttpMethod method) throws IntegrationException;

    RequestBuilder createRequestBuilder(final HttpMethod method, final Map<String, String> additionalHeaders) throws IntegrationException;

    HttpUriRequest copyHttpRequest(final HttpUriRequest request) throws IntegrationException;

    Response executeRequestWithoutException(final HttpUriRequest request) throws IntegrationException;

    /**
     * Will throw an exception if the status code is an error code
     */
    Response executeRequest(final HttpUriRequest request) throws IntegrationException;

    /**
     * Will throw an exception if the status code is an error code
     */
    Response executeRequest(final Request request) throws IntegrationException;

    Optional<Response> executeGetRequestIfModifiedSince(final Request getRequest, final long timeToCheck) throws IntegrationException, IOException;

    void logRequestHeaders(final HttpUriRequest request);

    void logResponseHeaders(final HttpResponse response);

    void addBuilderConnectionTimes();

    int getTimeout();

    void setTimeout(final int timeout);

    boolean isAlwaysTrustServerCertificate();

    void setAlwaysTrustServerCertificate(final boolean alwaysTrustServerCertificate);

    CloseableHttpClient getClient();

    void setClient(final CloseableHttpClient client);

    ProxyInfo getProxyInfo();

    CredentialsProvider getCredentialsProvider();

    HttpClientBuilder getClientBuilder();

    RequestConfig.Builder getDefaultRequestConfigBuilder();

    Map<String, String> getCommonRequestHeaders();

    void addCommonRequestHeader(final String key, final String value);

    void addCommonRequestHeaders(final Map<String, String> commonRequestHeaders);

    IntLogger getLogger();

    void validate(final BuilderStatus builder);
}

/**
 * integration-rest
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.rest.request;

import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.body.BodyContent;
import com.synopsys.integration.util.Stringable;

public class Request extends Stringable {
    private final String uri;
    private final HttpMethod method;
    private final String mimeType;
    private final Charset bodyEncoding;
    private final Map<String, Set<String>> queryParameters;
    private final Map<String, String> additionalHeaders;
    private final BodyContent bodyContent;

    private Request(Builder builder) {
        uri = builder.getUri();
        method = builder.getMethod();
        mimeType = builder.getMimeType();
        bodyEncoding = builder.getBodyEncoding();
        queryParameters = builder.getQueryParameters();
        additionalHeaders = builder.getAdditionalHeaders();
        bodyContent = builder.getBodyContent();
    }

    public Request(String uri, HttpMethod method, String mimeType, Charset bodyEncoding, Map<String, Set<String>> queryParameters, Map<String, String> additionalHeaders, BodyContent bodyContent) {
        this.uri = uri;
        this.method = method;
        this.mimeType = mimeType;
        this.bodyEncoding = bodyEncoding;
        this.queryParameters = queryParameters;
        this.additionalHeaders = additionalHeaders;
        this.bodyContent = bodyContent;
    }

    public HttpEntity createHttpEntity() {
        if (bodyContent == null) {
            return null;
        }
        return bodyContent.createEntity(this);
    }

    public String getUri() {
        return uri;
    }

    public Map<String, Set<String>> getPopulatedQueryParameters() {
        Map<String, Set<String>> populatedQueryParameters = new HashMap<>();
        if (getQueryParameters() != null && !getQueryParameters().isEmpty()) {
            populatedQueryParameters.putAll(getQueryParameters());
        }
        return populatedQueryParameters;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Charset getBodyEncoding() {
        return bodyEncoding;
    }

    public Map<String, Set<String>> getQueryParameters() {
        return queryParameters;
    }

    public Map<String, String> getAdditionalHeaders() {
        return additionalHeaders;
    }

    public BodyContent getBodyContent() {
        return bodyContent;
    }

    public HttpUriRequest createHttpUriRequest(Map<String, String> commonRequestHeaders) throws IntegrationException {
        Request request = this;

        if (request.getMethod() == null) {
            throw new IntegrationException("Missing the HttpMethod");
        }
        try {
            URIBuilder uriBuilder;
            if (StringUtils.isNotBlank(request.getUri())) {
                uriBuilder = new URIBuilder(request.getUri());
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
            if (request.getAdditionalHeaders() != null && !request.getAdditionalHeaders().isEmpty()) {
                for (Map.Entry<String, String> header : request.getAdditionalHeaders().entrySet()) {
                    requestBuilder.addHeader(header.getKey(), header.getValue());
                }
            }
            if (!commonRequestHeaders.isEmpty()) {
                for (Map.Entry<String, String> header : commonRequestHeaders.entrySet()) {
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

    public static class Builder {
        private String uri;
        private HttpMethod method;
        private String mimeType;
        private Charset bodyEncoding;
        private Map<String, Set<String>> queryParameters;
        private Map<String, String> additionalHeaders;
        private BodyContent bodyContent;

        public Builder(Request request) {
            uri = request.uri;
            method = request.method;
            mimeType = request.mimeType;
            bodyEncoding = request.bodyEncoding;
            if (request.queryParameters != null) {
                queryParameters = new HashMap<>(request.queryParameters);
            }
            if (request.additionalHeaders != null) {
                additionalHeaders = new HashMap<>(request.additionalHeaders);
            }
            bodyContent = request.bodyContent;
        }

        public Builder(String uri) {
            this.uri = uri;
            method = HttpMethod.GET;
            mimeType = ContentType.APPLICATION_JSON.getMimeType();
            bodyEncoding = StandardCharsets.UTF_8;
        }

        public Builder() {
            this((String) null);
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder bodyEncoding(Charset bodyEncoding) {
            this.bodyEncoding = bodyEncoding;
            return this;
        }

        public Builder queryParameters(Map<String, Set<String>> queryParameters) {
            this.queryParameters = queryParameters;
            return this;
        }

        public Builder addQueryParameter(String key, String value) {
            if (queryParameters == null) {
                queryParameters = new HashMap<>();
            }
            queryParameters.computeIfAbsent(key, k -> new HashSet<>()).add(value);
            return this;
        }

        public Builder additionalHeaders(Map<String, String> additionalHeaders) {
            this.additionalHeaders = additionalHeaders;
            return this;
        }

        public Builder addAdditionalHeader(String key, String value) {
            if (additionalHeaders == null) {
                additionalHeaders = new HashMap<>();
            }
            additionalHeaders.put(key, value);
            return this;
        }

        public Builder bodyContent(BodyContent bodyContent) {
            this.bodyContent = bodyContent;
            return this;
        }

        public Request build() {
            return new Request(this);
        }

        public String getUri() {
            return uri;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public String getMimeType() {
            return mimeType;
        }

        public Charset getBodyEncoding() {
            return bodyEncoding;
        }

        public Map<String, Set<String>> getQueryParameters() {
            return queryParameters;
        }

        public Map<String, String> getAdditionalHeaders() {
            return additionalHeaders;
        }

        public BodyContent getBodyContent() {
            return bodyContent;
        }
    }

}

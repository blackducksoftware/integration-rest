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
package com.synopsys.integration.rest.request;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;

import com.synopsys.integration.builder.Buildable;
import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.builder.IntegrationBuilder;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.BodyContent;
import com.synopsys.integration.util.Stringable;

public class Request extends Stringable implements Buildable {
    public static final String DEFAULT_ACCEPT_MIME_TYPE = ContentType.APPLICATION_JSON.getMimeType();

    private final HttpUrl url;
    private final HttpMethod method;
    private final String acceptMimeType;
    private final Charset bodyEncoding;
    private final Map<String, Set<String>> queryParameters = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private final BodyContent bodyContent;

    public Request(HttpUrl url, HttpMethod method, String acceptMimeType, Charset bodyEncoding, Map<String, Set<String>> queryParameters, Map<String, String> headers, BodyContent bodyContent) {
        this.url = url;
        this.method = method;
        this.acceptMimeType = StringUtils.isBlank(acceptMimeType) ? DEFAULT_ACCEPT_MIME_TYPE : acceptMimeType;
        this.bodyEncoding = null == bodyEncoding ? StandardCharsets.UTF_8 : bodyEncoding;
        this.queryParameters.putAll(queryParameters);
        this.headers.putAll(headers);
        this.bodyContent = bodyContent;
    }

    public Request(Builder builder) {
        this(builder.url, builder.method, builder.acceptMimeType, builder.bodyEncoding, builder.queryParameters, builder.headers, builder.bodyContent);
    }

    public Request.Builder createBuilder() {
        return new Builder(this);
    }

    public HttpEntity createHttpEntity() {
        if (bodyContent == null) {
            return null;
        }
        return bodyContent.createEntity(this);
    }

    public HttpUrl getUrl() {
        return url;
    }

    public Map<String, Set<String>> getPopulatedQueryParameters() {
        return new HashMap<>(queryParameters);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getAcceptMimeType() {
        return acceptMimeType;
    }

    public Charset getBodyEncoding() {
        return bodyEncoding;
    }

    public Map<String, Set<String>> getQueryParameters() {
        return queryParameters;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public BodyContent getBodyContent() {
        return bodyContent;
    }

    public static class Builder extends IntegrationBuilder<Request> {
        private HttpUrl url;
        private HttpMethod method;
        private String acceptMimeType;
        private Charset bodyEncoding;
        private Map<String, Set<String>> queryParameters = new HashMap<>();
        private Map<String, String> headers = new HashMap<>();
        private BodyContent bodyContent;

        public Builder(Request request) {
            url = request.url;
            method = request.method;
            acceptMimeType = request.acceptMimeType;
            bodyEncoding = request.bodyEncoding;
            queryParameters.putAll(request.queryParameters);
            headers.putAll(request.headers);
            bodyContent = request.bodyContent;
        }

        public Builder() {
            this(null, HttpMethod.GET);
        }

        public Builder(HttpUrl url) {
            this(url, HttpMethod.GET);
        }

        public Builder(HttpUrl url, HttpMethod method) {
            this(url, method, new HashMap<>());
        }

        public Builder(HttpUrl url, HttpMethod method, Map<String, String> headers) {
            this.url = url;
            this.method = method;
            this.headers.putAll(headers);
            acceptMimeType = DEFAULT_ACCEPT_MIME_TYPE;
            bodyEncoding = StandardCharsets.UTF_8;
        }

        @Override
        protected Request buildWithoutValidation() {
            return new Request(
                getUrl(),
                getMethod(),
                getAcceptMimeType(),
                getBodyEncoding(),
                getQueryParameters(),
                getHeaders(),
                getBodyContent());
        }

        @Override
        protected void validate(BuilderStatus builderStatus) {
            // currently, all Request instances are valid
        }

        public Builder url(HttpUrl url) {
            this.url = url;
            return this;
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder acceptMimeType(String acceptMimeType) {
            this.acceptMimeType = acceptMimeType;
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
            queryParameters.computeIfAbsent(key, k -> new HashSet<>()).add(value);
            return this;
        }

        public Builder setQueryParameter(String key, String value) {
            queryParameters.put(key, new HashSet<>(Arrays.asList(value)));
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder addHeader(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public Builder bodyContent(BodyContent bodyContent) {
            this.bodyContent = bodyContent;
            return this;
        }

        public HttpUrl getUrl() {
            return url;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public String getAcceptMimeType() {
            return acceptMimeType;
        }

        public Charset getBodyEncoding() {
            return bodyEncoding;
        }

        public Map<String, Set<String>> getQueryParameters() {
            return queryParameters;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public BodyContent getBodyContent() {
            return bodyContent;
        }
    }

}
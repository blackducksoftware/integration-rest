/*
 * integration-rest
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.request;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.blackduck.integration.rest.HttpMethod;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.body.BodyContent;
import com.synopsys.integration.builder.Buildable;
import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.builder.IntegrationBuilder;
import com.synopsys.integration.util.Stringable;

public class Request extends Stringable implements Buildable {
    private final HttpUrl url;
    private final HttpMethod method;
    private final Charset bodyEncoding;
    private final Map<String, Set<String>> queryParameters = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private final BodyContent bodyContent;

    public Request(HttpUrl url, HttpMethod method, Charset bodyEncoding, Map<String, Set<String>> queryParameters, Map<String, String> headers, BodyContent bodyContent) {
        this.url = url;
        this.method = method;
        this.bodyEncoding = null == bodyEncoding ? StandardCharsets.UTF_8 : bodyEncoding;
        this.queryParameters.putAll(queryParameters);
        this.headers.putAll(headers);
        this.bodyContent = bodyContent;
    }

    public Request(Builder builder) {
        this(builder.url, builder.method, builder.bodyEncoding, builder.queryParameters, builder.headers, builder.bodyContent);
    }

    public Request.Builder createBuilder() {
        return new Builder(this);
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
        private Charset bodyEncoding;
        private Map<String, Set<String>> queryParameters = new HashMap<>();
        private Map<String, String> headers = new HashMap<>();
        private BodyContent bodyContent;

        public Builder(Request request) {
            url = request.url;
            method = request.method;
            bodyEncoding = request.bodyEncoding;
            queryParameters.putAll(request.queryParameters);
            headers.putAll(request.headers);
            bodyContent = request.bodyContent;
        }

        public Builder(Builder builder) {
            url = builder.url;
            method = builder.method;
            bodyEncoding = builder.bodyEncoding;
            queryParameters.putAll(builder.queryParameters);
            headers.putAll(builder.headers);
            bodyContent = builder.bodyContent;
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
            bodyEncoding = StandardCharsets.UTF_8;
        }

        @Override
        protected Request buildWithoutValidation() {
            return new Request(
                getUrl(),
                getMethod(),
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
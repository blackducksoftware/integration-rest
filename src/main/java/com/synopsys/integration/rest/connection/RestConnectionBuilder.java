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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.util.BuilderStatus;
import com.synopsys.integration.util.IntegrationBuilder;

public abstract class RestConnectionBuilder<C extends RestConnection> extends IntegrationBuilder<C> {
    private String baseUrl;
    private int timeout = 120;
    private ProxyInfo proxyInfo;
    private IntLogger logger;
    private boolean alwaysTrustServerCertificate;
    private Map<String, String> commonRequestHeaders = new HashMap<>();

    @Override
    public final C build() throws IllegalArgumentException {
        final C restConnection = super.build();

        restConnection.setAlwaysTrustServerCertificate(alwaysTrustServerCertificate);
        if (!commonRequestHeaders.isEmpty()) {
            restConnection.getCommonRequestHeaders().putAll(commonRequestHeaders);
        }

        return restConnection;
    }

    @Override
    protected void validate(final BuilderStatus builderStatus) {
        if (StringUtils.isBlank(baseUrl)) {
            builderStatus.addErrorMessage("No base url was provided.");
        } else {
            try {
                final URL url = new URL(baseUrl);
                url.toURI();
            } catch (final MalformedURLException e) {
                builderStatus.addErrorMessage("The provided base url is not a valid java.net.URL.");
            } catch (final URISyntaxException e) {
                builderStatus.addErrorMessage("The provided base url is not a valid java.net.URI.");
            }
        }

        if (0 >= timeout) {
            builderStatus.addErrorMessage("The timeout must be greater than 0.");
        }

        if (null == logger) {
            builderStatus.addErrorMessage("The logger instance may not be null.");
        }

        if (null == commonRequestHeaders) {
            builderStatus.addErrorMessage("The common request headers map cannot be null.");
        }
    }

    public void addCommonRequestHeader(final String key, final String value) {
        commonRequestHeaders.put(key, value);
    }

    public Optional<URL> getURL() {
        try {
            return Optional.of(new URL(getBaseUrl()));
        } catch (final MalformedURLException e) {
            getLogger().error(String.format("The provided url, %s, was malformed and should have been caught by validation.", getBaseUrl()));
        }

        return Optional.empty();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public void setProxyInfo(final ProxyInfo proxyInfo) {
        this.proxyInfo = proxyInfo;
    }

    public IntLogger getLogger() {
        return logger;
    }

    public void setLogger(final IntLogger logger) {
        this.logger = logger;
    }

    public boolean isAlwaysTrustServerCertificate() {
        return alwaysTrustServerCertificate;
    }

    public void setAlwaysTrustServerCertificate(final boolean alwaysTrustServerCertificate) {
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
    }

    public Map<String, String> getCommonRequestHeaders() {
        return commonRequestHeaders;
    }

    public void setCommonRequestHeaders(final Map<String, String> commonRequestHeaders) {
        this.commonRequestHeaders = commonRequestHeaders;
    }
}

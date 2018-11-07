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

import java.util.HashMap;
import java.util.Map;

import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.util.BuilderStatus;
import com.synopsys.integration.util.IntegrationBuilder;

public abstract class RestConnectionBuilder<C extends RestConnection> extends IntegrationBuilder<C> {
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

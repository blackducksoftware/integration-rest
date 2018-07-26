/**
 * integration-rest
 * <p>
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.rest.connection;

import java.net.URL;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;

public class UnauthenticatedRestConnection extends RestConnection {
    public UnauthenticatedRestConnection(IntLogger logger, URL hubBaseUrl, ProxyInfo proxyInfo) {
        super(logger, hubBaseUrl, proxyInfo);
    }

    public UnauthenticatedRestConnection(IntLogger logger, URL hubBaseUrl, int timeout, ProxyInfo proxyInfo) {
        super(logger, hubBaseUrl, timeout, proxyInfo);
    }

    @Override
    public void populateHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) throws IntegrationException {
        // no additional fields to populate
    }

    @Override
    public void completeConnection() throws IntegrationException {
        // nothing additional needed to connect
    }

}

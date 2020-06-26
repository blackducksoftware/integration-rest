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

import java.util.Base64;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;

public class BasicAuthHttpClient extends AuthenticatingIntHttpClient {
    private static final String AUTHORIZATION_TYPE = "Basic";

    private final AuthenticationSupport authenticationSupport;
    private final String username;
    private final String password;

    public BasicAuthHttpClient(IntLogger logger, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, AuthenticationSupport authenticationSupport, String username, String password) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo);
        this.authenticationSupport = authenticationSupport;

        this.username = username;
        this.password = password;
    }

    @Override
    protected final void populateAuthenticatingHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) {
        httpClientBuilder.setDefaultCookieStore(new BasicCookieStore());
        defaultRequestConfigBuilder.setCookieSpec(CookieSpecs.DEFAULT);
        populateBasicHttpClientBuilder(httpClientBuilder, defaultRequestConfigBuilder);
    }

    protected void populateBasicHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) {
        // Subclasses can optionally add to the builders any additional fields they need to successfully initialize
    }

    @Override
    public void handleErrorResponse(HttpUriRequest request, Response response) {
        super.handleErrorResponse(request, response);

        authenticationSupport.handleErrorResponse(this, request, response, RestConstants.X_CSRF_TOKEN);
    }

    @Override
    public boolean isAlreadyAuthenticated(HttpUriRequest request) {
        return request.containsHeader(AuthenticationSupport.AUTHORIZATION_HEADER);
    }

    @Override
    protected void completeAuthenticationRequest(HttpUriRequest request, Response responseIgnored) {
        Base64.Encoder encoder = Base64.getEncoder();
        String unencodedAuthPair = String.format("%s:%s", username, password);
        String encodedAuthPair = encoder.encodeToString(unencodedAuthPair.getBytes());
        String encodedHeaderValue = String.format("%s %s", AUTHORIZATION_TYPE, encodedAuthPair);

        authenticationSupport.addAuthenticationHeader(this, request, AuthenticationSupport.AUTHORIZATION_HEADER, encodedHeaderValue);
    }

    @Override
    public final Response attemptAuthentication() throws IntegrationException {
        // Nothing to do for Basic Auth
        return null;
    }

}

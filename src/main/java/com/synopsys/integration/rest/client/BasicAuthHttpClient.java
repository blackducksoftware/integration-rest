/*
 * integration-rest
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.client;

import java.util.Base64;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;

public class BasicAuthHttpClient extends AuthenticatingIntHttpClient {
    private static final String AUTHORIZATION_TYPE = "Basic";

    private final AuthenticationSupport authenticationSupport;
    private final String username;
    private final String password;

    public BasicAuthHttpClient(IntLogger logger, Gson gson, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, AuthenticationSupport authenticationSupport, String username, String password) {
        super(logger, gson, timeout, alwaysTrustServerCertificate, proxyInfo);
        this.authenticationSupport = authenticationSupport;

        this.username = username;
        this.password = password;
    }

    @Override
    public boolean isAlreadyAuthenticated(HttpUriRequest request) {
        return request.containsHeader(AuthenticationSupport.AUTHORIZATION_HEADER);
    }

    @Override
    public final Response attemptAuthentication() throws IntegrationException {
        // Nothing to do for Basic Auth
        return null;
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
    protected void addToHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) {
        super.addToHttpClientBuilder(httpClientBuilder, defaultRequestConfigBuilder);
        httpClientBuilder.setDefaultCookieStore(new BasicCookieStore());
        defaultRequestConfigBuilder.setCookieSpec(CookieSpecs.DEFAULT);
    }

}

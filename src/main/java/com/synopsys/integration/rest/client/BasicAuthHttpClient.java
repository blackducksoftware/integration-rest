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
import com.synopsys.integration.rest.request.Response;
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
    public void populateHttpClientBuilder(final HttpClientBuilder httpClientBuilder, final RequestConfig.Builder defaultRequestConfigBuilder) {
        httpClientBuilder.setDefaultCookieStore(new BasicCookieStore());
        defaultRequestConfigBuilder.setCookieSpec(CookieSpecs.DEFAULT);
    }

    @Override
    public void handleErrorResponse(final HttpUriRequest request, final Response response) {
        super.handleErrorResponse(request, response);

        authenticationSupport.handleErrorResponse(this, request, response, RestConstants.X_CSRF_TOKEN);
    }

    @Override
    public boolean isAlreadyAuthenticated(final HttpUriRequest request) {
        return request.containsHeader(AuthenticationSupport.AUTHORIZATION_HEADER);
    }

    @Override
    protected void completeAuthenticationRequest(final HttpUriRequest request, final Response responseIgnored) {
        final Base64.Encoder encoder = Base64.getEncoder();
        final String unencodedAuthPair = String.format("%s:%s", username, password);
        final String encodedAuthPair = encoder.encodeToString(unencodedAuthPair.getBytes());
        final String encodedHeaderValue = String.format("%s %s", AUTHORIZATION_TYPE, encodedAuthPair);

        authenticationSupport.addAuthenticationHeader(this, request, AuthenticationSupport.AUTHORIZATION_HEADER, encodedHeaderValue);
    }

    @Override
    public final Response attemptAuthentication() throws IntegrationException {
        // Nothing to do for Basic Auth
        return null;
    }

}

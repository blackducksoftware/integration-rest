package com.blackduck.integration.rest.client;

import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.blackduck.integration.rest.RestConstants;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.response.DefaultResponse;
import com.blackduck.integration.rest.response.Response;
import com.google.gson.Gson;

class AuthenticatingIntHttpClientTest {
    private boolean isAuthenticated = false;
    private boolean isAuthenticationExpired = false;

    private final CloseableHttpResponse successfulResponse = Mockito.mock(CloseableHttpResponse.class);
    private final CloseableHttpResponse failureResponse = Mockito.mock(CloseableHttpResponse.class);
    private final Gson gson = new Gson();

    private AuthenticatingIntHttpClient authenticatingIntHttpClient;

    @BeforeEach
    void setUp() throws IOException {
        isAuthenticated = false;
        isAuthenticationExpired = false;
        Mockito.when(successfulResponse.getStatusLine()).thenReturn(new StatusLine() {
            @Override
            public ProtocolVersion getProtocolVersion() {
                return null;
            }

            @Override
            public int getStatusCode() {
                return RestConstants.OK_200;
            }

            @Override
            public String getReasonPhrase() {
                return null;
            }
        });
        Mockito.when(failureResponse.getStatusLine()).thenReturn(new StatusLine() {
            @Override
            public ProtocolVersion getProtocolVersion() {
                return null;
            }

            @Override
            public int getStatusCode() {
                return RestConstants.UNAUTHORIZED_401;
            }

            @Override
            public String getReasonPhrase() {
                return null;
            }
        });

        final IntLogger logger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));
        final CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
        Mockito.doAnswer(invocation -> {
            if (isAuthenticated && !isAuthenticationExpired) {
                return successfulResponse;
            } else {
                return failureResponse;
            }
        }).when(httpClient).execute(any(), any(HttpContext.class));

        final HttpClientBuilder httpClientBuilder = Mockito.mock(HttpClientBuilder.class);
        Mockito.when(httpClientBuilder.build()).thenReturn(httpClient);
        authenticatingIntHttpClient = new AuthenticatingIntHttpClient(logger, gson, 10, true, ProxyInfo.NO_PROXY_INFO, new BasicCredentialsProvider(), httpClientBuilder, RequestConfig.custom(), new HashMap<>()) {
            @Override
            public boolean isAlreadyAuthenticated(final HttpUriRequest request) {
                return isAuthenticated;
            }

            @Override
            public Response attemptAuthentication() {
                return new DefaultResponse(null, httpClient, successfulResponse);
            }

            @Override
            protected void completeAuthenticationRequest(final HttpUriRequest request, final Response response) {
                isAuthenticated = true;
                isAuthenticationExpired = false;
            }

            @Override
            public boolean canConnect() {
                return true;
            }

            @Override
            public ConnectionResult attemptConnection() {
                return ConnectionResult.SUCCESS(RestConstants.OK_200);
            }
        };
    }

    @Test
    void testUnauthenticated() throws IntegrationException, URISyntaxException {
        final HttpUriRequest request = Mockito.mock(HttpUriRequest.class);
        Mockito.when(request.getURI()).thenReturn(new URI("http://blackduck.com"));
        final Response response = authenticatingIntHttpClient.execute(request);
        Assertions.assertEquals(RestConstants.OK_200, response.getStatusCode(), "Expected the initial authentication to be successful.");
    }

    @Test
    void testExpiredAuthentication() throws IntegrationException, URISyntaxException {
        isAuthenticated = true;
        isAuthenticationExpired = true;
        final HttpUriRequest request = Mockito.mock(HttpUriRequest.class);
        Mockito.when(request.getURI()).thenReturn(new URI("http://blackduck.com"));

        final Response failedResponse = authenticatingIntHttpClient.execute(request);
        Assertions.assertEquals(RestConstants.OK_200, failedResponse.getStatusCode(), "Expected an authenticated but expired request to be successful because the authentication should have been reattempted.");
    }
}
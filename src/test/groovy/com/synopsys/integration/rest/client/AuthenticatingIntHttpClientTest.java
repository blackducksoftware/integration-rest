package com.synopsys.integration.rest.client;

import static org.mockito.ArgumentMatchers.any;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpUriRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

class AuthenticatingIntHttpClientTest {
    private boolean isAuthenticated = false;
    private boolean isAuthenticationExpired = false;

    private final Response successfulResponse = Mockito.mock(Response.class);
    private final Response failureResponse = Mockito.mock(Response.class);
    private AuthenticatingIntHttpClient authenticatingIntHttpClient;

    @BeforeEach
    void setUp() throws IntegrationException {
        isAuthenticated = false;
        isAuthenticationExpired = false;
        Mockito.when(successfulResponse.getStatusCode()).thenReturn(RestConstants.OK_200);
        Mockito.when(failureResponse.getStatusCode()).thenReturn(RestConstants.UNAUTHORIZED_401);

        final IntLogger logger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));
        authenticatingIntHttpClient = Mockito.spy(new AuthenticatingIntHttpClient(logger, 10, true, ProxyInfo.NO_PROXY_INFO) {
            @Override
            public boolean isAlreadyAuthenticated(final HttpUriRequest request) {
                return isAuthenticated;
            }

            @Override
            public Response attemptAuthentication() throws IntegrationException {
                return successfulResponse;
            }

            @Override
            protected void completeAuthenticationRequest(final HttpUriRequest request, final Response response) throws IntegrationException {
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

            @Override
            public Response execute(final Request request) throws IntegrationException {
                return super.execute(request);
            }
        });
        Mockito.doAnswer(invocation -> {
            if (isAuthenticated && !isAuthenticationExpired) {
                return successfulResponse;
            } else {
                return failureResponse;
            }
        }).when(authenticatingIntHttpClient).handleClientExecution(any());

    }

    @Test
    void testUnauthenticated() throws IntegrationException, URISyntaxException {
        final HttpUriRequest request = Mockito.mock(HttpUriRequest.class);
        Mockito.when(request.getURI()).thenReturn(new URI("http://synopsys.com"));
        final Response response = authenticatingIntHttpClient.execute(request);
        Assertions.assertEquals(new Integer(RestConstants.OK_200), response.getStatusCode(), "Expected the initial authentication to be successful.");
    }

    @Test
    void testExpiredAuthentication() throws IntegrationException, URISyntaxException {
        isAuthenticated = true;
        isAuthenticationExpired = true;
        final HttpUriRequest request = Mockito.mock(HttpUriRequest.class);
        Mockito.when(request.getURI()).thenReturn(new URI("http://synopsys.com"));

        final Response failedResponse = authenticatingIntHttpClient.execute(request);
        Assertions.assertEquals(new Integer(RestConstants.OK_200), failedResponse.getStatusCode(), "Expected an authenticated but expired request to be successful because the authentication should have been reattempted.");
    }
}
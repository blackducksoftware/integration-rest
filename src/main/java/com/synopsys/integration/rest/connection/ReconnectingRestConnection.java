package com.synopsys.integration.rest.connection;

import org.apache.http.client.methods.HttpUriRequest;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Response;

/**
 * A rest connection that will attempt to reconnect in the event of the client being unauthorized multiple times before
 * throwing an exception. Other unsuccessful status codes will result in an exception being thrown
 */
public abstract class ReconnectingRestConnection extends RestConnection {
    public ReconnectingRestConnection(final IntLogger logger, final ProxyInfo proxyInfo) {
        super(logger, proxyInfo);
    }

    public ReconnectingRestConnection(final IntLogger logger, final int timeout, final ProxyInfo proxyInfo) {
        super(logger, timeout, proxyInfo);
    }

    @Override
    public Response executeRequest(final HttpUriRequest request) throws IntegrationException {
        return handleClientExecution(request, 0);
    }

    private Response handleClientExecution(final HttpUriRequest request, final int retryCount) throws IntegrationException {
        final Response response = super.executeRequest(request);
        final int statusCode = response.getStatusCode();

        if (statusCode == RestConstants.UNAUTHORIZED_401 && retryCount < 2) {
            completeConnection();
            final HttpUriRequest newRequest = copyHttpRequest(request);
            return handleClientExecution(newRequest, retryCount + 1);
        }

        return response;
    }
}

package com.synopsys.integration.rest.connection;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class UnauthenticatedRestConnection extends RestConnection {
    public UnauthenticatedRestConnection(final IntLogger logger, final int timeout, final boolean alwaysTrustServerCertificate, final ProxyInfo proxyInfo) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo);
    }

    @Override
    public void populateHttpClientBuilder(final HttpClientBuilder httpClientBuilder, final RequestConfig.Builder defaultRequestConfigBuilder) {
        // Nothing to populate
    }

    @Override
    public void completeConnection() {
        // No authentication required
    }
}

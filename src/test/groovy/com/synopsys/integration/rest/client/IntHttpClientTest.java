package com.synopsys.integration.rest.client;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;

import com.synopsys.integration.rest.HttpUrl;

public class IntHttpClientTest {

    @Test
    public void testExecuteGetRequestIfModifiedSinceGets404() throws IOException, IntegrationException {
        IntLogger logger = new BufferedIntLogger();
        int timeoutInSeconds = 30;
        boolean alwaysTrustServerCertificate = true;
        ProxyInfo proxyInfo = ProxyInfo.NO_PROXY_INFO;
        CredentialsProvider credentialsProvider = null;
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        RequestConfig.Builder defaultRequestConfigBuilder = RequestConfig.custom();
        Map<String, String> commonRequestHeaders = new HashMap<>(0);

        IntHttpClient client = new IntHttpClient(logger, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo,
            credentialsProvider, clientBuilder, defaultRequestConfigBuilder, commonRequestHeaders);

        HttpUrl url = new HttpUrl("https://google.com/download/thisdoesntexist.zip");
        Request getRequest = new Request.Builder()
                                 .url(url)
                                 .build();
        long timeToCheck = 0L;
        try {
            client.executeGetRequestIfModifiedSince(getRequest, timeToCheck);
            fail("Expected an exception in response to an HTTP 404");
        } catch (IntegrationException e) {
            System.out.printf("Got the expected exception type. The exception message is:\n\t%s\n", e.getMessage());
        }
    }
}
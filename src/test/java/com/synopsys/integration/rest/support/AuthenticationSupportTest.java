package com.synopsys.integration.rest.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.client.AuthenticatingIntHttpClient;

public class AuthenticationSupportTest {
    @Test
    public void testContentLengthSetWithoutEntity() throws IntegrationException, IOException {
        SetupMocks setupMocks = new SetupMocks();
        setupMocks.authenticationSupport.attemptAuthentication(setupMocks.mockClient, new HttpUrl("https://www.synopsys.com"), setupMocks.requestBuilder);

        Mockito.verify(setupMocks.mockHttpClient).execute(setupMocks.requestCaptor.capture());

        Assertions.assertTrue(setupMocks.requestCaptor.getValue().containsHeader(HttpHeaders.CONTENT_LENGTH));
        Assertions.assertEquals("0", setupMocks.requestCaptor.getValue().getFirstHeader(HttpHeaders.CONTENT_LENGTH).getValue());
    }

    @Test
    public void testContentLengthWithEntity() throws IntegrationException, IOException {
        SetupMocks setupMocks = new SetupMocks();
        setupMocks.requestBuilder.setEntity(new StringEntity("lots of data", StandardCharsets.UTF_8));
        setupMocks.authenticationSupport.attemptAuthentication(setupMocks.mockClient, new HttpUrl("https://www.synopsys.com"), setupMocks.requestBuilder);

        Mockito.verify(setupMocks.mockHttpClient).execute(setupMocks.requestCaptor.capture());

        Assertions.assertTrue(setupMocks.requestCaptor.getValue().containsHeader(HttpHeaders.CONTENT_LENGTH));
        Assertions.assertTrue(Integer.parseInt(setupMocks.requestCaptor.getValue().getFirstHeader(HttpHeaders.CONTENT_LENGTH).getValue()) > 0);
    }

    private class SetupMocks {
        public CloseableHttpClient mockHttpClient;
        public AuthenticatingIntHttpClient mockClient;
        public AuthenticationSupport authenticationSupport;
        public ArgumentCaptor<HttpUriRequest> requestCaptor;
        public RequestBuilder requestBuilder;

        public SetupMocks() {
            //mockHttpClient = Mockito.mock(CloseableHttpClient.class);

            //  HttpClientBuilder mockHttpClientBuilder = Mockito.mock(HttpClientBuilder.class);
            //Mockito.when(mockHttpClientBuilder.build()).thenReturn(mockHttpClient);
            //
            //            mockClient = Mockito.mock(AuthenticatingIntHttpClient.class);
            //            Mockito.when(mockClient.getClientBuilder()).thenReturn(mockHttpClientBuilder);
            //
            //            authenticationSupport = new AuthenticationSupport();
            //
            //            requestCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
            //            requestBuilder = RequestBuilder.create(HttpMethod.POST.name());
        }
    }

}

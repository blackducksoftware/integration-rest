package com.synopsys.integration.rest.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.client.AuthenticatingIntHttpClient;

public class AuthenticationSupportTest {
    @Test
    public void testContentLengthSetWithoutEntity() throws IntegrationException, IOException {
        CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
        HttpClientBuilder mockHttpClientBuilder = Mockito.mock(HttpClientBuilder.class);
        Mockito.when(mockHttpClientBuilder.build()).thenReturn(mockHttpClient);

        AuthenticatingIntHttpClient mockClient = Mockito.mock(AuthenticatingIntHttpClient.class);
        Mockito.when(mockClient.getClientBuilder()).thenReturn(mockHttpClientBuilder);

        AuthenticationSupport authenticationSupport = new AuthenticationSupport();

        ArgumentCaptor<HttpUriRequest> requestCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        RequestBuilder requestBuilder = RequestBuilder.create(HttpMethod.POST.name());

        authenticationSupport.attemptAuthentication(mockClient, new HttpUrl("https://www.synopsys.com"), requestBuilder);

        Mockito.verify(mockHttpClient).execute(requestCaptor.capture());

        Assertions.assertTrue(requestCaptor.getValue().containsHeader(HttpHeaders.CONTENT_LENGTH));
        Assertions.assertEquals("0", requestCaptor.getValue().getFirstHeader(HttpHeaders.CONTENT_LENGTH).getValue());
    }

    @Test
    public void testContentLengthWithEntity() throws IntegrationException, IOException {
        CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
        HttpClientBuilder mockHttpClientBuilder = Mockito.mock(HttpClientBuilder.class);
        Mockito.when(mockHttpClientBuilder.build()).thenReturn(mockHttpClient);

        AuthenticatingIntHttpClient mockClient = Mockito.mock(AuthenticatingIntHttpClient.class);
        Mockito.when(mockClient.getClientBuilder()).thenReturn(mockHttpClientBuilder);

        AuthenticationSupport authenticationSupport = new AuthenticationSupport();

        ArgumentCaptor<HttpUriRequest> requestCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        RequestBuilder requestBuilder = RequestBuilder.create(HttpMethod.POST.name());

        requestBuilder.setEntity(new StringEntity("lots of data", StandardCharsets.UTF_8));
        authenticationSupport.attemptAuthentication(mockClient, new HttpUrl("https://www.synopsys.com"), requestBuilder);

        Mockito.verify(mockHttpClient).execute(requestCaptor.capture());

        Assertions.assertTrue(requestCaptor.getValue().containsHeader(HttpHeaders.CONTENT_LENGTH));
        Assertions.assertTrue(Integer.parseInt(requestCaptor.getValue().getFirstHeader(HttpHeaders.CONTENT_LENGTH).getValue()) > 0);
    }

}

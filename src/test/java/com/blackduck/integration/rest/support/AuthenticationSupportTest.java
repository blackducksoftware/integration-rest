package com.blackduck.integration.rest.support;

import java.io.IOException;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpMethod;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.client.AuthenticatingIntHttpClient;

class AuthenticationSupportTest {
    @Test
    void testContentLengthSetWithoutEntity() throws IntegrationException, IOException {
        CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
        HttpClientBuilder mockHttpClientBuilder = Mockito.mock(HttpClientBuilder.class);
        Mockito.when(mockHttpClientBuilder.build()).thenReturn(mockHttpClient);

        AuthenticatingIntHttpClient mockClient = Mockito.mock(AuthenticatingIntHttpClient.class);
        Mockito.when(mockClient.getClientBuilder()).thenReturn(mockHttpClientBuilder);

        AuthenticationSupport authenticationSupport = new AuthenticationSupport();

        ArgumentCaptor<HttpUriRequest> requestCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        RequestBuilder requestBuilder = RequestBuilder.create(HttpMethod.POST.name());

        authenticationSupport.attemptAuthentication(mockClient, new HttpUrl("https://www.blackduck.com"), requestBuilder);

        Mockito.verify(mockHttpClient).execute(requestCaptor.capture());

        Assertions.assertTrue(requestCaptor.getValue().containsHeader(HttpHeaders.CONTENT_LENGTH));
        Assertions.assertEquals("0", requestCaptor.getValue().getFirstHeader(HttpHeaders.CONTENT_LENGTH).getValue());
    }



    @Test
    void testReplaceAuthorizationHeader() {
        CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
        HttpClientBuilder mockHttpClientBuilder = Mockito.mock(HttpClientBuilder.class);
        Mockito.when(mockHttpClientBuilder.build()).thenReturn(mockHttpClient);

        AuthenticatingIntHttpClient mockClient = Mockito.mock(AuthenticatingIntHttpClient.class);
        Mockito.when(mockClient.getClientBuilder()).thenReturn(mockHttpClientBuilder);

        AuthenticationSupport authenticationSupport = new AuthenticationSupport();
        RequestBuilder requestBuilder = RequestBuilder.create(HttpMethod.POST.name());
        HttpUriRequest request = requestBuilder.build();

        authenticationSupport.addAuthenticationHeader(mockClient, request, AuthenticationSupport.AUTHORIZATION_HEADER, "Original Authorization Value");
        authenticationSupport.addAuthenticationHeader(mockClient, request, AuthenticationSupport.AUTHORIZATION_HEADER, "Updated Authorization Value");
        Assertions.assertTrue(request.containsHeader(HttpHeaders.AUTHORIZATION));
        Assertions.assertEquals(1, request.getHeaders(HttpHeaders.AUTHORIZATION).length);
        Assertions.assertEquals("Updated Authorization Value", request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
    }

}

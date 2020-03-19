package com.synopsys.integration.rest.support;

import com.synopsys.integration.log.SilentIntLogger;
import com.synopsys.integration.rest.client.AuthenticatingIntHttpClient;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthenticationSupportTest {
    @Test
    public void testAttemptAuthentication() throws Exception {
        assertUrlPiecesCombineAsExpected("http://www.google.com/pathpiece", "login", "http://www.google.com/pathpiece/login");
        assertUrlPiecesCombineAsExpected("http://www.google.com/pathpiece/", "login", "http://www.google.com/pathpiece/login");
        assertUrlPiecesCombineAsExpected("http://www.google.com/pathpiece", "/login", "http://www.google.com/pathpiece/login");
        assertUrlPiecesCombineAsExpected("http://www.google.com/pathpiece/", "/login", "http://www.google.com/pathpiece/login");
        assertUrlPiecesCombineAsExpected("http://www.google.com/pathpiece", "./login", "http://www.google.com/pathpiece/login");
        assertUrlPiecesCombineAsExpected("http://www.google.com/pathpiece/", "./login", "http://www.google.com/pathpiece/login");
    }

    private void assertUrlPiecesCombineAsExpected(String baseUrl, String authenticationUrl, String expectedResult) throws Exception {
        AuthenticatingIntHttpClient authenticatingIntHttpClient = Mockito.mock(AuthenticatingIntHttpClient.class);
        HttpClientBuilder mockHttpClientBuilder = Mockito.mock(HttpClientBuilder.class);
        CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);

        Mockito.when(authenticatingIntHttpClient.getClientBuilder()).thenReturn(mockHttpClientBuilder);
        Mockito.when(mockHttpClientBuilder.build()).thenReturn(mockHttpClient);

        ArgumentCaptor<HttpUriRequest> requestArgumentCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        Mockito.when(mockHttpClient.execute(requestArgumentCaptor.capture())).thenReturn(Mockito.mock(CloseableHttpResponse.class));

        RequestBuilder requestBuilder = RequestBuilder.create(HttpGet.METHOD_NAME);

        Field loggerField = FieldUtils.getField(AuthenticatingIntHttpClient.class, "logger", true);
        FieldUtils.writeField(loggerField, authenticatingIntHttpClient, new SilentIntLogger());

        AuthenticationSupport authenticationSupport = new AuthenticationSupport();
        authenticationSupport.attemptAuthentication(authenticatingIntHttpClient, baseUrl, authenticationUrl, requestBuilder);

        HttpUriRequest request = requestArgumentCaptor.getValue();
        assertEquals(expectedResult, request.getURI().toString());
    }

}

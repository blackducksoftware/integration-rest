/**
 * Hub Common Rest
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.*/
package com.synopsys.integration.rest

import com.synopsys.integration.exception.IntegrationException
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.LogLevel
import com.synopsys.integration.log.PrintStreamIntLogger
import com.synopsys.integration.rest.connection.RestConnection
import com.synopsys.integration.rest.connection.UnauthenticatedRestConnection
import com.synopsys.integration.rest.connection.UnauthenticatedRestConnectionBuilder
import com.synopsys.integration.rest.credentials.Credentials
import com.synopsys.integration.rest.exception.IntegrationRestException
import com.synopsys.integration.rest.proxy.ProxyInfo
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder
import com.synopsys.integration.rest.request.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.apache.commons.codec.Charsets
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.ContentType
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.nio.charset.Charset

class RestConnectionTest {
    public static final int CONNECTION_TIMEOUT = 213

    private final MockWebServer server = new MockWebServer();

    @Before
    public void setUp() throws Exception {
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    private RestConnection getRestConnection() {
        getRestConnection(new MockResponse().setResponseCode(200))
    }

    private RestConnection getRestConnection(MockResponse response) {
        final Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                response
            }
        };
        server.setDispatcher(dispatcher);
        UnauthenticatedRestConnectionBuilder builder = new UnauthenticatedRestConnectionBuilder();
        builder.logger = new PrintStreamIntLogger(System.out, LogLevel.TRACE);
        builder.baseUrl = server.url("/")
        builder.timeout = CONNECTION_TIMEOUT
        builder.setProxyInfo(ProxyInfo.NO_PROXY_INFO);
        builder.build()
    }

    @Test
    public void testClientBuilding() {
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)
        int timeoutSeconds = 213
        int timeoutMilliSeconds = timeoutSeconds * 1000
        UnauthenticatedRestConnectionBuilder builder = new UnauthenticatedRestConnectionBuilder()
        builder.logger = logger
        builder.baseUrl = server.url("/").url()
        builder.timeout = timeoutSeconds
        builder.setProxyInfo(ProxyInfo.NO_PROXY_INFO)
        builder.alwaysTrustServerCertificate = true

        RestConnection restConnection = builder.build()
        def realClient = restConnection.client
        assert null == realClient
        restConnection.connect()
        realClient = restConnection.client
        assert timeoutMilliSeconds == realClient.defaultConfig.socketTimeout
        assert timeoutMilliSeconds == realClient.defaultConfig.connectionRequestTimeout
        assert timeoutMilliSeconds == realClient.defaultConfig.connectTimeout
        assert null == realClient.defaultConfig.proxy

        String proxyHost = "ProxyHost"
        int proxyPort = 3128
        String proxyIgnoredHosts = "IgnoredHost"
        ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
        proxyBuilder.host = proxyHost
        proxyBuilder.port = proxyPort
        proxyBuilder.credentials = new Credentials("testUser", "password")
        proxyBuilder.ignoredProxyHosts = proxyIgnoredHosts
        ProxyInfo proxyInfo = proxyBuilder.build()
        builder = new UnauthenticatedRestConnectionBuilder()
        builder.logger = logger
        builder.baseUrl = server.url("/").url()
        builder.timeout = timeoutSeconds
        builder.setProxyInfo(proxyInfo)
        restConnection = builder.build()

        restConnection.connect()
        realClient = restConnection.client
        assert null != realClient.defaultConfig.proxy

        proxyIgnoredHosts = ".*"
        proxyBuilder = new ProxyInfoBuilder()
        proxyBuilder.host = proxyHost
        proxyBuilder.port = proxyPort
        proxyBuilder.credentials = new Credentials("testUser", "password")
        proxyBuilder.ignoredProxyHosts = proxyIgnoredHosts
        proxyInfo = proxyBuilder.build()
        builder = new UnauthenticatedRestConnectionBuilder()
        builder.logger = logger
        builder.baseUrl = server.url("/").url()
        builder.timeout = timeoutSeconds
        builder.setProxyInfo(proxyInfo)
        restConnection = builder.build()

        restConnection.connect()
        realClient = restConnection.client
        assert null == realClient.defaultConfig.proxy
    }

    @Test
    public void testRestConnectionNoProxy() {
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)
        int timeoutSeconds = 213
        UnauthenticatedRestConnection restConnection = new UnauthenticatedRestConnection(logger, timeoutSeconds, null)
        try {
            restConnection.connect();
            fail('Should have thrown exception')
        } catch (IllegalStateException e) {
            assert RestConnection.ERROR_MSG_PROXY_INFO_NULL == e.getMessage()
        }
    }

    @Test
    public void testToString() {
        RestConnection restConnection = getRestConnection()
        String s = "RestConnection [baseUrl=${server.url("/").toString()}]"
        assert s.equals(restConnection.toString())
    }

    @Test
    public void testHandleExecuteClientCallSuccessful() {
        RestConnection restConnection = getRestConnection()
        restConnection.commonRequestHeaders.put("Common", "Header")
        RequestBuilder requestBuilder = restConnection.createRequestBuilder(HttpMethod.DELETE);
        assert null != requestBuilder.getHeaders("Common")

        restConnection.executeRequest(requestBuilder.build()).withCloseable { assert 200 == it.getStatusCode() }
    }

    @Test
    public void testHandleExecuteClientCallFail() {
        RestConnection restConnection = getRestConnection()
        RequestBuilder requestBuilder = restConnection.createRequestBuilder(HttpMethod.GET);
        HttpUriRequest request = requestBuilder.build();
        restConnection.connect()

        restConnection = getRestConnection(new MockResponse().setResponseCode(404))
        try {
            restConnection.executeRequest(request)
            fail('Should have thrown exception')
        } catch (IntegrationRestException e) {
            assert 404 == e.httpStatusCode
        }

        restConnection = getRestConnection(new MockResponse().setResponseCode(401))
        try {
            restConnection.executeRequest(request)
            fail('Should have thrown exception')
        } catch (IntegrationRestException e) {
            assert 401 == e.httpStatusCode
        }
    }

    @Test
    public void testCreateHttpRequestNoRequest() {
        RestConnection restConnection = new UnauthenticatedRestConnection(new PrintStreamIntLogger(System.out, LogLevel.TRACE), null, 300, ProxyInfo.NO_PROXY_INFO)
        try {
            restConnection.createHttpRequest(null)
            fail('Should have thrown exception')
        } catch (IntegrationException e) {
            assert "Missing the Request" == e.getMessage()
        }
    }

    @Test
    public void testCreateHttpRequestNoURI() {
        RestConnection restConnection = new UnauthenticatedRestConnection(new PrintStreamIntLogger(System.out, LogLevel.TRACE), null, 300, ProxyInfo.NO_PROXY_INFO)
        Request request = new Request.Builder(null).build();
        try {
            restConnection.createHttpRequest(request)
            fail('Should have thrown exception')
        } catch (IntegrationException e) {
            assert "Missing the URI" == e.getMessage()
        }
    }

    @Test
    public void testCreateHttpRequest() {
        RestConnection restConnection = getRestConnection()

        String uri = restConnection.baseUrl.toURI().toString()
        Map<String, String> queryParametes = [test: "one", query: "two"]
        String q = 'q'
        String mimeType = 'mime'
        Charset bodyEncoding = Charsets.UTF_8

        Request request = new Request.Builder(null).build()
        HttpRequestBase requestBase = restConnection.createHttpRequest(request)
        assert HttpMethod.GET.name() == requestBase.method
        assert ContentType.APPLICATION_JSON.getMimeType() == requestBase.getFirstHeader(HttpHeaders.ACCEPT).getValue()
        assert null != requestBase.getURI()
        assert requestBase.getURI().toString().contains(restConnection.baseUrl.toURI().toString())

        request = new Request.Builder(uri).build()
        requestBase = restConnection.createHttpRequest(request)
        assert HttpMethod.GET.name() == requestBase.method
        assert ContentType.APPLICATION_JSON.getMimeType() == requestBase.getFirstHeader(HttpHeaders.ACCEPT).getValue()
        assert null != requestBase.getURI()
        assert requestBase.getURI().toString().contains(restConnection.baseUrl.toURI().toString())

        request = new Request.Builder(uri).queryParameters([offset: ['0'] as Set, limit: ['100'] as Set]).build()
        requestBase = restConnection.createHttpRequest(request)
        assert HttpMethod.GET.name() == requestBase.method
        assert ContentType.APPLICATION_JSON.getMimeType() == requestBase.getFirstHeader(HttpHeaders.ACCEPT).getValue()
        assert null != requestBase.getURI()
        assert requestBase.getURI().toString().contains(restConnection.baseUrl.toURI().toString())
        assert requestBase.getURI().toString().contains('offset=0')
        assert requestBase.getURI().toString().contains('limit=100')

        request = new Request.Builder(uri).queryParameters([q: ['q'] as Set, test: ['one'] as Set, query: ['two'] as Set, offset: ['0'] as Set, limit: ['100'] as Set]).mimeType('mime').additionalHeaders([header: 'one', thing: 'two']).
            build()
        requestBase = restConnection.createHttpRequest(request)
        assert HttpMethod.GET.name() == requestBase.method
        assert 'one' == requestBase.getFirstHeader('header').getValue()
        assert 'two' == requestBase.getFirstHeader('thing').getValue()
        assert null != requestBase.getURI()
        assert requestBase.getURI().toString().contains(restConnection.baseUrl.toURI().toString())
        assert requestBase.getURI().toString().contains('offset=0')
        assert requestBase.getURI().toString().contains('limit=100')

        Map headersMap = [header: 'one', thing: 'two']
        headersMap.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType())
        request = new Request.Builder(uri).queryParameters([q: ['q'] as Set, test: ['one'] as Set, query: ['two'] as Set, offset: ['0'] as Set, limit: ['100'] as Set]).mimeType('mime').bodyEncoding(bodyEncoding).
            additionalHeaders(headersMap).build()
        requestBase = restConnection.createHttpRequest(request)
        assert HttpMethod.GET.name() == requestBase.method
        assert ContentType.APPLICATION_XML.getMimeType() == requestBase.getFirstHeader(HttpHeaders.ACCEPT).getValue()
        assert null != requestBase.getURI()
        assert requestBase.getURI().toString().contains(restConnection.baseUrl.toURI().toString())
        assert requestBase.getURI().toString().contains('offset=0')
        assert requestBase.getURI().toString().contains('limit=100')

        Request deleteRequest = new Request.Builder(uri).method(HttpMethod.DELETE).mimeType('mime').bodyEncoding(bodyEncoding).additionalHeaders([header: 'one', thing: 'two']).build()
        requestBase = restConnection.createHttpRequest(deleteRequest)
        assert HttpMethod.DELETE.name() == requestBase.method
        assert 'one' == requestBase.getFirstHeader('header').getValue()
        assert 'two' == requestBase.getFirstHeader('thing').getValue()
        assert 2 == requestBase.getAllHeaders().size()
        assert null != requestBase.getURI()
        assert requestBase.getURI().toString().contains(restConnection.baseUrl.toURI().toString())
    }
}

package com.synopsys.integration.rest

import com.google.gson.Gson
import com.synopsys.integration.exception.IntegrationException
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.LogLevel
import com.synopsys.integration.log.PrintStreamIntLogger
import com.synopsys.integration.rest.client.IntHttpClient
import com.synopsys.integration.rest.exception.IntegrationRestException
import com.synopsys.integration.rest.proxy.ProxyInfo
import com.synopsys.integration.rest.request.Request
import com.synopsys.integration.rest.response.Response
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.ContentType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

import static org.junit.jupiter.api.Assertions.fail

class IntHttpClientTest {
    public static final int CONNECTION_TIMEOUT = 213

    private final MockWebServer server = new MockWebServer()
    private final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG)
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() throws Exception {
        server.start()
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown()
    }

    private HttpUrl getValidUrl() {
        return new HttpUrl(server.url("www.synopsys.com").uri())
    }

    private IntHttpClient getRestConnection() {
        getRestConnection(new MockResponse().setResponseCode(200))
    }

    private IntHttpClient getRestConnection(MockResponse response) {
        final Dispatcher dispatcher = new Dispatcher() {
            @Override
            MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                response
            }
        }
        server.setDispatcher(dispatcher)

        return new IntHttpClient(logger, gson, CONNECTION_TIMEOUT, false, ProxyInfo.NO_PROXY_INFO)
    }

    @Test
    void testRestConnectionNoProxy() {
        int timeoutSeconds = 213

        try {
            new IntHttpClient(logger, gson, timeoutSeconds, true, null)
            fail('Should have thrown exception')
        } catch (IllegalArgumentException e) {
            assert IntHttpClient.ERROR_MSG_PROXY_INFO_NULL == e.getMessage()
        }
    }

    @Test
    void testHandleExecuteClientCallSuccessful() {
        IntHttpClient restConnection = getRestConnection()
        restConnection.commonRequestHeaders.put("Common", "Header")
        RequestBuilder requestBuilder = restConnection.createRequestBuilder(HttpMethod.DELETE)
        requestBuilder.setUri(getValidUrl().uri())
        assert null != requestBuilder.getHeaders("Common")

        Response response = restConnection.execute(requestBuilder.build())

        assert 200 == response.getStatusCode()
    }

    @Test
    void testHandleExecuteClientCallFail() {
        IntHttpClient restConnection = getRestConnection(new MockResponse().setResponseCode(404))
        RequestBuilder requestBuilder = restConnection.createRequestBuilder(HttpMethod.GET)
        requestBuilder.setUri(getValidUrl().uri())
        HttpUriRequest request = requestBuilder.build()
        try {
            final Response response = restConnection.execute(request)
            assert 404 == response.getStatusCode()
        } catch (IntegrationRestException e) {
            fail('Should NOT have thrown exception')
        }

        restConnection = getRestConnection(new MockResponse().setResponseCode(401))
        try {
            final Response response = restConnection.execute(request)
            assert 401 == response.getStatusCode()
        } catch (IntegrationRestException e) {
            fail('Should NOT have thrown exception')
        }
    }

    @Test
    void testCreateHttpRequestNoURI() {
        IntHttpClient restConnection = new IntHttpClient(logger, gson, 300, true, ProxyInfo.NO_PROXY_INFO)
        Request request = new Request.Builder().build()
        try {
            restConnection.createHttpUriRequest(request)
            fail('Should have thrown exception')
        } catch (IntegrationException e) {
            assert "Missing the HttpUrl" == e.getMessage()
        }
    }

    @Test
    void testCreateHttpRequest() {
        IntHttpClient restConnection = getRestConnection()

        final HttpUrl url = getValidUrl()
        Charset bodyEncoding = StandardCharsets.UTF_8

        Request request = new Request.Builder(url).build()
        HttpUriRequest uriRequest = restConnection.createHttpUriRequest(request)
        assert HttpMethod.GET.name() == uriRequest.method
        assert null == uriRequest.getFirstHeader(HttpHeaders.ACCEPT)
        assert null != uriRequest.getURI()
        assert uriRequest.getURI().toString().contains(url.string())

        request = new Request.Builder(url).build()
        uriRequest = restConnection.createHttpUriRequest(request)
        assert HttpMethod.GET.name() == uriRequest.method
        assert null == uriRequest.getFirstHeader(HttpHeaders.ACCEPT)
        assert null != uriRequest.getURI()
        assert uriRequest.getURI().toString().contains(url.string())

        request = new Request.Builder(url).queryParameters([offset: ['0'] as Set, limit: ['100'] as Set]).build()
        uriRequest = restConnection.createHttpUriRequest(request)
        assert HttpMethod.GET.name() == uriRequest.method
        assert null == uriRequest.getFirstHeader(HttpHeaders.ACCEPT)
        assert null != uriRequest.getURI()
        assert uriRequest.getURI().toString().contains(url.string())
        assert uriRequest.getURI().toString().contains('offset=0')
        assert uriRequest.getURI().toString().contains('limit=100')

        request = new Request.Builder(url).queryParameters([q: ['q'] as Set, test: ['one'] as Set, query: ['two'] as Set, offset: ['0'] as Set, limit: ['100'] as Set]).acceptMimeType('mime').headers([header: 'one', thing: 'two']).
                build()
        uriRequest = restConnection.createHttpUriRequest(request)
        assert HttpMethod.GET.name() == uriRequest.method
        assert 'one' == uriRequest.getFirstHeader('header').getValue()
        assert 'two' == uriRequest.getFirstHeader('thing').getValue()
        assert null != uriRequest.getURI()
        assert uriRequest.getURI().toString().contains(url.string())
        assert uriRequest.getURI().toString().contains('offset=0')
        assert uriRequest.getURI().toString().contains('limit=100')

        Map headersMap = [header: 'one', thing: 'two']
        headersMap.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType())
        request = new Request.Builder(url).queryParameters([q: ['q'] as Set, test: ['one'] as Set, query: ['two'] as Set, offset: ['0'] as Set, limit: ['100'] as Set]).acceptMimeType('mime').bodyEncoding(bodyEncoding).
                headers(headersMap).build()
        uriRequest = restConnection.createHttpUriRequest(request)
        assert HttpMethod.GET.name() == uriRequest.method
        assert ContentType.APPLICATION_XML.getMimeType() == uriRequest.getFirstHeader(HttpHeaders.ACCEPT).getValue()
        assert null != uriRequest.getURI()
        assert uriRequest.getURI().toString().contains(url.string())
        assert uriRequest.getURI().toString().contains('offset=0')
        assert uriRequest.getURI().toString().contains('limit=100')

        Request deleteRequest = new Request.Builder(url).method(HttpMethod.DELETE).acceptMimeType('mime').bodyEncoding(bodyEncoding).headers([header: 'one', thing: 'two']).build()
        uriRequest = restConnection.createHttpUriRequest(deleteRequest)
        assert HttpMethod.DELETE.name() == uriRequest.method
        assert 'one' == uriRequest.getFirstHeader('header').getValue()
        assert 'two' == uriRequest.getFirstHeader('thing').getValue()
        assert 2 == uriRequest.getAllHeaders().size()
        assert null != uriRequest.getURI()
        assert uriRequest.getURI().toString().contains(url.string())
    }

}
